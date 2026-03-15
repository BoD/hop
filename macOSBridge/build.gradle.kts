plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.tapmoc)
}

val generatedJvmResourcesDir = layout.buildDirectory.dir("generated/resources/jvmMain")
val dylibOutputFile = generatedJvmResourcesDir.map { it.file("darwin-aarch64/libMacOSBridge.dylib") }

val compileDylib: TaskProvider<Exec> by tasks.registering(Exec::class) {
  val sourceFile = layout.projectDirectory.file("src/objective-c/MacOSBridge.m")
  val outputFile = dylibOutputFile.get().asFile

  inputs.file(sourceFile)
  outputs.file(outputFile)

  doFirst {
    outputFile.parentFile.mkdirs()
  }

  commandLine(
    "clang",
    "-dynamiclib",
    "-framework", "AppKit",
    "-framework", "CoreServices",
    "-o", outputFile.absolutePath,
    sourceFile.asFile.absolutePath
  )
}

kotlin {
  jvm()
  macosArm64()

  sourceSets {
    commonMain {
      dependencies {
        api(libs.compose.ui)
        api(libs.kotlinx.io.core)
        implementation(libs.klibnanolog)
      }
    }

    jvmMain {
      dependencies {
        implementation(libs.jna)
      }
    }
  }
}

// Package the dylib as a JVM resource.
tasks.named<ProcessResources>("jvmProcessResources") {
  dependsOn(compileDylib)
  from(generatedJvmResourcesDir)
}

tapmoc {
  java(17)
  kotlin("2.3.10")
  checkDependencies()
}
