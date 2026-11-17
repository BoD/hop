plugins {
  alias(libs.plugins.compose.hotReload).apply(false)
  alias(libs.plugins.compose.multiplatform).apply(false)
  alias(libs.plugins.compose.compiler).apply(false)
  alias(libs.plugins.kotlin.multiplatform).apply(false)
  alias(libs.plugins.kotlin.serialization).apply(false)
  alias(libs.plugins.sqldelight).apply(false)
  alias(libs.plugins.tapmoc).apply(false)
}

// Compose will use the group name as the package name where to generate the Res class.
allprojects {
  group = "org.jraf.hop"
  version = "1.0.0"
}


// `./gradlew refreshVersions` to update dependencies
// `./gradlew :desktopApp:run` to run the application
// `./gradlew :desktopApp:createDistributable` to build the application (results in `desktopApp/build/compose/binaries/main/app/org.jraf.hop.app`)
// `./gradlew :desktopApp:packageDistributionForCurrentOS` to package the application (results in `desktopApp/build/compose/binaries/main/dmg/org.jraf.hop-1.0.0.dmg`)
