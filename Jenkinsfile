#!/usr/bin/env groovy

node('linux') {
checkout scm

def dockerreponame = "statusim/openbounty-app"

	try {
		stage('Build & push') {

			GIT_COMMIT_HASH = sh (script: "git rev-parse --short HEAD | tr -d '\n'", returnStdout: true)

			docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-statusvan') {
				def openbountyApp = docker.build("${dockerreponame}:${env.BUILD_NUMBER}")
				openbountyApp.push("${env.BRANCH_NAME}")
		        if (env.BRANCH_NAME == 'develop') {
		            openbountyApp.push("develop")
		        } else if (env.BRANCH_NAME == 'master') {
		            openbountyApp.push("master")
		        } else {
		            println "Not named branch have no custom tag"
		        }
			}

		}

		stage('Deploy') {
			if ( currentBuild.rawBuild.getCauses()[0].toString().contains('UserIdCause') ){
				build job: 'status-openbounty/openbounty-cluster', parameters: [[$class: 'StringParameterValue', name: 'DEPLOY_ENVIRONMENT', value: "dev"], [$class: 'StringParameterValue', name: 'BRANCH', value: env.BRANCH_NAME]]
			} else {
				echo "No deployment on automatic trigger, go to Jenkins and push build button to deliver it."
			}
		}
		stage('Testing') {
			if ( currentBuild.rawBuild.getCauses()[0].toString().contains('UserIdCause') ){
			    echo 'Waiting 2,5 minutes for deployment to complete prior starting smoke testing'
			    sleep 150
				build job: 'status-openbounty/sob-end-to-end-tests', parameters: [[$class: 'StringParameterValue', name: 'BRANCH', value: "*/Jenkins/experiment"]]
			} else {
				echo "No testing on automatic trigger, go to Jenkins and push build button to deliver it and test it."
			}
		}


	} catch (e) {
    // slackSend color: 'bad', message: REPO + ":" + BRANCH_NAME + ' failed to build. ' + env.BUILD_URL
    throw e
  }
}
