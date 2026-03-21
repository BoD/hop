plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.tapmoc)
}

kotlin {
  jvm()
  macosArm64()

  sourceSets {
    commonMain {
      dependencies {
        api(project(":action-api"))
        implementation(project(":action-util"))
        implementation(libs.kotlinx.serialization.json)
        implementation(libs.ktor.client.contentNegotiation)
        implementation(libs.ktor.client.logging)
        implementation(libs.ktor.serialization.kotlinx.json)
        implementation(libs.klibnanolog)
      }
    }
  }
}

tapmoc {
  java(17)
  kotlin("2.3.20")
  checkDependencies()
}
