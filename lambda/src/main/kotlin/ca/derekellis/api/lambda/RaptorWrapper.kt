package ca.derekellis.api.lambda

import ca.derekellis.api.lambda.models.Journey
import ca.derekellis.api.lambda.models.RaptorRequest
import ca.derekellis.api.lambda.models.RouteLeg
import ca.derekellis.api.lambda.models.TransferLeg
import ca.derekellis.kgtfs.domain.model.StopId
import ca.derekellis.kgtfs.raptor.Raptor
import ca.derekellis.kgtfs.raptor.providers.InMemoryProvider
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDate
import ca.derekellis.kgtfs.raptor.models.RouteLeg as RaptorRouteLeg
import ca.derekellis.kgtfs.raptor.models.TransferLeg as RaptorTransferLeg

private val DATE = LocalDate.of(2022, 5, 17)

suspend fun journey(path: String, request: RaptorRequest): List<Journey> {
  val logger = LoggerFactory.getLogger("journey")
  requireNotNull(request.origin) { "Origin can not be null" }
  requireNotNull(request.destination) { "Destination can not be null" }
  requireNotNull(request.time) { "Time can not be null" }

  val provider = InMemoryProvider.fromCache(path, DATE)
  logger.info("Loaded memory cache")
  val raptor = Raptor(provider)

  val results = raptor.journeys(
    StopId(request.origin),
    StopId(request.destination),
    request.localTime,
    buffer = Duration.ofMinutes(1)
  )
  logger.info("Computed results")
  return results.map { journey ->
    Journey(journey.legs.map {
      when (it) {
        is RaptorTransferLeg -> TransferLeg(
          it.from,
          it.to,
          it.start,
          it.end,
          it.duration.toSeconds(),
          it.distance,
          it.geometry
        )

        is RaptorRouteLeg -> RouteLeg(it.from, it.to, it.start, it.end, it.trip)
      }
    })
  }
}
