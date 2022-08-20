package ca.derekellis.api.server.workers

import ca.derekellis.api.models.Journey
import ca.derekellis.api.models.Leg
import ca.derekellis.api.models.RaptorResponse
import ca.derekellis.api.models.RouteLeg
import ca.derekellis.api.models.TransferLeg
import ca.derekellis.api.server.di.DataPath
import ca.derekellis.api.server.models.FrequencyComparisonResponse
import ca.derekellis.api.server.models.Part
import ca.derekellis.kgtfs.domain.model.GtfsTime
import ca.derekellis.kgtfs.dsl.Gtfs
import ca.derekellis.kgtfs.dsl.StaticGtfsScope
import ca.derekellis.kgtfs.ext.lineStringBetween
import io.github.dellisd.spatialk.geojson.dsl.feature
import me.tatarka.inject.annotations.Inject
import java.time.Duration
import java.time.LocalTime
import kotlin.io.path.div

@Inject
class FrequencyComparisonWorker(private val dataPath: DataPath?) {
  private val base: Gtfs by lazy { Gtfs(dataPath!! / "comparison/base.db") }
  private val doubled: Gtfs by lazy { Gtfs(dataPath!! / "comparison/doubled.db") }

  suspend fun getComparisonInfo(data: RaptorResponse): FrequencyComparisonResponse {
    val base = data.base.filter { it.duration < Duration.ofHours(4) }.take(2).map { base { getJourneyInfo(it) } }
    val double = data.double.filter { it.duration < Duration.ofHours(4) }.take(2).map { doubled { getJourneyInfo(it) } }
    return FrequencyComparisonResponse(base, double)
  }

  context(StaticGtfsScope)
  private fun getJourneyInfo(journey: Journey): List<Part> = journey.legs.map { leg ->
    when (leg) {
      is RouteLeg -> leg.map()
      is TransferLeg -> leg.map()
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
}