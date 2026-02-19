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
        implementation(libs.klibnanolog)
        implementation(libs.kotlinx.io.core)

        // TODO version catalog
        implementation("com.getiox.plist:plist:0.0.2")
      }
    }

    jvmMain {
      dependencies {
        // TODO version catalog
        implementation("com.github.gino0631:icns-core:1.2")
      }
    }
  }
}

tapmoc {
  java(17)
  kotlin("2.3.10")
  checkDependencies()
}
