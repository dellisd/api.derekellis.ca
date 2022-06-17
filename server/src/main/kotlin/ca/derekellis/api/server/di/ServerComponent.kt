package ca.derekellis.api.server.di

import ca.derekellis.api.server.routes.RootRoute
import me.tatarka.inject.annotations.Component

@Component
abstract class ServerComponent {
  abstract val root: RootRoute
}
