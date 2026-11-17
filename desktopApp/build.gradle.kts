import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.compose.hotReload)
  alias(libs.plugins.tapmoc)
}

kotlin {
  jvm("desktop") {
  }

  sourceSets {
    val desktopMain = getByName("desktopMain")

    desktopMain.apply {
      dependencies {
        implementation(libs.compose.ui)
        implementation(libs.compose.components.resources)
        implementation(libs.kotlinx.coroutines.swing)
        implementation(libs.jkeymaster)
        implementation(libs.jna)
        implementation(libs.logback)

        implementation(project(":macOSBridge"))
        implementation(project(":action-app"))
        implementation(project(":action-bookmark"))
        implementation(project(":action-browserHistory"))
        implementation(project(":action-url"))
        implementation(project(":action-webSearch"))
        implementation(project(":action-calculator"))
        implementation(project(":action-wikipedia"))
        implementation(project(":ui"))
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = "org.jraf.hop.desktopapp.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "hop"
      packageVersion = rootProject.version.toString()
      vendor = "Benoit 'BoD' Lubek"
      copyright = "Copyright (C) 2026-present Benoit 'BoD' Lubek (BoD@JRAF.org)"
      licenseFile.set(rootProject.file("COPYING"))

      // Include `java.sql` in the bundled Java runtime, needed for SQLDelight
      modules("java.sql")

      macOS {
        iconFile.set(rootProject.file("etc/logo/hop.icns"))

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
  kotlin("2.3.20")
  checkDependencies()
}
