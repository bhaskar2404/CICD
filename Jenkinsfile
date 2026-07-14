pipeline {

    agent any

    environment {
        PATH = "/usr/local/bin:/opt/homebrew/bin:${env.PATH}"
        IMAGE_NAME = "bhaskarvanam/spring-boot"
        IMAGE_TAG  = "1.0.${BUILD_NUMBER}"
        DEPLOY_COLOR = ""
    }

    tools {
        jdk 'JDK 21'
        maven 'mvn'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'dev',
                    url: 'https://github.com/bhaskar2404/CICD.git'
            }
        }

        stage('Tool Check') {
            steps {
                sh '''
                    which docker
                    which kubectl

                    docker version
                    kubectl version --client
                '''
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
                    docker build \
                    -t ${IMAGE_NAME}:${IMAGE_TAG} .
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
                        echo $DOCKER_TOKEN | docker login \
                        -u $DOCKER_USER \
                        --password-stdin

                        docker push ${IMAGE_NAME}:${IMAGE_TAG}
                    '''
                }
            }
        }

        stage('Kubernetes Check') {
            steps {
                sh '''
                    kubectl cluster-info
                    kubectl get nodes
                '''
            }
        }

        stage('Decide Deployment Color') {

            steps {

                script {

                    def blueExists = sh(
                        script: "kubectl get deployment springboot-app-blue >/dev/null 2>&1",
                        returnStatus: true
                    )

                    if (blueExists != 0) {

                        env.DEPLOY_COLOR = "blue"
                        echo "======== FIRST DEPLOYMENT ========"

                    } else {

                        env.DEPLOY_COLOR = "green"
                        echo "======== BLUE EXISTS ========"
                        echo "Deploying to GREEN"

                    }

                    echo "Deploy Color : ${env.DEPLOY_COLOR}"

                }

            }

        }

        stage('Deploy Application') {

            steps {

                sh """
                    echo "Deploying ${DEPLOY_COLOR}"

                    sed -i.bak "s|IMAGE_TAG|${IMAGE_TAG}|g" \
                    k8s/${DEPLOY_COLOR}/deployment.yml

                    kubectl apply -f k8s/${DEPLOY_COLOR}/deployment.yml
                    kubectl apply -f k8s/${DEPLOY_COLOR}/service.yml

                    kubectl rollout status \
                    deployment/springboot-app-${DEPLOY_COLOR} \
                    --timeout=120s
                """

            }

        }

        stage('Health Check') {

            steps {

                sh """
                    kubectl get pods -l color=${DEPLOY_COLOR}

                    kubectl get svc springboot-${DEPLOY_COLOR}-service

                    kubectl get endpoints springboot-${DEPLOY_COLOR}-service
                """

            }

        }

        stage('Approve BLUE Deployment') {

            when {
                expression {
                    return env.DEPLOY_COLOR == "green"
                }
            }

            steps {

                input(
                    message: "GREEN deployment is healthy.\nDeploy same image to BLUE?",
                    ok: "Deploy BLUE"
                )

            }

        }

        stage('Deploy BLUE') {

            when {
                expression {
                    return env.DEPLOY_COLOR == "green"
                }
            }

            steps {

                sh """
                    echo "Deploying ${IMAGE_TAG} to BLUE"

                    sed -i.bak "s|IMAGE_TAG|${IMAGE_TAG}|g" \
                    k8s/blue/deployment.yml

                    kubectl apply -f k8s/blue/deployment.yml
                    kubectl apply -f k8s/blue/service.yml

                    kubectl rollout status \
                    deployment/springboot-app-blue \
                    --timeout=120s
                """

            }

        }

        stage('Verify BLUE') {

            when {
                expression {
                    return env.DEPLOY_COLOR == "green"
                }
            }

            steps {

                sh '''
                    kubectl get pods -l color=blue

                    kubectl get svc springboot-blue-service

                    echo "Testing BLUE endpoint"

                    curl http://localhost:30080/api/v1/entry
                '''

            }

        }

        stage('Delete GREEN') {

            when {
                expression {
                    return env.DEPLOY_COLOR == "green"
                }
            }

            steps {

                input(
                    message: "BLUE deployment is successful.\nDelete GREEN deployment?",
                    ok: "Delete GREEN"
                )

                sh '''
                    kubectl delete deployment springboot-app-green
                    kubectl delete service springboot-green-service
                '''

            }

        }

    }

    post {

        success {

            echo """
=========================================
CI/CD COMPLETED SUCCESSFULLY
=========================================
Image      : ${IMAGE_NAME}:${IMAGE_TAG}
Deployment : ${DEPLOY_COLOR}
=========================================
"""

        }

        failure {

            echo """
=========================================
PIPELINE FAILED
=========================================
"""

        }

    }

}