package ca.derekellis.api.server.routes

import ca.derekellis.api.server.ServerConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
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
class FrequencyRoute(engine: HttpClientEngine) : RoutingContainer {
  private val client: HttpClient = HttpClient(engine) {
    install(ContentNegotiation) {
      json()
    }
  }

  context(Routing)
  override fun route() = route("frequency") {
    post("/") {
      val result = client.post(ServerConfig.LAMBDA_ENDPOINT) {
        contentType(ContentType.parse("application/json"))
        setBody(call.receive<JsonElement>())
      }

      // TODO: Post-processing
      call.respond(result.body<JsonElement>())
    }
  }
}
