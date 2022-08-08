package ca.derekellis.api.server.di

import ca.derekellis.api.server.routes.FrequencyRoute
import ca.derekellis.api.server.routes.RootRoute
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import java.nio.file.Path

typealias DataPath = Path

@Component
abstract class ServerComponent(
  @get:Provides protected val dataPath: DataPath?,
  @Component val networkComponent: NetworkComponent
) {
  abstract val root: RootRoute
  abstract val frequency: FrequencyRoute
}
