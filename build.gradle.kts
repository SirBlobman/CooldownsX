val baseVersion = fetchProperty("version.base", "invalid")
val betaString = fetchProperty("version.beta", "false")
val jenkinsBuildNumber = fetchEnv("BUILD_NUMBER", null, "Unofficial")

val betaBoolean = betaString.toBoolean()
val betaVersion = if (betaBoolean) "Beta-" else ""
version = "$baseVersion.$betaVersion$jenkinsBuildNumber"

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
    id("java")
}

tasks.named("jar") {
    enabled = false
}

subprojects {
    apply(plugin = "java")
    version = rootProject.version

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    repositories {
        mavenCentral() // Maven Central
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // SpigotMC Repository
        maven("https://oss.sonatype.org/content/repositories/snapshots/") // Sonatype OSS
        maven("https://nexus.sirblobman.xyz/public/") // SirBlobman Public
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:26.0.2") // JetBrains Annotations
        compileOnly("org.spigotmc:spigot-api:1.21.7-R0.1-SNAPSHOT") // Spigot API
        compileOnly("com.github.sirblobman.api:core:2.9-SNAPSHOT") // BlueSlimeCore
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.compilerArgs.add("-Xlint:deprecation")
            options.compilerArgs.add("-Xlint:unchecked")
        }

        withType<Javadoc> {
            options.encoding = "UTF-8"
            val standardOptions = options as StandardJavadocDocletOptions
            standardOptions.addStringOption("Xdoclint:none", "-quiet")
        }
    }
}
