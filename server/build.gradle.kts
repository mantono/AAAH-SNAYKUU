fun version(artifact: String): String {
    val key = "version.${artifact.toLowerCase()}"
    return project.ext[key]?.toString()
        ?: throw IllegalStateException("No version found for artifact '$artifact'")
}

fun projectName(): String = project.name.replace("{", "").replace("}", "")

plugins {
    id("application") apply true
    id("org.jetbrains.kotlin.jvm") version "1.3.50" apply true
    id("java") apply true
}

application {
    mainClassName = "snaykuu.Main"
}

group = "snaykuu"
version = "0.1.0"
description = "An engine/API for programming Snake bots and pitting them against each other"

defaultTasks = mutableListOf("test")

repositories {
    jcenter()
    mavenCentral()
    maven(url = "https://dl.bintray.com/kotlin/ktor")
}

dependencies {
    implementation("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", "1.3.61")
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", "1.3.3")

    // Logging
    implementation("io.github.microutils", "kotlin-logging", "1.6.20")
    // Enable for applications
    // runtime("ch.qos.logback", "logback-classic", "1.2.3")

    // Junit
    testImplementation("org.junit.jupiter", "junit-jupiter-api", version("junit"))
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", version("junit"))
}

tasks {
    test {
        useJUnitPlatform()

        // Show test results.
        testLogging {
            events("passed", "skipped", "failed")
        }
        reports {
            junitXml.isEnabled = false
            html.isEnabled = true
        }
    }

    compileKotlin {
        sourceCompatibility = version("jvm")
        kotlinOptions {
            jvmTarget = version("jvm")
        }
    }

    wrapper {
        description = "Generates gradlew[.bat] scripts for faster execution"
        gradleVersion = version("gradle")
    }
}
