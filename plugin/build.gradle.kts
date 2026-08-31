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
    implementation(project(":api")) // CooldownsX API
    compileOnly("com.github.sirblobman.combatlogx:api:11.7-SNAPSHOT") // CombatLogX
    compileOnly("me.clip:placeholderapi:2.12.3") // PlaceholderAPI
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

    named<ProcessResources>("processResources") {
        val pluginVersion = providers.provider { project.version.toString() }
        inputs.property("version", pluginVersion)

        filesMatching("plugin.yml") {
            expand(mapOf("version" to pluginVersion.get()))
        }
    }
}
