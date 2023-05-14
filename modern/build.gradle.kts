plugins {
    id("java")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

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

    // Spigot API
    compileOnly("org.spigotmc:spigot-api:1.14.4-R0.1-SNAPSHOT")

    // BlueSlimeCore
    val coreVersion = rootProject.ext.get("coreVersion")
    compileOnly("com.github.sirblobman.api:core:$coreVersion")
}
