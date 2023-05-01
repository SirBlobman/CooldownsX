plugins {
    id("java")
    id("maven-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    withSourcesJar()
    withJavadocJar()
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
    compileOnly("com.github.sirblobman.api:core:2.9-SNAPSHOT")
}

publishing {
    repositories {
        maven("https://nexus.sirblobman.xyz/public/") {
            credentials {
                username = rootProject.ext.get("mavenUsername") as String
                password = rootProject.ext.get("mavenPassword") as String
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.sirblobman.plugin.cooldowns"
            artifactId = "cooldowns-api"
            version = rootProject.ext.get("apiVersion") as String
            from(components["java"])
        }
    }
}

tasks.withType<Javadoc> {
    val standardOptions = options as StandardJavadocDocletOptions
    standardOptions.addStringOption("Xdoclint:none", "-quiet")
}
