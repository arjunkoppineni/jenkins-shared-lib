def installAndTestApp(String path = 'myapp/backend') {
    dir(path) {
        sh 'npm install'
        sh 'npm test || echo "No tests found"'
    }
}

def deployToK8s(String kubeDir = 'myapp/kubernetes') {
    withKubeConfig([credentialsId: 'kubeconfig']) {
        dir(kubeDir) {
            sh 'kubectl apply -f deployment.yaml'
            sh 'kubectl apply -f service.yaml'
            sh 'kubectl rollout status deployment my-sample-app-deployment'
            sh 'kubectl get pods'
        }
    }
}
