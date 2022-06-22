package ca.derekellis.api.server.routes

import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing

interface RoutingContainer {
  context(Routing)
  fun route(): Route
}
