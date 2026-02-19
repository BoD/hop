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
        api(libs.kotlinx.coroutines.core)
        api(libs.compose.ui)
        api(libs.compose.components.resources)
      }
    }
  }
}

tapmoc {
  java(17)
  kotlin("2.3.10")
  checkDependencies()
}
