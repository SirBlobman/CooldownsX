import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

fun fetchProperty(propertyName: String, defaultValue: String): String {
    val found = findProperty(propertyName)
    if (found != null) {
        return found.toString()
    }

    return defaultValue
}

plugins {
    id("com.gradleup.shadow") version "8.3.5"
}

repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.helpch.at/releases/")
}

dependencies {
    // Local Dependencies
    implementation(project(":api"))
    implementation(project(":modern"))

    // Spigot API
    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")

    // Plugin Dependencies
    compileOnly("com.github.sirblobman.combatlogx:api:11.6-SNAPSHOT") // CombatLogX
    compileOnly("me.clip:placeholderapi:2.11.6") // PlaceholderAPI
}


tasks {
    named<Jar>("jar") {
        enabled = false
    }

    named<ShadowJar>("shadowJar") {
        archiveClassifier.set(null as String?)
        archiveBaseName.set("CooldownsX")
    }

    named("build") {
        dependsOn("shadowJar")
    }

    processResources {
        val pluginName = fetchProperty("bukkit.plugin.name", "")
        val pluginPrefix = fetchProperty("bukkit.plugin.prefix", "")
        val pluginDescription = fetchProperty("bukkit.plugin.description", "")
        val pluginWebsite = fetchProperty("bukkit.plugin.website", "")
        val pluginMainClass = fetchProperty("bukkit.plugin.main", "")

        filesMatching("plugin.yml") {
            expand(
                mapOf(
                    "pluginName" to pluginName,
                    "pluginPrefix" to pluginPrefix,
                    "pluginDescription" to pluginDescription,
                    "pluginWebsite" to pluginWebsite,
                    "pluginMainClass" to pluginMainClass,
                    "pluginVersion" to version
                )
            )
        }

        filesMatching("config.yml") {
            expand(mapOf("pluginVersion" to version))
        }
    }
}
