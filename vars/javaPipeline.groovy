def call(Map config = [:]) {
    pipeline {
        agent any

        environment {
            IMAGE_NAME = config.imageName ?: 'my-org/java-app'
            TAG = config.tag ?: 'latest'
            DOCKER_CRED_ID = config.dockerCredId ?: 'Docker_credentials'
            KUBECONFIG_CRED_ID = config.kubeconfigId ?: 'kubeconfig'
        }

        stages {
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Build with Maven') {
                steps {
                    sh 'mvn clean package'
                    sh 'ls -l target'
                }
            }

            stage('Docker Build and Push') {
                when {
                    branch 'Develop'
                }
                steps {
                    script {
                        docker.withRegistry('https://index.docker.io/v2/', DOCKER_CRED_ID) {
                            def image = docker.build("${IMAGE_NAME}:${TAG}")
                            image.push()
                        }
                    }
                }
            }

            stage('Deploy to Kubernetes') {
                when {
                    branch 'Develop'
                }
                steps {
                    withKubeConfig([credentialsId: KUBECONFIG_CRED_ID]) {
                        sh 'kubectl apply -f deployment.yaml'
                        sh 'kubectl apply -f service.yaml'
                    }
                }
            }
        }
    }
}
