pipeline {
    agent {
        label "jdk25"
    }

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

    stages {
        stage ("Gradle: Build") {
            steps {
                withGradle {
                    sh("./gradlew --no-daemon --refresh-dependencies clean build")
                }
            }
        }

        stage ("Gradle: Publish") {
            when {
                branch "main"
            }

            steps {
                withGradle {
                    sh("./gradlew --no-daemon publish")
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
                def description = """
                    **Branch:** ${env.GIT_BRANCH}
                    **Build:** ${env.BUILD_NUMBER}
                    **Status:** ${currentBuild.currentResult}
                """

                discordSend(
                    webhookURL: DISCORD_URL,
                    title: 'CooldownsX',
                    link: env.BUILD_URL,
                    result: currentBuild.currentResult,
                    description: description.stripIndent(),
                    enableArtifactsList: false,
                    showChangeset: true
                )
            }
        }
    }
}
