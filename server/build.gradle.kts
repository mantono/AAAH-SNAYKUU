fun version(artifact: String): String {
    val key = "version.${artifact.toLowerCase()}"
    return project.ext[key]?.toString()
        ?: throw IllegalStateException("No version found for artifact '$artifact'")
}

fun projectName(): String = project.name.replace("{", "").replace("}", "")

plugins {
    id("application") apply true
    kotlin("jvm") version "1.3.72"
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
    flatDir {
        dirs("bot")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk9", "1.3.7")

    // Logging
    implementation("io.github.microutils", "kotlin-logging", "1.6.20")
    runtimeOnly("ch.qos.logback", "logback-classic", "1.2.3")

    // Serialization, Saving and Replays
    implementation("com.fasterxml.jackson.core", "jackson-core", "2.11.0")
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.11.0")

    // Bots & Reflection
    compileOnly(fileTree("bots"))
    implementation("org.reflections", "reflections", "0.9.12")

    // Junit/Testin
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

    //register<Build_gradle.CopyAndCompileBots>("bots")
}

sourceSets.main {
    val customBotsPath: String = System.getenv().getOrDefault("BOTS_PATH", "bot")
    java.srcDirs("src/main/java", "src/main/kotlin", "bot", customBotsPath)
}
//
//open class CopyAndCompileBots: DefaultTask() {
//    @org.gradle.api.tasks.TaskAction
//    fun execute() {
//        val botsDir: String = project.projectDir.absolutePath + "/bot"
//        println(File("src/main/java/bots").mkdirs())
//        println(File("src/main/kotlin/bots").mkdirs())
//        println(botsDir)
//        File(botsDir).listFiles()
//            .asSequence()
//            .onEach { println(it.absolutePath) }
//            .filter { it.extension == "java" || it.extension == "kt" }
//            .filter { it.canRead() }
//            .filter { it.length() > 0 }
//            .map { it to destination(it) }
//            .onEach { println("Copying ${it.first} to ${it.second}") }
//            .forEach { (from: File, to: File) -> from.copyTo(to, overwrite = true) }
//    }
//
//    private inline fun destination(file: File): File {
//        val fileName: String = File.separator + file.name
//        return when(file.extension) {
//            "kt" -> File(KOTLIN_DESTINATION + fileName)
//            "java" -> File(JAVA_DESTINATION + fileName)
//            else -> error("Not supported extension: '${file.extension}'")
//        }
//    }
//
//    companion object {
//        private const val KOTLIN_DESTINATION = "src/main/kotlin/bots"
//        private const val JAVA_DESTINATION = "src/main/java/bots"
//    }
//}