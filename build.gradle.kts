val apiVersion = findProperty("version.api") ?: "invalid"
rootProject.ext.set("apiVersion", apiVersion)

val mavenDeployUsername = System.getenv("MAVEN_DEPLOY_USR") ?: findProperty("mavenUsernameSirBlobman") ?: ""
rootProject.ext.set("mavenUsername", mavenDeployUsername)

val mavenDeployPassword = System.getenv("MAVEN_DEPLOY_PSW") ?: findProperty("mavenPasswordSirBlobman") ?: ""
rootProject.ext.set("mavenPassword", mavenDeployPassword)

val baseVersion = findProperty("version.base") ?: "invalid"
val betaString = ((findProperty("version.beta") ?: "false") as String)
val jenkinsBuildNumber = System.getenv("BUILD_NUMBER") ?: "Unofficial"

val betaBoolean = betaString.toBoolean()
val betaVersion = if (betaBoolean) "Beta-" else ""
val calculatedVersion = "$baseVersion.$betaVersion$jenkinsBuildNumber"
rootProject.ext.set("pluginVersion", calculatedVersion)
