package ca.derekellis.api.lambda

import ca.derekellis.api.models.Journey
import ca.derekellis.api.models.RaptorRequest
import ca.derekellis.api.models.RaptorResponse
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
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
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div

class RaptorHandler : RequestStreamHandler {
  private val logger = LoggerFactory.getLogger(javaClass)

  override fun handleRequest(input: InputStream, output: OutputStream, context: Context) {
    val rawInput = Json.decodeFromStream<JsonObject>(input)

    val request: RaptorRequest = Json.decodeFromString(rawInput.getValue("body").jsonPrimitive.content)
    logger.info("Received: $request")

    val result = runBlocking(Dispatchers.Default) {
      val base = launchAsyncJourney(Path("./base.db"), request)
      val double = launchAsyncJourney(Path("./double.db"), request)

      RaptorResponse(base.await(), double.await())
    }

    Json.encodeToStream(result, output)
  }

  private fun CoroutineScope.launchAsyncJourney(path: Path, request: RaptorRequest): Deferred<List<Journey>> = async {
    checkAndCopyDatabaseFile(path)
    journey(path, request)
  }

  private fun checkAndCopyDatabaseFile(path: Path) {
    val dest = Path("/tmp") / path
    if (!Files.exists(dest)) {
      Files.copy(path, dest)
    }
  }
}
