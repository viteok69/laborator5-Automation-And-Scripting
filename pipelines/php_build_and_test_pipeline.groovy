def PROJECT_NAME = 'php-project'
def REPO_URL = 'https://github.com/viteok69/laborator5-Automation-And-Scripting' 

pipeline {
    agent { label 'ssh-agent' }
    options {
        skipDefaultCheckout true
    }
    stages {
        stage('Checkout Source Code') {
            steps {
                git branch: 'main', url: REPO_URL 
            }
        }
        stage('Install Dependencies') {
            steps {
                dir(PROJECT_NAME) {
                    sh 'composer install --no-dev --prefer-dist' 
                }
            }
        }
        stage('Run Unit Tests') {
            steps {
                dir(PROJECT_NAME) {
                    sh './bin/phpunit tests'
                }
            }
        }
        stage('Build Artifact') {
            steps {
                script {
                    sh "zip -r ../deploy-artifact-${BUILD_NUMBER}.zip *"
                    archiveArtifacts artifacts: "../deploy-artifact-${BUILD_NUMBER}.zip"
                }
            }
        }
    }
    post {
        failure {
            echo "Build failed. Check tests."
        }
    }
}