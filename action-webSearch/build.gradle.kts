plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.tapmoc)
}

kotlin {
  jvm()
  macosArm64()

  sourceSets {
    commonMain {
      dependencies {
        api(project(":action-api"))
        implementation(libs.compose.components.resources)
        implementation(libs.klibnanolog)
        implementation(libs.ktor.client.core)
        implementation(libs.ktor.client.cio)
      }
    }
  }
}

tapmoc {
  java(17)
  kotlin("2.3.10")
  checkDependencies()
}
