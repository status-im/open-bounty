#!/usr/bin/env groovy

node('linux') {
checkout scm

	try {
		stage('Build') {

			GIT_COMMIT_HASH = sh (script: "git rev-parse --short HEAD | tr -d '\n'", returnStdout: true)

			def openbountyApp = docker.build("statusimdockerhub/openbounty-app:${env.BUILD_NUMBER}")
			openbountyApp.push('latest')

			// sh ("docker build -t status-open-bounty:latest -t status-open-bounty:${env.BUILD_NUMBER}.${GIT_COMMIT_HASH} -t statusimdockerhub/openbounty-app . ")

		}

		stage('Push to registry') {
			sh ("docker push statusimdockerhub/openbounty-app")
		}

		stage('Deploy') {

		}
	} catch (e) {
    // slackSend color: 'bad', message: REPO + ":" + BRANCH_NAME + ' failed to build. ' + env.BUILD_URL
    throw e
  }
}