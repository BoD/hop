plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.compose.hotReload)
  alias(libs.plugins.tapmoc)
}

kotlin {
  jvm()
  macosArm64()

  sourceSets {
    commonMain {
      dependencies {
        api(libs.compose.runtime)
        api(libs.compose.foundation)
        api(libs.compose.material3)
        api(libs.compose.ui)
        implementation(libs.compose.components.resources)
        implementation(libs.compose.ui.toolingPreview)

        api(project(":engine"))
      }
    }
  }
}

compose.resources {
  publicResClass = true
}

tapmoc {
  java(17)
  kotlin("2.3.10")
  checkDependencies()
}
