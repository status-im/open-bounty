#!/usr/bin/env groovy

node('linux') {
checkout scm

	try {
		stage('Build') {

		GIT_COMMIT_HASH = sh (script: "git rev-parse --short HEAD", returnStdout: true)

		docker build -t status-open-bounty:latest -t status-open-bounty:" + env.BUILD_NUMBER + "-" + GIT_COMMIT_HASH + " -t status-open-bounty:" + GIT_COMMIT_HASH + ":latest . "

		}

		stage('Push to registry') {

		}

		stage('Deploy') {

		}
	}
}