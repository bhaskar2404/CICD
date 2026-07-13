pipeline {

    agent any


    environment {

        PATH = "/usr/local/bin:${env.PATH}"

        IMAGE_NAME = "bhaskarvanam/spring-boot"

        IMAGE_TAG = "1.0.${BUILD_NUMBER}"

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



        stage('Decide Deployment Color') {

            steps {


                script {


                    def blueExists = sh(

                        script: "kubectl get deployment springboot-app-blue",

                        returnStatus: true

                    )


                    if (blueExists != 0) {


                        echo "First deployment detected"

                        env.DEPLOY_COLOR = "blue"


                    }

                    else {


                        echo "Existing application found"

                        echo "Deploying new version to GREEN"


                        env.DEPLOY_COLOR = "green"


                    }


                    echo "Selected deployment: ${DEPLOY_COLOR}"

                }

            }

        }



        stage('Deploy Application') {

            steps {


                sh '''

                echo Deploying ${DEPLOY_COLOR}


                sed -i '' \
                "s/IMAGE_TAG/${IMAGE_TAG}/g" \
                k8s/${DEPLOY_COLOR}/deployment.yml



                kubectl apply \
                -f k8s/${DEPLOY_COLOR}/deployment.yml



                kubectl apply \
                -f k8s/${DEPLOY_COLOR}/service.yml



                kubectl rollout status deployment/springboot-app-${DEPLOY_COLOR}


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



        stage('Green Approval') {


            when {

                expression {

                    env.DEPLOY_COLOR == "green"

                }

            }


            steps {


                input(

                    message: 'Green deployment ready. Continue traffic switch?'

                )


            }

        }



    }



    post {


        success {


            echo "Deployment completed successfully 🚀"

            echo "Version: ${IMAGE_TAG}"

            echo "Color: ${DEPLOY_COLOR}"


        }


        failure {


            echo "Pipeline Failed ❌"


        }

    }

}