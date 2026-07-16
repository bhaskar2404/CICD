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

                         echo "================================"

                         echo "FIRST DEPLOYMENT"

                         echo "DEPLOYING BLUE"

                         echo "================================"


                     }

                     else {


                         env.DEPLOY_COLOR = "green"


                         echo "================================"

                         echo "BLUE EXISTS"

                         echo "DEPLOYING GREEN"

                         echo "================================"


                     }


                     echo "FINAL COLOR : ${env.DEPLOY_COLOR}"


                 }

             }

         }




         stage('Validate Color') {


             steps {


                 script {


                     if (!env.DEPLOY_COLOR) {

                         error("DEPLOY_COLOR is empty")

                     }


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





         stage('Deploy BLUE Ingress') {


             when {


                 expression {


                     return env.DEPLOY_COLOR == "blue"


                 }

             }


             steps {


                 sh """


                 echo "Creating BLUE ingress"


                 kubectl apply \
                 -f k8s/blue/ingress.yml



                 kubectl get ingress


                 """

             }

         }


stage('Deploy BLUE Ingress') {

    when {
        expression {
            return env.DEPLOY_COLOR == "green"
        }
    }

    steps {

        sh """

        echo "Switching Ingress to BLUE"

        kubectl apply -f k8s/blue/ingress.yml

        kubectl rollout status deployment/ingress-nginx-controller \
            -n ingress-nginx --timeout=60s || true

        kubectl describe ingress springboot-ingress

        """

    }
}


         stage('Approve GREEN Traffic Switch') {


             when {


                 expression {


                     return env.DEPLOY_COLOR == "green"


                 }

             }


             steps {


                 input(

                 message: "GREEN is healthy. Switch traffic to GREEN?",

                 ok: "Promote GREEN"

                 )


             }

         }





         stage('Switch Traffic To GREEN') {


             when {


                 expression {


                     return env.DEPLOY_COLOR == "green"


                 }

             }


             steps {


                 sh """


                 echo "Switching traffic to GREEN"


                 kubectl apply \
                 -f k8s/green/ingress.yml



                 kubectl get ingress


                 """

             }

         }





         stage('Verify Deployment') {


             steps {


                 sh '''


                 kubectl get pods

                 kubectl get svc

                 kubectl get ingress


                 '''

             }

         }





         stage('Delete BLUE') {


             when {


                 expression {


                     return env.DEPLOY_COLOR == "green"


                 }

             }


             steps {


                 input(

                 message: "GREEN is live. Delete BLUE?",

                 ok: "Delete BLUE"

                 )


                 sh '''


                 kubectl delete deployment springboot-app-blue


                 kubectl delete service springboot-blue-service


                 '''


             }

         }


     }



     post {


         success {


             echo """

 ========================================

 PIPELINE SUCCESS 🚀


 IMAGE:

 ${IMAGE_NAME}:${IMAGE_TAG}


 DEPLOYED COLOR:

 ${env.DEPLOY_COLOR}


 ========================================

 """

         }



         failure {


             echo """

 ========================================

 PIPELINE FAILED ❌

 ========================================

 """


         }

     }

 }