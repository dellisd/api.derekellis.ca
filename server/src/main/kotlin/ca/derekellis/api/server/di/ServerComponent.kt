package ca.derekellis.api.server.di

import ca.derekellis.api.server.routes.FrequencyRoute
import ca.derekellis.api.server.routes.RootRoute
import me.tatarka.inject.annotations.Component

@Component
abstract class ServerComponent(
  @Component val networkComponent: NetworkComponent
) {
  abstract val root: RootRoute
  abstract val frequency: FrequencyRoute
}
