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



        stage('Create Kind Cluster') {

            steps {

                sh '''

                echo "Checking Kind cluster"


                if ! kind get clusters | grep -q "^kind$"
                then

                    echo "Creating Kind cluster"

                    kind create cluster \
                    --config k8s/cluster.yml

                else

                    echo "Kind cluster already exists"

                fi


                kubectl cluster-info


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



        stage('Decide Blue Green') {


            steps {


                script {


                    def blueExists = sh(

                        script: "kubectl get deployment springboot-app-blue",

                        returnStatus: true

                    )


                    if (blueExists != 0) {


                        echo "First deployment"

                        env.DEPLOY_COLOR = "blue"


                    } else {


                        echo "Blue exists"

                        echo "Deploying Green"

                        env.DEPLOY_COLOR = "green"

                    }


                    echo "Deploying ${env.DEPLOY_COLOR}"

                }

            }

        }




        stage('Deploy Application') {


            steps {


                sh """

                sed -i '' \
                "s|IMAGE_TAG|${IMAGE_TAG}|g" \
                k8s/${env.DEPLOY_COLOR}/deployment.yml



                kubectl apply \
                -f k8s/${env.DEPLOY_COLOR}/deployment.yml



                kubectl apply \
                -f k8s/${env.DEPLOY_COLOR}/service.yml



                kubectl rollout status \
                deployment/springboot-app-${env.DEPLOY_COLOR}



                """

            }

        }




        stage('Health Check') {


            steps {


                sh """

                echo "Checking ${env.DEPLOY_COLOR} health"


                kubectl get pods \
                -l color=${env.DEPLOY_COLOR}



                kubectl rollout status \
                deployment/springboot-app-${env.DEPLOY_COLOR} \
                --timeout=120s


                """

            }

        }




        stage('Verify') {


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

            Deployment Successful 🚀

            Image:
            ${IMAGE_NAME}:${IMAGE_TAG}

            """

        }


        failure {

            echo "Deployment Failed ❌"

        }

    }

}