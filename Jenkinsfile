pipeline {
    agent any
    tools {
        maven 'maven'
    }
    environment {
        SCANNER_HOME = tool 'sonar-scanner'
        APP_NAME = "java-registration-app"
        RELEASE = "1.0.0"
        DOCKER_USER = "andynze4"
        DOCKER_PASS = 'DockerHub-Token-18dockerhub'
        IMAGE_NAME = "${DOCKER_USER}" + "/" + "${APP_NAME}"
        IMAGE_TAG = "${RELEASE}-${BUILD_NUMBER}"
        SONARSERVER = 'SonarQube-Server'
        NEXUS_USER = 'admin'
        NEXUS_PASS = 'please'
        RELEASE_REPO = 'vtech-release'
        CENTRAL_REPO = 'vtech-maven-central'
        NEXUSIP = '172.16.226.100'
        NEXUSPORT = '8081'
        NEXUS_GRP_REPO = 'vtech-maven-group'
        NEXUS_LOGIN = 'nexuslogin'
        NEXUS_PROTOCOL = 'http'
        NEXUS_URL = 'http://172.16.226.100:8081/'
        NEXUS_REPOGRP_ID = 'QA'
        NEXUS_VERSION = 'nexus3'
    }
    stages {
        stage('clean workspace') {
            steps {
                cleanWs()
            }
         }
        stage('Checkout from Git') {
            steps {
                git branch: 'master', url: 'https://github.com/andynze1/registration-app.git'
            }
         }
        stage ('Build Artifact')  {
	        steps {
                dir('webapp'){
                sh "mvn -s settings.xml -DskipTests clean install"
                }
             }
            post {
                success {
                    echo 'Now Archiving...'
                    archiveArtifacts artifacts: '**/target/*.war'
                }
            }
         }
        stage('Unit Test') {
            steps {
                sh 'mvn -s settings.xml test'
            }
        }
        stage('Code Analysis with Checkstyle') {
            steps {
                sh 'mvn -s settings.xml checkstyle:checkstyle'
            }
            post {
                success {
                    echo 'Generated Analysis Result'
                }
            }
        }
        stage ('SonarQube Analysis with SonarQube') {
            environment {
                scannerHome = tool SONARSCANNER
            }
            steps {
              withSonarQubeEnv(SONARSCANNER) {
                dir('webapp'){
                sh 'mvn -U clean install sonar:sonar'
                }
              }
            }
         }
        stage("Quality Gate") {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
         }
        stage ('Publish to Nexus Repository Manager') {
            steps {
                nexusArtifactUploader (
                nexusVersion: "${NEXUS_VERSION}",
                protocol: "${NEXUS_PROTOCOL}",
                nexusUrl: "${NEXUS_URL}",
                groupId: "${NEXUS_REPOGRP_ID}",
                version: "${env.BUILD_ID}-${env.BUILD_TIMESTAMP}",
                repository: "${RELEASE_REPO}",
                credentialsId: "${NEXUS_LOGIN}",
                artifacts: [
                    [artifactId: 'vtechapp',
                    classifier: '',
                    file: 'target/webapp.war',
                    type: 'war']
                    ]
                )
            }
        }
    }
    post {
        always {
            script {
                def color = COLOR_MAP.get(currentBuild.currentResult, '#808080') // Default to gray if result not in map
                echo 'Slack Notification.'
                slackSend (
                    channel: '#jenkinscicd',
                    color: color,
                    message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER} \nMore info at: ${env.BUILD_URL}",
                to: 'andynze4@gmail.com',
                attachmentsPattern: 'trivyfs.txt,trivyimage.txt'
                )
            }
        }
    }
}