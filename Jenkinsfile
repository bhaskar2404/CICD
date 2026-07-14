pipeline {

```
agent any

environment {

    PATH = "/usr/local/bin:/opt/homebrew/bin:${env.PATH}"

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

                    script: "kubectl get deployment springboot-app-blue",

                    returnStatus: true

                )


                if (blueExists != 0) {


                    env.DEPLOY_COLOR = "blue"

                    echo "FIRST DEPLOYMENT"

                    echo "Deploying BLUE"


                } else {


                    env.DEPLOY_COLOR = "green"

                    echo "BLUE EXISTS"

                    echo "Deploying GREEN"

                }


                echo "Selected Color: ${env.DEPLOY_COLOR}"


            }

        }

    }



    stage('Deploy Application') {

        steps {

            sh """

                echo "Deploying ${env.DEPLOY_COLOR}"


                sed -i.bak \
                "s|IMAGE_TAG|${IMAGE_TAG}|g" \
                k8s/${env.DEPLOY_COLOR}/deployment.yml



                kubectl apply \
                -f k8s/${env.DEPLOY_COLOR}/deployment.yml



                kubectl apply \
                -f k8s/${env.DEPLOY_COLOR}/service.yml



                kubectl rollout status \
                deployment/springboot-app-${env.DEPLOY_COLOR} \
                --timeout=120s

            """

        }

    }



    stage('Health Check') {

        steps {

            sh """

                echo "Checking ${env.DEPLOY_COLOR}"


                kubectl get pods \
                -l color=${env.DEPLOY_COLOR}


                kubectl get endpoints \
                springboot-${env.DEPLOY_COLOR}-service


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

                message: "GREEN is healthy. Deploy this version to BLUE?",

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


                sed -i.bak \
                "s|IMAGE_TAG|${IMAGE_TAG}|g" \
                k8s/blue/deployment.yml



                kubectl apply \
                -f k8s/blue/deployment.yml



                kubectl apply \
                -f k8s/blue/service.yml



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

                echo "BLUE verification"


                kubectl get pods \
                -l color=blue


                kubectl get svc springboot-blue-service


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

                message: "BLUE is working. Delete GREEN deployment?",

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
```

========================================

PIPELINE SUCCESS 🚀

Image:

${IMAGE_NAME}:${IMAGE_TAG}

Deployment Color:

${env.DEPLOY_COLOR}

========================================

"""

```
    }



    failure {


        echo """
```

========================================

PIPELINE FAILED ❌

========================================

"""

```
    }

}
```

}
