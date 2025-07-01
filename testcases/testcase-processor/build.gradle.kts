plugins {
    java
    application
    idea
    kotlin("jvm") version "2.0.10"
    kotlin("plugin.serialization") version "2.1.20"
}

group = "org.sdpi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}