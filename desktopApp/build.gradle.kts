import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.compose.hotReload)
  alias(libs.plugins.tapmoc)
}

kotlin {
  jvm("desktop")

  sourceSets {
    val desktopMain by getting

    desktopMain.apply {
      dependencies {
        implementation(compose.desktop.currentOs)
        implementation(libs.kotlinx.coroutines.swing)

        implementation("com.github.tulskiy:jkeymaster:1.3")
        implementation("net.java.dev.jna:jna:5.18.1")


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

tapmoc {
  java(17)
  kotlin("2.3.10")
  checkDependencies()
}
