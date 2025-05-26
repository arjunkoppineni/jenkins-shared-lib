def call(Map config = [:]) {
    pipeline {
        agent any

        stages {
            stage('Setup Environment') {
                steps {
                    script {
                        env.IMAGE_NAME = config.imageName ?: 'my-org/java-app'
                        env.TAG = config.tag ?: 'latest'
                        env.DOCKER_CRED_ID = config.dockerCredId ?: 'docker-hub-cred'
                        env.KUBECONFIG_CRED_ID = config.kubeconfigId ?: 'kubeconfig'
                    }
                }
            }

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
                        docker.withRegistry('https://index.docker.io/v2/', env.DOCKER_CRED_ID) {
                            def image = docker.build("${env.IMAGE_NAME}:${env.TAG}")
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
                    withKubeConfig([credentialsId: env.KUBECONFIG_CRED_ID]) {
                        sh 'kubectl apply -f deployment.yaml'
                        sh 'kubectl apply -f service.yaml'
                    }
                }
            }
        }
    }
}
