def call() {
    stage('Checkout') {
        checkout scm
    }

    stage('Unit Tests') {
        echo 'Running unit tests...'
        echo 'This is from Shared library'
    }

    stage('Deploy') {
        echo 'Deploying to environment... from shared library'
        
    }
}
