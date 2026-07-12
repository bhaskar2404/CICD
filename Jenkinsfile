pipeline {
    agent any

    environment {
        PATH = "/usr/local/bin:${env.PATH}"
        IMAGE_NAME = "bhaskarvanam/spring-boot"
        IMAGE_TAG = "1.0.${BUILD_NUMBER}"
    }

    tools {
        jdk 'JDK 21'
        maven 'mvn'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'dev', url: 'https://github.com/bhaskar2404/CICD.git'
            }
        }

        stage('Build & Test') {
            steps {
                sh '''
                    java -version
                    mvn clean package
                '''
            }
        }

        stage('Docker Check') {
            steps {
                sh '''
                    which docker
                    docker version
                    docker ps
                '''
            }
        }

        stage('Docker Build') {
            steps {
                sh '''
                    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                '''
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'c98361e3-927f-49d7-95cf-1214da85f03f',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_TOKEN'
                    )
                ]) {
                    sh '''
                        echo $DOCKER_TOKEN | docker login -u $DOCKER_USER --password-stdin
                        docker push ${IMAGE_NAME}:${IMAGE_TAG}
                    '''
                }
            }
        }

        stage('Check Files') {
            steps {
                sh '''
                    pwd
                    ls -la
                    ls -la k8s
                '''
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                    sed -i "s/IMAGE_TAG/${IMAGE_TAG}/g" k8s/deployment.yml
                    kubectl apply -f k8s/deployment.yml
                    kubectl apply -f k8s/service.yml
                '''
            }
        }

        stage('Verify Deployment') {
            steps {
                sh '''
                    kubectl get pods
                    kubectl get svc
                '''
            }
        }
    }

    post {
        success {
            echo 'CI/CD Pipeline completed successfully 🚀'
        }
        failure {
            echo 'Pipeline failed ❌'
        }
    }
}
