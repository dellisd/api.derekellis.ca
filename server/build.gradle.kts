import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
import kotlin.requireNotNull

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.ksp)
  alias(libs.plugins.shadow)
  alias(libs.plugins.buildconfig)
  application
}

group = "ca.derekellis.api"

repositories {
  google()
  mavenCentral()
  maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
  maven(url = "https://repo.kotlin.link")
}

dependencies {
  implementation(libs.inject.runtime)
  implementation(libs.logback)
  implementation(libs.bundles.ktor.server)
  implementation(libs.bundles.ktor.client)
  implementation(libs.clikt)
  implementation(libs.kgtfs.gtfs)

  ksp(libs.inject.compiler)

  implementation(libs.kgtfs.gtfs)

  implementation(project(":shared"))
}

kotlin {
  sourceSets {
    getByName("main") {
      kotlin.srcDir("$buildDir/generated/ksp/main/kotlin")
    }
  }
}

tasks.withType<KotlinCompile> {
  kotlinOptions.freeCompilerArgs += "-Xcontext-receivers"
}

application {
  mainClass.set("ca.derekellis.api.server.MainKt")
}

buildConfig {
  val props = file("$rootDir/local.properties").takeIf { it.exists() }?.let { Properties().apply { load(it.reader()) } }
  if(props == null) {
    logger.warn("local.properties file not found")
    return@buildConfig
  }

  className("ServerConfig")
  buildConfigField("String", "LAMBDA_ENDPOINT", "\"${props.getProperty("LAMBDA_ENDPOINT", "")}\"")
}
