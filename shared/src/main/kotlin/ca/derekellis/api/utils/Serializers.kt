package ca.derekellis.api.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object IntRangeSerializer : KSerializer<IntRange> {
  override fun deserialize(decoder: Decoder): IntRange {
    val array = decoder.decodeSerializableValue(JsonArray.serializer())

    return array[0].jsonPrimitive.int..array[1].jsonPrimitive.int
  }

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IntRange", PrimitiveKind.INT)

  override fun serialize(encoder: Encoder, value: IntRange) {
    val array = buildJsonArray {
      add(JsonPrimitive(value.first))
      add(JsonPrimitive(value.last))
    }
    encoder.encodeSerializableValue(JsonArray.serializer(), array)
  }
}

object LocalTimeSerializer : KSerializer<LocalTime> {
  private val format = DateTimeFormatter.ofPattern("HH:mm")

  override fun deserialize(decoder: Decoder): LocalTime = LocalTime.parse(decoder.decodeString(), format)

  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: LocalTime) {
    encoder.encodeString(value.format(format))
  }
}
