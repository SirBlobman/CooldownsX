val coreVersion = fetchProperty("version.core", "invalid")
rootProject.ext.set("coreVersion", coreVersion)

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    repositories {
        mavenCentral()
        maven("https://nexus.sirblobman.xyz/public/")
    }

    dependencies {
        compileOnly("org.jetbrains:annotations:26.0.1")
        compileOnly("com.github.sirblobman.api:core:$coreVersion")
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
