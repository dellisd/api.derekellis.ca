package ca.derekellis.api.server.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@ServerScope
abstract class NetworkComponent {
  @Provides
  open fun httpClientEngine(): HttpClientEngine = OkHttp.create()
}
