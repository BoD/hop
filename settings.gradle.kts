@file:Suppress("UnstableApiUsage")

rootProject.name = "hop"

pluginManagement {
  repositories {
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
  }
}

plugins {
  // See https://splitties.github.io/refreshVersions
  id("de.fayard.refreshVersions") version "0.60.6"
}

include(
  ":macOSBridge",
  ":action-api",
  ":action-util",
  ":action-app",
  ":action-bookmark",
  ":action-url",
  ":action-webSearch",
  ":action-calculator",
  ":action-wikipedia",
  ":engine",
  ":ui",
  ":desktopApp",
)
