// Jenkinsfile - per progetto scolastico
// Funziona con Jenkins installato localmente

pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-21'
    }

    environment {
        // Variabili d'ambiente per i test
        SPRING_DATASOURCE_URL = 'jdbc:postgresql://localhost:5432/testdb'
        SPRING_DATASOURCE_USERNAME = 'postgres'
        SPRING_DATASOURCE_PASSWORD = 'postgres'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📦 Clonazione repository...'
                checkout scm
            }
        }

        stage('Test Backend Spring') {
            steps {
                echo '🧪 Esecuzione test Spring Boot...'
                dir('backend-spring') {
                    sh 'mvn clean test'
                }
            }
            post {
                always {
                    junit 'backend-spring/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                echo '🐳 Build immagini Docker...'
                sh 'docker build -t nutritionists-backend:latest ./backend-spring'
                sh 'docker build -t nutritionists-flask:latest ./backend-flask'
                sh 'docker build -t nutritionists-frontend:latest ./frontend-react'
            }
        }

        stage('Deploy Locale (Demo)') {
            steps {
                echo '🚀 Avvio container con docker-compose...'
                sh 'docker-compose down || true'
                sh 'docker-compose up -d'
                sh 'sleep 10'
            }
        }

        stage('Health Check') {
            steps {
                echo '🏥 Verifica servizi...'
                sh 'curl -f http://localhost:8080/api/health || exit 1'
                sh 'curl -f http://localhost:5000/health || exit 1'
                sh 'curl -f http://localhost || exit 1'
            }
        }
    }

    post {
        success {
            echo '🎉 Pipeline completata con successo!'
            echo '🌐 Frontend: http://localhost'
            echo '🔧 API: http://localhost:8080/api'
        }
        failure {
            echo '❌ Pipeline fallita! Controlla i log.'
        }
    }
}