pipeline {
    agent any

    options {
        githubProjectProperty(projectUrlStr: "https://github.com/SirBlobman/CooldownsX")
    }

    environment {
        DISCORD_URL = credentials('PUBLIC_DISCORD_WEBHOOK')
        MAVEN_DEPLOY = credentials('MAVEN_DEPLOY')
    }

    triggers {
        githubPush()
    }

    tools {
        jdk "JDK 21"
    }

    stages {
        stage ("Gradle: Publish") {
            steps {
                withGradle {
                    script {
                        sh("./gradlew --refresh-dependencies clean build")
                        if (env.BRANCH_NAME == "main") {
                            sh("./gradlew publish")
                        }
                        sh("./gradlew --stop")
                    }
                }
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'plugin/build/libs/CooldownsX-*.jar', fingerprint: true
        }

        always {
            script {
                discordSend webhookURL: DISCORD_URL, title: "CooldownsX", link: "${env.BUILD_URL}",
                        result: currentBuild.currentResult,
                        description: """\
                            **Branch:** ${env.GIT_BRANCH}
                            **Build:** ${env.BUILD_NUMBER}
                            **Status:** ${currentBuild.currentResult}""".stripIndent(),
                        enableArtifactsList: false, showChangeset: true
            }
        }
    }
}
