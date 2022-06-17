package ca.derekellis.api.server.routes

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import me.tatarka.inject.annotations.Inject

@Inject
class RootRoute {

  context(Routing)
  fun route() {
    get("/") {
      call.respond("Hello World!")
    }
  }
}
