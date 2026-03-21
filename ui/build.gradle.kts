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
        implementation(libs.materialKolor)
        implementation(libs.coil.compose)
        implementation(libs.coil.ktor3)
        api(project(":engine"))
      }
    }

    jvmMain {
      dependencies {
        implementation(libs.nucleus.coreRuntime)
        implementation(libs.nucleus.systemColor)
      }
    }
  }
}

compose.resources {
  publicResClass = true
  packageOfResClass = "org.jraf.hop.ui.generated.resources"
}

tapmoc {
  java(17)
  kotlin("2.3.20")
  checkDependencies()
}
