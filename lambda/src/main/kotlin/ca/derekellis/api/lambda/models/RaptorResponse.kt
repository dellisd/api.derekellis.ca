package ca.derekellis.api.lambda.models

import kotlinx.serialization.Serializable

@Serializable
data class RaptorResponse(
  val base: List<Journey>,
  val double: List<Journey>
)
