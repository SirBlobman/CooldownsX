plugins {
    id("java")
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://nexus.sirblobman.xyz/public/")
}

dependencies {
    // Local Dependencies
    compileOnly(project(":api"))

    // Java Dependencies
    compileOnly("org.jetbrains:annotations:24.0.1") // JetBrains Annotations
    compileOnly("org.spigotmc:spigot-api:1.14.4-R0.1-SNAPSHOT") // Spigot API

    // Plugin Dependencies
    compileOnly("com.github.sirblobman.api:core:2.8-SNAPSHOT") // BlueSlimeCore
}
