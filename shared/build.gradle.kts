plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

repositories {
  google()
  mavenCentral()
  maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
  implementation(libs.kgtfs.gtfs)
  implementation(libs.kgtfs.raptor)

  implementation(libs.kotlinx.serialization.json)
}
