def COLOR_MAP = [
    'SUCCESS': 'good',
    'FAILURE': 'danger',
    'UNSTABLE': 'warning',
    'ABORTED': '#808080'
]

pipeline {
    agent any

    tools {
        maven "maven"
        jdk "openjdk17"
    }

    environment {
        SCANNER_HOME = tool 'sonar-scanner'
        APP_NAME = "java-registration-app"
        RELEASE = "1.0.0"
        DOCKER_USER = "andynze4"
        DOCKER_PASS = 'DockerHub-Token-18dockerhub'
        NEXUS_USER = 'admin'
        NEXUS_PASS = 'please'
        IMAGE_NAME = "${DOCKER_USER}/${APP_NAME}"
        IMAGE_TAG = "${RELEASE}-${BUILD_NUMBER}"
        SONAR_SERVER = 'SonarQube-Server'
        RELEASE_REPO = 'vtech-release'
        CENTRAL_REPO = 'vtech-maven-central'
        NEXUSIP = '172.16.226.100'
        NEXUSPORT = '8081'
        NEXUS_GRP_REPO = 'vtech-maven-group'
        NEXUS_LOGIN = 'nexuslogin'
        NEXUS_PROTOCOL = 'http'
        NEXUS_URL = 'http://172.16.226.100:8081'
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

        stage ('Build Artifact') {
            steps {
                dir('webapp') {
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
                dir('webapp') {
                    // Execute tests and generate jacoco.exec file
                    sh "mvn -s settings.xml test"
                }
            }
        }

        stage('Code Analysis with Checkstyle') {
            steps {
                dir('webapp') {
                    sh "mvn -s settings.xml checkstyle:checkstyle"
                }
            }
            post {
                success {
                    echo 'Generated Analysis Result'
                }
            }
        }

        stage ('SonarQube Analysis') {
            steps {
                withSonarQubeEnv(SONAR_SERVER) {
                    dir('webapp') {
                        sh """${SCANNER_HOME}/bin/sonar-scanner \
                            -Dsonar.projectKey=vtech \
                            -Dsonar.projectName=vtech-app \
                            -Dsonar.projectVersion=1.0.0 \
                            -Dsonar.sources=src/ \
                            -Dsonar.java.binaries=target/classes,target/test-classes \
                            -Dsonar.junit.reportsPath=target/surefire-reports/ \
                            -Dsonar.jacoco.reportsPath=target/jacoco.exec \
                            -Dsonar.java.checkstyle.reportPaths=target/checkstyle-result.xml"""
                    }
                }
            }
        }

        stage("Quality Gate") {
            steps {
                script {
                    waitForQualityGate abortPipeline: true, credentialsId: 'token-for-jenkins'
                }
            }
        }

        stage ('Publish to Nexus Repository Manager') {
            steps {
                nexusArtifactUploader (
                    nexusVersion: "${NEXUS_VERSION}",
                    protocol: "${NEXUS_PROTOCOL}",
                    nexusUrl: "${NEXUSIP}:${NEXUSPORT}",
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
        stage('TRIVY FS SCAN') {
            steps {
                sh "trivy fs . > trivyfs.txt"
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
                    notifyCommitters: false,
                    iconEmoji: '',
                    username: '',
                    timestamp: ''
                )
            }
        }
    }
}
