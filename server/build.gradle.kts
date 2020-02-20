apply plugin: 'java'
apply plugin: 'kotlin'
apply plugin: 'maven'

group = 'xanindorf'
version = '1.0'
description = 'Snaykuu'

defaultTasks 'run'

task run(type: JavaExec) {
    main = 'Main'
    classpath = sourceSets.main.runtimeClasspath
}

buildscript {
    ext.kotlin_version = '1.2.41'
    ext.jvm_version = '1.8'

    ext.jarName = 'snaykuu.jar'
    ext.bin = 'classes'
    ext.src = 'src'
    ext.bot = 'bot'
    ext.img = 'img'
    ext.doc = 'doc'
    ext.encoding = 'UTF-8'

    repositories {
        mavenCentral()
        maven { url "https://plugins.gradle.org/m2/" }
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

dependencies {
    runtime "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
    runtime "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
}

repositories {
    maven { url "http://jcenter.bintray.com" }
}

compileJava {
    sourceCompatibility = jvm_version
    targetCompatibility = jvm_version
    options.incremental = true
    options.encoding = encoding
}

sourceSets {
    main.java.srcDirs += src
    main.resources.srcDirs += 'resources'
}

jar {
    archiveName jarName
    destinationDir = file('.')
    from(img) {
        into(img)
    }
    manifest {
        attributes 'Main-Class': 'Main'
    }
}

task sourcesJar(type: Jar, dependsOn: classes) {
    description 'Create a Jar file for the application, including source'
    classifier = 'sources'
    from sourceSets.main.allSource
}

task javadocJar(type: Jar, dependsOn: javadoc) {
    classifier = 'javadoc'
    javadoc.failOnError = false
    from javadoc.destinationDir
}

artifacts {
    archives sourcesJar
    archives javadocJar
}

wrapper {
    description = 'Generates gradlew[.bat] scripts for faster execution'
    gradleVersion = '4.7'
}
