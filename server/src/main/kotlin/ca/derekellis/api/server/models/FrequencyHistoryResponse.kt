package ca.derekellis.api.server.models

import kotlinx.serialization.Serializable

@Serializable
data class FrequencyHistoryResponse(val data: List<FrequencyEntry>)
