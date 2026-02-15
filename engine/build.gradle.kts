plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.tapmoc)
}

// Generate a Version.kt file with a constant for the version name
val generateVersionKtTask: TaskProvider<Task> = tasks.register("generateVersionKt") {
  val outputDir = layout.buildDirectory.dir("generated/source/kotlin").get().asFile
  outputs.dir(outputDir)
  val version = rootProject.version
  doFirst {
    val outputWithPackageDir = File(outputDir, "org/jraf/hop/engine").apply { mkdirs() }
    File(outputWithPackageDir, "Version.kt").writeText(
      """
        package org.jraf.hop.engine
        const val VERSION = "v$version"
      """.trimIndent()
    )
  }
}

kotlin {
  jvm()

  sourceSets {
    commonMain {
      kotlin.srcDir(generateVersionKtTask)

      dependencies {
        implementation(libs.kotlinx.coroutines.core)
      }
    }

    jvmMain {
      dependencies {
        implementation(libs.androidx.datastore)
        implementation(libs.androidx.datastore.preferences)
      }
    }
  }
}

tapmoc {
  java(17)
  kotlin("2.3.10")
  checkDependencies()
}
