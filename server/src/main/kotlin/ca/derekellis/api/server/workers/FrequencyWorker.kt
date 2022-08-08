package ca.derekellis.api.server.workers

import ca.derekellis.api.models.Journey
import ca.derekellis.api.models.Leg
import ca.derekellis.api.models.RaptorResponse
import ca.derekellis.api.models.RouteLeg
import ca.derekellis.api.models.TransferLeg
import ca.derekellis.api.server.di.DataPath
import ca.derekellis.api.server.models.FrequencyComparisonResponse
import ca.derekellis.api.server.models.FrequencyEntry
import ca.derekellis.api.server.models.FrequencyHistoryRequest
import ca.derekellis.api.server.models.FrequencyHistoryResponse
import ca.derekellis.api.server.models.Part
import ca.derekellis.kgtfs.domain.model.GtfsTime
import ca.derekellis.kgtfs.domain.model.ServiceId
import ca.derekellis.kgtfs.domain.model.StopId
import ca.derekellis.kgtfs.domain.model.StopTime
import ca.derekellis.kgtfs.domain.model.TripId
import ca.derekellis.kgtfs.domain.model.toGtfsTime
import ca.derekellis.kgtfs.dsl.Gtfs
import ca.derekellis.kgtfs.dsl.StaticGtfsScope
import ca.derekellis.kgtfs.ext.lineStringBetween
import io.github.dellisd.spatialk.geojson.dsl.feature
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import me.tatarka.inject.annotations.Inject
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import kotlin.io.path.div

@Inject
class FrequencyWorker(val gtfs: Gtfs = TODO(), private val dataPath: DataPath?) {

  suspend fun getComparisonInfo(data: RaptorResponse): FrequencyComparisonResponse {
    val base = data.base.filter { it.duration < Duration.ofHours(4) }.take(2).map { getJourneyInfo(it) }
    val double = data.double.filter { it.duration < Duration.ofHours(4) }.take(2).map { getJourneyInfo(it) }
    return FrequencyComparisonResponse(base, double)
  }

  private suspend fun getJourneyInfo(journey: Journey): List<Part> = gtfs {
    journey.legs.map { leg ->
      when (leg) {
        is RouteLeg -> leg.map()
        is TransferLeg -> leg.map()
      }
    }
  }

  context(StaticGtfsScope)
    private fun RouteLeg.map(): Part {
    val origin = stops.getById(from)
    val destination = stops.getById(to)

    val trip = trips.getById(trip)

    // Route number
    val routeNumber = trip.route.shortName ?: ""
    val heading = trip.headsign ?: ""
    // Shape
    val geometry = trip.shape?.lineStringBetween(origin, destination)

    // TODO: Compute frequency
    return Part.Route(
      from.value,
      to.value,
      feature(geometry),
      start.toLocalTime(),
      end.toLocalTime(),
      routeNumber,
      heading,
      0..0
    )
  }

  private fun TransferLeg.map(): Part =
    Part.Transfer(from.value, to.value, geometry, start.toLocalTime(), end.toLocalTime())

  private fun GtfsTime.toLocalTime(): LocalTime = LocalTime.of(hour, minute, second)

  private val Journey.duration: Duration get() = legs.last().end - legs.first().start

  private val Leg.start: GtfsTime
    get() = when (this) {
      is RouteLeg -> this.start
      is TransferLeg -> this.start
    }

  private val Leg.end: GtfsTime
    get() = when (this) {
      is RouteLeg -> this.end
      is TransferLeg -> this.end
    }

  suspend fun getFrequencyHistory(request: FrequencyHistoryRequest): FrequencyHistoryResponse = coroutineScope {
    requireNotNull(dataPath) { "Data path must be specified" }

    val data = (2009..2022).map { year ->
      async {
        if (request.destination != null) {
          getFrequencyBetweenStop(year, request.origin, request.destination)
        } else {
          getFrequencyAtStop(year, request.origin)
        }
      }
    }
      .awaitAll()
      .flatten()

    FrequencyHistoryResponse(data)
  }

  private suspend fun getFrequencyAtStop(year: Int, stop: String): List<FrequencyEntry> = gtfsForYear(year) {
    val date = getDateForYear(year).with(DayOfWeek.WEDNESDAY)
    val serviceIds = calendar.onDate(date).map { it.serviceId }.toSet()

    val tripsAtStop = stops.getById(stop).trips
      .asSequence()
      .filter { it.serviceId in serviceIds }
    val times = tripsAtStop
      .map { it.timeAtStop(StopId(stop)) }
      .sortedBy { it.arrivalTime }

    mapToFrequencies(year, times.toList())
  }

  private suspend fun getFrequencyBetweenStop(year: Int, from: String, to: String): List<FrequencyEntry> =
    gtfsForYear(year) {
      val date = getDateForYear(year).with(DayOfWeek.WEDNESDAY)
      val serviceIds = calendar.onDate(date).map { it.serviceId }.toSet()

      //language=SQLite
      val allTimes = rawQuery(
        """
      SELECT A.*, T.service_id
      FROM StopTime A
               JOIN StopTime B ON A.trip_id = B.trip_id
               JOIN Trip T ON A.trip_id = T.trip_id
      WHERE A.stop_id = ?
        AND B.stop_id = ?
        AND A.stop_sequence < B.stop_sequence
      ORDER BY A.arrival_time;
    """.trimIndent(), mapper = { cursor ->
          buildList<Pair<ServiceId, StopTime>> {
            while (cursor.next()) {
              add(
                ServiceId(cursor.getString(7)!!) to StopTime(
                  TripId(cursor.getString(0)!!),
                  GtfsTime(cursor.getString(1)!!),
                  GtfsTime(cursor.getString(2)!!),
                  StopId(cursor.getString(3)!!),
                  cursor.getLong(4)!!.toInt(),
                  null,
                  cursor.getLong(5)!!.toInt(),
                  cursor.getLong(6)!!.toInt(),
                )
              )
            }
          }

        }, 2
      ) {
        bindString(1, from)
        bindString(2, to)
      }

      val times = allTimes
        .filter { (serviceId) -> serviceId in serviceIds }
        .map { (_, time) -> time }
      mapToFrequencies(year, times)
    }

  private fun mapToFrequencies(year: Int, times: List<StopTime>): List<FrequencyEntry> {
    val amPeak = times.filter { it.arrivalTime in am6..am9 } to "6AM-9AM"
    val midday = times.filter { it.arrivalTime in am9..pm3 } to "9AM-3PM"
    val pmPeak = times.filter { it.arrivalTime in pm3..pm6 } to "3PM-6PM"
    val evening = times.filter { it.arrivalTime in pm6..pm10 } to "6PM-10PM"

    return listOf(amPeak, midday, pmPeak, evening).map { (arrivals, label) ->
      val mean = arrivals.zipWithNext { a, b -> b.arrivalTime - a.arrivalTime }
        .map { it.toMinutes() }
        .toList()
        .average()
      FrequencyEntry(year, mean, label)
    }
  }

  private suspend fun <R> gtfsForYear(year: Int, block: StaticGtfsScope.() -> R): R {
    val gtfs = Gtfs(dataPath!! / "history/$year.db")
    return gtfs(block)
  }

  private fun getDateForYear(year: Int) = when (year) {
    in 2009..2021 -> LocalDate.of(year, 10, 30)
    2022 -> LocalDate.of(year, 7, 6)
    else -> throw IllegalArgumentException("Unsupported year!")
  }

  suspend fun getStops() = gtfsForYear(2022) {
    stops.getAll()
  }

  /**
   *
   */
  suspend fun getSelectableStops(origin: String) {

  }

  companion object {
    private val am6 = LocalTime.of(6, 0).toGtfsTime()
    private val am9 = LocalTime.of(9, 0).toGtfsTime()
    private val pm3 = LocalTime.of(15, 0).toGtfsTime()
    private val pm6 = LocalTime.of(18, 0).toGtfsTime()
    private val pm10 = LocalTime.of(22, 0).toGtfsTime()
  }
}

