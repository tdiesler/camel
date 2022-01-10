/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

def MAVEN_PARAMS = '-U -B -e -fae -V -Dnoassembly -Dmaven.compiler.fork=true -Dsurefire.rerunFailingTestsCount=2'

pipeline {

    agent {
        label 'checkin' 
    }

    tools {
        jdk 'java-11'
    }

    environment {
        MAVEN_SKIP_RC = true
    }

    options {
        buildDiscarder(
            logRotator(artifactNumToKeepStr: '5', numToKeepStr: '10')
        )
        disableConcurrentBuilds()
    }

    parameters {
        booleanParam(name: 'CLEAN', defaultValue: true, description: 'Perform the build in clean workspace')
    }

    stages {

        stage('Clean workspace') {
             when {
                 expression { params.CLEAN }
             }
             steps {
                 sh 'git clean -fdx'
           }
        }

        stage('Build & Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh "./mvnw $MAVEN_PARAMS -Pdeploy -Dmaven.test.skip.exec=true clean deploy"
            }
        }

        stage('Website update') {
            when {
                branch 'main'
                changeset 'docs/**/*'
            }

            steps {
                build job: 'Camel/Camel.website/main', wait: false
            }
        }

        stage('Checks') {
            steps {
                sh "./mvnw $MAVEN_PARAMS -pl :camel-buildtools install"
                sh "./mvnw $MAVEN_PARAMS -Psourcecheck -Dcheckstyle.failOnViolation=false checkstyle:check"
            }
        }

        stage('Test') {
            steps {
                sh "./mvnw $MAVEN_PARAMS -Dmaven.test.failure.ignore=true clean install"
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                    junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/*.xml'
                }
            }
        }

    }

    post {
        always {
            emailext(
                subject: '${DEFAULT_SUBJECT}',
                body: '${DEFAULT_CONTENT}',
                recipientProviders: [[$class: 'CulpritsRecipientProvider']]
            )
        }
    }
}

