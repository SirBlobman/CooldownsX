val apiVersion = fetchProperty("version.api", "invalid")
val coreVersion = fetchProperty("version.core", "invalid")
val mavenUsername = fetchEnv("MAVEN_DEPLOY_USR", "mavenUsernameSirBlobman", "")
val mavenPassword = fetchEnv("MAVEN_DEPLOY_PSW", "mavenPasswordSirBlobman", "")

rootProject.ext.set("apiVersion", apiVersion)
rootProject.ext.set("coreVersion", coreVersion)
rootProject.ext.set("mavenUsername", mavenUsername)
rootProject.ext.set("mavenPassword", mavenPassword)

val baseVersion = fetchProperty("version.base", "invalid")
val betaString = fetchProperty("version.beta", "false")
val jenkinsBuildNumber = fetchEnv("BUILD_NUMBER", null, "Unofficial")

val betaBoolean = betaString.toBoolean()
val betaVersion = if (betaBoolean) "Beta-" else ""
val calculatedVersion = "$baseVersion.$betaVersion$jenkinsBuildNumber"
rootProject.ext.set("pluginVersion", calculatedVersion)

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

allprojects {
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
