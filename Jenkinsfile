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
       stage('test'){
        steps{
        sh 'mvn test'
        }
       }
       stage('Docker Build'){
        steps{
         sh 'Docker build -t bhaskarvanam/spring-boot:v1 .'
        }
       }

    }
}