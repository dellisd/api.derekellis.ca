package ca.derekellis.api.models

import ca.derekellis.kgtfs.domain.model.GtfsTime
import ca.derekellis.kgtfs.domain.model.StopId
import ca.derekellis.kgtfs.domain.model.TripId
import io.github.dellisd.spatialk.geojson.Feature
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Leg

@Serializable
@SerialName("transfer")
data class TransferLeg(
  val from: StopId,
  val to: StopId,
  val start: GtfsTime,
  val end: GtfsTime,
  val duration: Long,
  val distance: Double,
  val geometry: Feature?,
) : Leg()

@Serializable
@SerialName("route")
data class RouteLeg(
  val from: StopId,
  val to: StopId,
  val start: GtfsTime,
  val end: GtfsTime,
  val trip: TripId,
) : Leg()
