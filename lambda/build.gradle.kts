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
  implementation(project(":shared"))
}

tasks.create<Copy>("unzipData") {
  val zipFile = file("$projectDir/assets/data.zip")

  from(zipTree(zipFile))
  into(file("$projectDir/src/main/resources"))
}

tasks.create<Zip>("buildZip") {
  from(tasks.getByName("compileKotlin"))
  from(tasks.getByName("processResources"))

  into("lib") {
    from(configurations.runtimeClasspath)
  }

  dependsOn("unzipData")
}

tasks.getByName("processResources").dependsOn("unzipData")
