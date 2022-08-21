package ca.derekellis.api.models

import kotlinx.serialization.Serializable

@Serializable
data class RaptorResponse(
  val base: List<Journey>,
  val double: List<Journey>
)
