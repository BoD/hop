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
        api(project(":action-api"))
        implementation(project(":action-util"))
        implementation(libs.klibnanolog)
        implementation(libs.kotlinx.io.core)
        implementation(project(":macOSBridge"))
      }
    }

    jvmMain {
      dependencies {
        implementation(libs.jna)
      }
    }
  }
}

tapmoc {
  java(17)
  kotlin("2.3.20")
  checkDependencies()
}
