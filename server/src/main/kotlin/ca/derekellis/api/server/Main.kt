package ca.derekellis.api.server

import ca.derekellis.api.server.di.NetworkComponent
import ca.derekellis.api.server.di.ServerComponent
import ca.derekellis.api.server.di.create
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.types.path
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import java.nio.file.Path

class MainCommand : CliktCommand() {
  private val dataPath: Path? by argument().path(canBeFile = false, mustExist = true).optional()

  override fun run() {
    val component = ServerComponent::class.create(dataPath, NetworkComponent::class.create())
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
}

fun main(args: Array<String>) {
  MainCommand().main(args)
}
