import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val pluginVersion = rootProject.ext.get("pluginVersion") as String

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://nexus.sirblobman.xyz/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    // Local Dependencies
    implementation(project(":api"))
    implementation(project(":modern"))

    // Java Dependencies
    compileOnly("org.jetbrains:annotations:24.0.1") // JetBrains Annotations
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT") // Spigot API

    // Plugin Dependencies
    compileOnly("com.github.sirblobman.api:core:2.9-dev-SNAPSHOT") // BlueSlimeCore
    compileOnly("com.github.sirblobman.combatlogx:api:11.3-SNAPSHOT") // CombatLogX
    compileOnly("me.clip:placeholderapi:2.11.3") // PlaceholderAPI
}


tasks {
    named<Jar>("jar") {
        enabled = false
    }

    named<ShadowJar>("shadowJar") {
        archiveClassifier.set(null as String?)
        archiveFileName.set("CooldownsX-$pluginVersion.jar")
    }

    named("build") {
        dependsOn("shadowJar")
    }

    processResources {
        val pluginName = (findProperty("bukkit.plugin.name") ?: "") as String
        val pluginPrefix = (findProperty("bukkit.plugin.prefix") ?: "") as String
        val pluginDescription = (findProperty("bukkit.plugin.description") ?: "") as String
        val pluginWebsite = (findProperty("bukkit.plugin.website") ?: "") as String
        val pluginMainClass = (findProperty("bukkit.plugin.main") ?: "") as String

        filesMatching("plugin.yml") {
            expand(mapOf(
                "pluginName" to pluginName,
                "pluginPrefix" to pluginPrefix,
                "pluginDescription" to pluginDescription,
                "pluginWebsite" to pluginWebsite,
                "pluginMainClass" to pluginMainClass,
                "pluginVersion" to pluginVersion
            ))
        }

        filesMatching("config.yml") {
            expand(mapOf("pluginVersion" to pluginVersion))
        }
    }
}
