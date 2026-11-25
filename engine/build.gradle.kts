plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.sqldelight)
  alias(libs.plugins.tapmoc)
}

val generateVersionKt: TaskProvider<Task> = tasks.register("generateVersionKt") {
  description = "Generates Version.kt"
  val outputDir = layout.buildDirectory.dir("generated/source/kotlin").get().asFile
  outputs.dir(outputDir)
  val versionProvider = providers.provider { rootProject.version.toString() }
  inputs.property("version", versionProvider)
  doFirst {
    val outputWithPackageDir = File(outputDir, "org/jraf/hop/engine").apply { mkdirs() }
    File(outputWithPackageDir, "Version.kt").writeText(
      """
        package org.jraf.hop.engine
        const val VERSION = "v${versionProvider.get()}"
      """.trimIndent()
    )
  }
}

kotlin {
  jvm()
  macosArm64()

  sourceSets {
    commonMain {
      kotlin.srcDir(generateVersionKt)

      dependencies {
        api(libs.kotlinx.coroutines.core)
        api(project(":action-api"))

        implementation(libs.sqldelight.runtime)
        implementation(libs.sqldelight.coroutines)
      }
    }

    jvmMain {
      dependencies {
        implementation(libs.sqldelight.sqlite)
      }
    }
  }
}

sqldelight {
  databases {
    register("HopDatabase") {
      srcDirs("src/commonMain/sqldelight/hop")
      packageName.set("org.jraf.hop.engine.db.hop")
      schemaOutputDirectory.set(file("src/commonMain/sqldelight/hop/databases"))
    }
  }
}

tapmoc {
  java(17)
  kotlin("2.3.20")
  checkDependencies()
}

// `./gradlew generateCommonMainHopDatabaseSchema` to generate the database schema (results in `src/commonMain/sqldelight/databases/hop`)
