package ca.derekellis.api.server.models

import kotlinx.serialization.Serializable

@Serializable
data class FrequencyHistoryRequest(val origin: String, val destination: String? = null)
