import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val baseVersion = findProperty("version.base") ?: "invalid"
val betaString = ((findProperty("version.beta") ?: "false") as String)
val jenkinsBuildNumber = System.getenv("BUILD_NUMBER") ?: "Unofficial"

val betaBoolean = betaString.toBoolean()
val betaVersion = if (betaBoolean) "Beta-" else ""
val calculatedVersion = "$baseVersion.$betaVersion$jenkinsBuildNumber"

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.0"
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://nexus.sirblobman.xyz/repository/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    implementation(project(":api"))
    implementation(project(":modern"))

    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("org.spigotmc:spigot-api:1.19.3-R0.1-SNAPSHOT")
    compileOnly("com.github.sirblobman.api:core:2.7-SNAPSHOT")
    compileOnly("com.github.sirblobman.combatlogx:api:11.2-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}


tasks {
    named<Jar>("jar") {
        enabled = false
    }

    named<ShadowJar>("shadowJar") {
        archiveClassifier.set(null as String?)
        archiveFileName.set("CooldownsX-$calculatedVersion.jar")
    }

    build {
        dependsOn(shadowJar)
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
                "pluginVersion" to calculatedVersion
            ))
        }

        filesMatching("config.yml") {
            expand(mapOf("pluginVersion" to calculatedVersion))
        }
    }
}
