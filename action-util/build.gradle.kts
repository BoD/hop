plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.tapmoc)
}

kotlin {
  jvm()
  macosArm64()

  sourceSets {
    commonMain {
      dependencies {
        api(libs.kotlinx.io.core)
        api(libs.compose.ui)
        api(libs.ktor.client.core)
        implementation(libs.ktor.client.cio)
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
