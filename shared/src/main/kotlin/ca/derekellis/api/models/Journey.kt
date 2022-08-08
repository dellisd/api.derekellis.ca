package ca.derekellis.api.models

import kotlinx.serialization.Serializable

@Serializable
data class Journey(val legs: List<Leg>)
