package ca.derekellis.api.server.models

import kotlinx.serialization.Serializable

@Serializable
data class FrequencyEntry(
  val year: Int,
  val mean: Double,
  val label: String
)
