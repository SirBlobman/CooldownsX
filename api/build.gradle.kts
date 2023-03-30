plugins {
    id("java")
    id("maven-publish")
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://nexus.sirblobman.xyz/public/")
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    compileOnly("com.github.sirblobman.api:core:2.8-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
    withJavadocJar()
}

publishing {
    repositories {
        maven {
            url = uri("https://nexus.sirblobman.xyz/public/")

            credentials {
                var currentUsername = System.getenv("MAVEN_DEPLOY_USR")
                if (currentUsername == null) {
                    currentUsername = property("mavenUsernameSirBlobman") as String
                }

                var currentPassword = System.getenv("MAVEN_DEPLOY_PSW")
                if (currentPassword == null) {
                    currentPassword = property("mavenPasswordSirBlobman") as String
                }

                username = currentUsername
                password = currentPassword
            }
        }
    }

    publications {
        group = "com.github.sirblobman.plugin.cooldowns"
        version = "5.0.0-SNAPSHOT"

        create<MavenPublication>("maven") {
            artifactId = "cooldowns-api"
            from(components["java"])
        }
    }
}

tasks {
    javadoc {
        val standardOptions = options as StandardJavadocDocletOptions
        standardOptions.addStringOption("Xdoclint:none", "-quiet")
    }
}
