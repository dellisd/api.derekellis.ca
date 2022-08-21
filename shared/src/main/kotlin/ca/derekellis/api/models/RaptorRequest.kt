package ca.derekellis.api.models

import kotlinx.serialization.Serializable
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable
data class RaptorRequest(val origin: String, val destination: String, val time: String) {
  val localTime: LocalTime
    get() = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"))
}
