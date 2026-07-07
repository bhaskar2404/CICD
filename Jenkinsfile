pipeline{
    agent any
    stages{

        stage('Checkout'){
            steps{
            git branch: 'dev',
                url: 'https://github.com/bhaskar2404/CICD.git'
            }
        }

       stage('Build'){
           steps{
                sh 'mvn clean package'
           }
       }

       stage('Test'){
            steps{
                 sh 'mvn test'
            }
       }

       stage('Docker Build'){
            steps{
                sh 'docker build -t bhaskarvanam/spring-boot:v1 .'
            }
       }
       stage('Docker Push'){
        steps{
        sh 'docker push bhaskarvanam/spring-boot:v1'
        }
       }
    }
}