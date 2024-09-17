import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    application
    idea
    kotlin("jvm") version "2.0.10"
    kotlin("plugin.serialization") version "2.0.10"
}

group = "org.sdpi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // regex pattern
    implementation(kotlin("stdlib"))

    // command line parsing
    // https://mvnrepository.com/artifact/com.github.ajalt/clikt
    implementation(group = "com.github.ajalt", name = "clikt", version = "2.8.0")

    // asciidoc conversion
    // https://mvnrepository.com/artifact/org.asciidoctor/asciidoctorj
    implementation("org.asciidoctor:asciidoctorj:2.5.4")
    implementation("org.asciidoctor:asciidoctorj-pdf:2.1.4")
    implementation("org.asciidoctor:asciidoctorj-diagram:2.2.3")
    implementation("org.asciidoctor:asciidoctorj-diagram-plantuml:1.2022.5")

    implementation("org.jsoup:jsoup:1.15.3")

    // logging
    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-core
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    // https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-api-kotlin
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.0.0")

    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = "5.7.1")

    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-engine
    runtimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = "5.7.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    // https://mvnrepository.com/artifact/org.kohsuke/github-api
    implementation("org.kohsuke:github-api:1.315")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

val jvmVersion = JvmTarget.JVM_17

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(jvmVersion.target)
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

application {
    mainClass.set("org.sdpi.ConvertAndVerifySupplementKt")
}