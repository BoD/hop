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
