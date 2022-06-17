package ca.derekellis.api.server

import ca.derekellis.api.server.di.ServerComponent
import ca.derekellis.api.server.di.create
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing

fun main() {
  val component = ServerComponent::class.create()
  embeddedServer(Netty, port = 8080) {
    routing {
      component.root.route()
    }
  }.start(wait = true)
}
