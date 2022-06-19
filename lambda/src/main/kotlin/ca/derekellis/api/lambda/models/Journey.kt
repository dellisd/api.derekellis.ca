package ca.derekellis.api.lambda.models

import kotlinx.serialization.Serializable

@Serializable
data class Journey(val legs: List<Leg>)
