plugins {
  alias(libs.plugins.compose.hotReload).apply(false)
  alias(libs.plugins.compose.multiplatform).apply(false)
  alias(libs.plugins.compose.compiler).apply(false)
  alias(libs.plugins.kotlin.multiplatform).apply(false)
  alias(libs.plugins.kotlin.serialization).apply(false)
  alias(libs.plugins.tapmoc).apply(false)
}

// Compose will use the group name as the package name where to generate the Res class.
allprojects {
  group = "org.jraf.hop"
  version = "1.0.0"
}


// `./gradlew refreshVersions` to update dependencies
// `./gradlew installDesktopDist` to install the CLI application (results in `desktopApp/build/install/desktopApp-desktop/bin`)
// `./gradlew desktopDistZip` to create a ZIP file containing the CLI application (results in `desktopApp/build/distributions/desktopApp-<version>-desktop.zip`)
