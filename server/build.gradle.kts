import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.ksp)
  alias(libs.plugins.shadow)
  application
}

group = "ca.derekellis.api"

repositories {
  google()
  mavenCentral()
}

dependencies {
  implementation(libs.ktor.server.core)
  implementation(libs.ktor.server.netty)
  implementation(libs.inject.runtime)
  implementation(libs.logback)

  ksp(libs.inject.compiler)
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
