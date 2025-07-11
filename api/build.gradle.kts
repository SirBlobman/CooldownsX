val mavenUsername = fetchEnv("MAVEN_DEPLOY_USR", "mavenUsernameSirBlobman", "")
val mavenPassword = fetchEnv("MAVEN_DEPLOY_PSW", "mavenPasswordSirBlobman", "")
version = fetchProperty("version.api", "invalid")

fun fetchProperty(propertyName: String, defaultValue: String): String {
    val found = findProperty(propertyName)
    if (found != null) {
        return found.toString()
    }

    return defaultValue
}

fun fetchEnv(envName: String, propertyName: String?, defaultValue: String): String {
    val found = System.getenv(envName)
    if (found != null) {
        return found
    }

    if (propertyName != null) {
        return fetchProperty(propertyName, defaultValue)
    }

    return defaultValue
}

plugins {
    id("maven-publish")
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    repositories {
        maven("https://nexus.sirblobman.xyz/public/") {
            credentials {
                username = mavenUsername
                password = mavenPassword
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = "com.github.sirblobman.plugin.cooldowns"
            artifactId = "cooldowns-api"
            from(components["java"])
        }
    }
}
