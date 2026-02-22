import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.compose.hotReload)
  alias(libs.plugins.tapmoc)
}

kotlin {
  jvm("desktop") {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    binaries {
      executable {
        mainClass.set("org.jraf.hop.desktopapp.MainKt")
      }
    }
  }

  sourceSets {
    val desktopMain by getting

    desktopMain.apply {
      dependencies {
        implementation(compose.desktop.currentOs)
        implementation(libs.compose.components.resources)
        implementation(libs.kotlinx.coroutines.swing)
        implementation(libs.jkeymaster)
        implementation(libs.jna)

        implementation(project(":action-app"))
        implementation(project(":action-bookmark"))
        implementation(project(":action-url"))
        implementation(project(":action-webSearch"))
        implementation(project(":ui"))
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = "org.jraf.hop.ui.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "org.jraf.hop"
      packageVersion = rootProject.version.toString()

      macOS {
        infoPlist {
          extraKeysRawXml = """
            <key>LSUIElement</key>
            <string>true</string>
          """.trimIndent()
        }
      }
    }
  }
}

compose.resources {
  generateResClass = always
}

tapmoc {
  java(17)
  kotlin("2.3.10")
  checkDependencies()
}
