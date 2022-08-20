@file:UseSerializers(LocalTimeSerializer::class, IntRangeSerializer::class)

package ca.derekellis.api.server.models

import ca.derekellis.api.utils.IntRangeSerializer
import ca.derekellis.api.utils.LocalTimeSerializer
import io.github.dellisd.spatialk.geojson.Feature
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalTime

@Serializable
data class FrequencyComparisonResponse(
  val base: List<List<Part>>,
  val doubled: List<List<Part>>
)

@Serializable
sealed class Part {
  abstract val from: String
  abstract val to: String
  abstract val geometry: Feature?
  abstract val start: LocalTime
  abstract val end: LocalTime

  @SerialName("transfer")
  @Serializable
  data class Transfer(
    override val from: String,
    override val to: String,
    override val geometry: Feature?,
    override val start: LocalTime,
    override val end: LocalTime,
  ) : Part()

  @SerialName("route")
  @Serializable
  data class Route(
    override val from: String,
    override val to: String,
    override val geometry: Feature?,
    override val start: LocalTime,
    override val end: LocalTime,
    val number: String,
    val heading: String,
    val frequency: IntRange,
  ) : Part()
}