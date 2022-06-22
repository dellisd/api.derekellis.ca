package ca.derekellis.api.server

import ca.derekellis.api.server.di.NetworkComponent
import ca.derekellis.api.server.di.ServerComponent
import ca.derekellis.api.server.di.create
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing

fun main() {
  val component = ServerComponent::class.create(NetworkComponent::class.create())
  embeddedServer(Netty, port = 8080) {
    install(ContentNegotiation) {
      json()
    }

    routing {
      component.root.route()
      component.frequency.route()
    }
  }.start(wait = true)
}
