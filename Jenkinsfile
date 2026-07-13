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


                        echo "No BLUE deployment found"

                        echo "First deployment -> Deploy BLUE"


                        env.DEPLOY_COLOR = "blue"


                    }

                    else {


                        echo "BLUE deployment exists"

                        echo "New release -> Deploy GREEN"


                        env.DEPLOY_COLOR = "green"


                    }


                    echo "Selected deployment color: ${env.DEPLOY_COLOR}"


                }

            }

        }



        stage('Prepare Kubernetes Manifest') {

            steps {


                sh """

                echo Updating image tag


                sed -i '' \
                "s/IMAGE_TAG/${IMAGE_TAG}/g" \
                k8s/${env.DEPLOY_COLOR}/deployment.yml


                cat k8s/${env.DEPLOY_COLOR}/deployment.yml


                """

            }

        }




        stage('Deploy Application') {

            steps {


                sh """

                echo Deploying ${env.DEPLOY_COLOR}


                kubectl apply \
                -f k8s/${env.DEPLOY_COLOR}/deployment.yml


                kubectl apply \
                -f k8s/${env.DEPLOY_COLOR}/service.yml



                kubectl rollout status \
                deployment/springboot-app-${env.DEPLOY_COLOR}


                """

            }

        }



        stage('Verify Deployment') {

            steps {


                sh '''

                echo "Pods"

                kubectl get pods


                echo "Services"

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

                    message: 'Green deployment is ready. Continue traffic switch?'

                )


            }

        }


    }



    post {


        success {


            echo """
            CI/CD Pipeline completed successfully 🚀

            Image:
            ${IMAGE_NAME}:${IMAGE_TAG}

            Deployment:
            ${env.DEPLOY_COLOR}
            """

        }



        failure {


            echo "Pipeline failed ❌"


        }

    }

}