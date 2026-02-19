plugins {
  alias(libs.plugins.compose.hotReload).apply(false)
  alias(libs.plugins.compose.multiplatform).apply(false)
  alias(libs.plugins.compose.compiler).apply(false)
  alias(libs.plugins.kotlin.multiplatform).apply(false)
  alias(libs.plugins.kotlin.serialization).apply(false)
  alias(libs.plugins.tapmoc).apply(false)
}

group = "org.jraf.hop"
version = "1.0.0"
