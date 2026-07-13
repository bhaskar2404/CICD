```groovy
pipeline {

    agent any


    environment {

        PATH = "/usr/local/bin:/opt/homebrew/bin:${env.PATH}"

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



        stage('Tool Check') {

            steps {

                sh '''

                echo "Checking tools"

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


                    docker push \
                    ${IMAGE_NAME}:${IMAGE_TAG}


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

                        script: "kubectl get deployment springboot-app-blue",

                        returnStatus: true

                    )


                    if (blueExists != 0) {


                        env.DEPLOY_COLOR = "blue"

                        echo "First deployment -> BLUE"


                    } else {


                        env.DEPLOY_COLOR = "green"

                        echo "Existing BLUE found -> Deploy GREEN"

                    }


                    echo "Selected deployment: ${env.DEPLOY_COLOR}"

                }

            }

        }





        stage('Validate Kubernetes Files') {

            steps {

                sh """

                ls -la k8s/${DEPLOY_COLOR}


                test -f k8s/${DEPLOY_COLOR}/deployment.yml

                test -f k8s/${DEPLOY_COLOR}/service.yml


                """

            }

        }





        stage('Deploy Application') {

            steps {

                sh """

                echo "Deploying ${DEPLOY_COLOR}"


                sed -i '' \
                "s|IMAGE_TAG|${IMAGE_TAG}|g" \
                k8s/${DEPLOY_COLOR}/deployment.yml



                kubectl apply \
                -f k8s/${DEPLOY_COLOR}/deployment.yml



                kubectl apply \
                -f k8s/${DEPLOY_COLOR}/service.yml



                kubectl rollout status \
                deployment/springboot-app-${DEPLOY_COLOR} \
                --timeout=120s


                """

            }

        }





        stage('Health Check') {

            steps {

                sh """

                echo "Checking ${DEPLOY_COLOR}"


                kubectl get pods \
                -l color=${DEPLOY_COLOR}


                kubectl get endpoints \
                springboot-${DEPLOY_COLOR}-service


                """

            }

        }





        stage('Green Approval') {

            when {

                expression {

                    return env.DEPLOY_COLOR == "green"

                }

            }


            steps {


                input(

                    message: 'Green is healthy. Promote traffic?',

                    ok: 'Promote Green'

                )


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

            echo """

            CI/CD Completed Successfully 🚀


            Image:

            ${IMAGE_NAME}:${IMAGE_TAG}


            Deployment:

            ${DEPLOY_COLOR}

            """

        }


        failure {

            echo "Pipeline Failed ❌"

        }


    }

}
```
