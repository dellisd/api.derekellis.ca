package ca.derekellis.api.lambda

import ca.derekellis.api.lambda.models.RaptorRequest
import ca.derekellis.api.lambda.models.RaptorResponse
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.json.jsonPrimitive
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.OutputStream

class RaptorHandler : RequestStreamHandler {
  private val logger = LoggerFactory.getLogger(javaClass)

  override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
    val rawInput = Json.decodeFromStream<JsonObject>(input)

    val request: RaptorRequest = Json.decodeFromString(rawInput.getValue("body").jsonPrimitive.content)
    logger.info("Received: $request")

    val result = runBlocking(Dispatchers.Default) {
      val base = async { journey("./base.db", request) }
      val double = async { journey("./double.db", request) }

      RaptorResponse(base.await(), double.await())
    }

    Json.encodeToStream(result, output)
  }
}
