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


        stage('Build') {

            steps {

                sh """

                java -version

                mvn clean package

                """

            }

        }


        stage('Docker Build') {

            steps {

                sh """

                docker build \
                -t ${IMAGE_NAME}:${IMAGE_TAG} .

                """

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


                    sh """

                    echo \$DOCKER_TOKEN | docker login \
                    -u \$DOCKER_USER \
                    --password-stdin


                    docker push ${IMAGE_NAME}:${IMAGE_TAG}


                    """

                }

            }

        }



        stage('Kubernetes Check') {

            steps {

                sh """

                kubectl get nodes

                """

            }

        }



        stage('Decide Color') {

            steps {

                script {


                    def blue = sh(

                    script: "kubectl get deployment springboot-app-blue",

                    returnStatus:true

                    )


                    if(blue != 0){

                        env.DEPLOY_COLOR="blue"

                    }
                    else{

                        env.DEPLOY_COLOR="green"

                    }


                    echo "Deploying ${env.DEPLOY_COLOR}"

                }

            }

        }




        stage('Deploy Application') {

            steps {


                sh """

                sed -i.bak \
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

                kubectl get pods \
                -l color=${DEPLOY_COLOR}


                kubectl get endpoints \
                springboot-${DEPLOY_COLOR}-service


                """

            }

        }



        /*
          FIRST DEPLOYMENT
          Create BLUE test ingress
        */

        stage('Create Blue Ingress') {


            when {

                expression {

                    return env.DEPLOY_COLOR=="blue"

                }

            }


            steps {


                sh """

                echo "Creating Blue Ingress"


                kubectl apply \
                -f k8s/blue/ingress-test.yml


                kubectl apply \
                -f k8s/blue/ingress-prod.yml


                kubectl get ingress


                """

            }

        }



        /*
          GREEN deployment testing
        */


        stage('Create Green Test Ingress') {


            when {

                expression {

                    return env.DEPLOY_COLOR=="green"

                }

            }


            steps {


                sh """

                echo "Creating Green Test Ingress"


                kubectl apply \
                -f k8s/green/ingress-test.yml


                kubectl get ingress


                """

            }

        }




        stage('Approve Production Switch') {


            when {

                expression {

                    return env.DEPLOY_COLOR=="green"

                }

            }


            steps {


                input(

                message:
                "Green tested successfully. Switch production traffic?",

                ok:
                "Promote Green"

                )

            }

        }




        stage('Switch Production To Green') {


            when {

                expression {

                    return env.DEPLOY_COLOR=="green"

                }

            }


            steps {


                sh """

                echo "Switching production traffic to Green"


                kubectl apply \
                -f k8s/green/ingress-prod.yml



                kubectl describe ingress springboot-ingress


                """

            }

        }




        stage('Verify') {


            steps {


                sh """

                kubectl get pods

                kubectl get svc

                kubectl get ingress


                """

            }

        }


    }


    post {


        success {

            echo """

====================================

PIPELINE SUCCESS 🚀

IMAGE:

${IMAGE_NAME}:${IMAGE_TAG}


COLOR:

${DEPLOY_COLOR}

====================================

"""

        }


        failure {

            echo """

====================================

PIPELINE FAILED ❌

====================================

"""

        }

    }

}