package ca.derekellis.api.server.routes

import ca.derekellis.api.server.ServerConfig
import ca.derekellis.api.server.models.FrequencyHistoryRequest
import ca.derekellis.api.server.workers.FrequencyComparisonWorker
import ca.derekellis.api.server.workers.FrequencyHistoryWorker
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.json.JsonElement
import me.tatarka.inject.annotations.Inject

@Inject
class FrequencyRoute(
  engine: HttpClientEngine,
  private val historyWorker: FrequencyHistoryWorker,
  private val comparisonWorker: FrequencyComparisonWorker
) : RoutingContainer {
  private val client: HttpClient = HttpClient(engine) {
    install(HttpTimeout) {
      requestTimeoutMillis = 30000
      socketTimeoutMillis = 30000
    }
    install(ContentNegotiation) {
      json()
    }
  }

  context(Routing)
  override fun route() = route("frequency") {
    post("/comparison") {
      val result = client.post(ServerConfig.LAMBDA_ENDPOINT) {
        contentType(ContentType.parse("application/json"))
        setBody(call.receive<JsonElement>())
      }

      val comparison = comparisonWorker.getComparisonInfo(result.body())
      call.respond(comparison)
    }

    post("/history") {
      val request = call.receive<FrequencyHistoryRequest>()
      call.respond(historyWorker.getFrequencyHistory(request))
    }
  }
}
