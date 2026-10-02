// Jenkinsfile — builds the Seashell Capacitor/Android app on this local Jenkins
// (localhost:8080), running on the same Windows machine/agent used for manual
// builds. Mirrors exactly the steps that were verified to work manually:
//   1) npm install (Capacitor deps)
//   2) npx cap sync android (copy www/ into the native project, sync plugins)
//   3) gradlew assembleDebug, using:
//        - a pinned JDK 21 (org.gradle.java.home is already set in
//          android/gradle.properties, so no extra env var is needed for that)
//        - GRADLE_OPTS pointing at the custom trust store containing the
//          corporate proxy root CAs (Cognizant/Zscaler), required because this
//          machine sits behind an SSL-inspecting proxy and neither JDK's own
//          cacerts file is writable without admin rights.
//   4) archive the resulting APK as a Jenkins build artifact.
//
// Requirements on the Jenkins agent (same as manual local setup):
//   - Node.js + npm on PATH
//   - ANDROID_SDK_ROOT / ANDROID_HOME set (user env var, already configured)
//   - The custom trust store already generated at:
//       %LOCALAPPDATA%\Android\gradle-truststore.jks
//     (see android/gradle.properties comments for how it was created)

pipeline {
    agent any

    options {
        timestamps()
        // Keep only recent builds/artifacts around
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        // Trust store created once, manually, in the user's profile — contains
        // the corporate proxy root CAs needed for Gradle/Maven HTTPS downloads
        // on this locked-down machine. Not committed to the repo.
        GRADLE_OPTS = "-Djavax.net.ssl.trustStore=\"${env.LOCALAPPDATA}\\Android\\gradle-truststore.jks\" -Djavax.net.ssl.trustStorePassword=changeit"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Install dependencies') {
            steps {
                bat 'npm install'
            }
        }

        stage('Sync Capacitor Android project') {
            steps {
                bat 'npx cap sync android'
            }
        }

        stage('Build APK (Gradle)') {
            steps {
                dir('android') {
                    bat 'gradlew.bat assembleDebug'
                }
            }
        }

        stage('Archive APK') {
            steps {
                archiveArtifacts artifacts: 'android/app/build/outputs/apk/debug/*.apk', fingerprint: true
            }
        }
    }

    post {
        always {
            echo 'Build finished. Check the archived APK under this build\'s "Build Artifacts".'
        }
        failure {
            echo 'Build failed — check the console log above. Common causes on this machine: SSL/trust store path wrong, JDK not pinned to 21, or Android SDK env vars not visible to the Jenkins service.'
        }
    }
}
