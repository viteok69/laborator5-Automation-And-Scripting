def PLAYBOOK_INSTALL = 'ansible/playbook-install.yaml'
def ANSIBLE_HOSTS = 'ansible/hosts'

pipeline {
    agent { label 'ansible-agent' } 
    options {
        skipDefaultCheckout true
    }
    stages {
        stage('Checkout Ansible Files') {
            steps {
                git branch: 'main', url: 'https://github.com/viteok69/laborator5-Automation-And-Scripting'
            }
        }
        stage('Deploy Test Server Infrastructure') {
            steps {
                script {
                    sshagent(['ANSIBLE_TO_TEST_SERVER_CREDENTIAL_ID']) {
                        
                        echo "Running Ansible Playbook to configure Test Server (Apache2 + PHP-FPM)..."
                        
                        sh "export ANSIBLE_HOST_KEY_CHECKING=False && ansible-playbook -i ${ANSIBLE_HOSTS} ${PLAYBOOK_INSTALL} --user=ansible"
                    }
                }
            }
        }
    }
    post {
        success {
            echo "Deployment Infrastructure successfully configured!"
        }
        failure {
            echo "Infrastructure setup failed. Check Ansible logs."
        }
    }
}