pipeline {
    agent any

    environment {
        PATH = "/usr/local/bin:/opt/homebrew/bin:${env.PATH}"
        IMAGE_NAME = "bhaskarvanam/spring-boot"
        IMAGE_TAG = "1.0.${BUILD_NUMBER}"
        INGRESS_NAME = "springboot-app-ingress"
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

        stage('Docker Build') {
            steps {
                sh '''
                    docker build -t ${IMAGE_NAME}:${IMAGE_TAG} .
                '''
            }
        }

        stage('Docker Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'c98361e3-927f-49d7-95cf-1214da85f03f',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_TOKEN'
                )]) {
                    sh '''
                        echo $DOCKER_TOKEN | docker login -u $DOCKER_USER --password-stdin
                        docker push ${IMAGE_NAME}:${IMAGE_TAG}
                    '''
                }
            }
        }

        stage('Decide Deployment Color') {
            steps {
                script {
                    def blueExists = sh(
                        script: "kubectl get deployment springboot-app-blue >/dev/null 2>&1",
                        returnStatus: true
                    ) == 0

                    env.DEPLOY_COLOR = blueExists ? "green" : "blue"
                    echo "Deploying to ${env.DEPLOY_COLOR} environment"
                }
            }
        }

        stage('Deploy Application') {
            steps {
                sh '''
                    echo "Deploying to ${DEPLOY_COLOR}"

                    # Update image tag in deployment
                    sed -i.bak "s|IMAGE_TAG|${IMAGE_TAG}|g" k8s/${DEPLOY_COLOR}/deployment.yml

                    kubectl apply -f k8s/${DEPLOY_COLOR}/deployment.yml
                    kubectl apply -f k8s/${DEPLOY_COLOR}/service.yml

                    kubectl rollout status deployment/springboot-app-${DEPLOY_COLOR} --timeout=120s
                '''
            }
        }

        stage('Health Check') {
            steps {
                sh '''
                    echo "Health check for ${DEPLOY_COLOR}"
                    kubectl get pods -l color=${DEPLOY_COLOR}
                    kubectl get svc springboot-${DEPLOY_COLOR}-service
                '''
            }
        }

        stage('Update Ingress') {
            steps {
                sh '''
                    echo "Updating Ingress to route traffic to ${DEPLOY_COLOR}"

                    # Ensure Ingress exists
                    if ! kubectl get ingress ${INGRESS_NAME} >/dev/null 2>&1; then
                        kubectl apply -f k8s/ingress.yml
                    fi

                    # Update Ingress to point to active service
                    kubectl patch ingress ${INGRESS_NAME} --type='json' -p='[
                        {"op": "replace", "path": "/spec/rules/0/http/paths/0/backend/service/name", "value": "springboot-'${DEPLOY_COLOR}'-service"}
                    ]'

                    echo "Ingress successfully updated to ${DEPLOY_COLOR}"
                    kubectl get ingress ${INGRESS_NAME}
                '''
            }
        }

        // Only run when we deployed Green (i.e., switching to Blue)
        stage('Approve Blue Deployment') {
            when { expression { env.DEPLOY_COLOR == 'green' } }
            steps {
                input message: "Green deployment is healthy. Deploy to BLUE?", ok: "Deploy BLUE"
            }
        }

        stage('Deploy Blue') {
            when { expression { env.DEPLOY_COLOR == 'green' } }
            steps {
                sh '''
                    echo "Deploying Blue with same image"
                    sed -i.bak "s|IMAGE_TAG|${IMAGE_TAG}|g" k8s/blue/deployment.yml

                    kubectl apply -f k8s/blue/deployment.yml
                    kubectl apply -f k8s/blue/service.yml

                    kubectl rollout status deployment/springboot-app-blue --timeout=120s
                '''
            }
        }

        stage('Verify Blue') {
            when { expression { env.DEPLOY_COLOR == 'green' } }
            steps {
                sh '''
                    kubectl get pods -l color=blue
                    kubectl get svc springboot-blue-service
                '''
            }
        }

        stage('Delete Green') {
            when { expression { env.DEPLOY_COLOR == 'green' } }
            steps {
                input message: "Blue is running successfully. Delete Green?", ok: "Delete GREEN"
                sh '''
                    kubectl delete deployment springboot-app-green --ignore-not-found=true
                    kubectl delete service springboot-green-service --ignore-not-found=true
                '''
            }
        }
    }

    post {
        success {
            echo """
            ====================================
            ✅ PIPELINE SUCCESS
            Image : ${IMAGE_NAME}:${IMAGE_TAG}
            Color : ${env.DEPLOY_COLOR}
            ====================================
            """
        }
        failure {
            echo """
            ====================================
            ❌ PIPELINE FAILED
            ====================================
            """
        }
    }
}