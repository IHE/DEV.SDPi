import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    application
}

group = "org.sdpi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    // regex pattern
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.7.10")

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

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("org.sdpi.ConvertAndVerifySupplementKt")
}