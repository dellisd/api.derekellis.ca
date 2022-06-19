plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

group = "ca.derekellis.api"

repositories {
  google()
  mavenCentral()
  maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
  implementation(libs.aws)
  implementation(libs.kgtfs.gtfs)
  implementation(libs.kgtfs.raptor)
  implementation(libs.logback)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.serialization.json)
}

tasks.create<Zip>("buildZip") {
  from(tasks.getByName("compileKotlin"))
  from(tasks.getByName("processResources"))

  into("lib") {
    from(configurations.runtimeClasspath)
  }
}
