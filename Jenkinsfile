pipeline {
	options {
		timeout(time: 80, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr:'5'))
		disableConcurrentBuilds(abortPrevious: true)
	}
	agent {
		label "centos-latest"
	}
	tools {
		maven 'apache-maven-latest'
		jdk 'temurin-jdk17-latest'
	}
	stages {
		stage('Build') {
			steps {
				wrap([$class: 'Xvnc', useXauthority: true]) {
					sh """
					mvn clean verify --batch-mode --fail-at-end -Dmaven.repo.local=$WORKSPACE/.m2/repository \
						-Pbree-libs -Papi-check -Pjavadoc \
						-Dcompare-version-with-baselines.skip=false \
						-Dproject.build.sourceEncoding=UTF-8 \
						-Dorg.slf4j.simpleLogger.showDateTime=true -Dorg.slf4j.simpleLogger.dateTimeFormat=HH:mm:ss.SSS \
						-DtrimStackTrace=false 
					"""
				}
			}
			post {
				always {
					archiveArtifacts artifacts: '*.log,*/target/work/data/.metadata/*.log,*/tests/target/work/data/.metadata/*.log,apiAnalyzer-workspace/.metadata/*.log', allowEmptyArchive: true
					junit '**/target/surefire-reports/TEST-*.xml'
					discoverGitReferenceBuild referenceJob: 'eclipse.platform.ui/master'
					recordIssues publishAllIssues: true, tools: [eclipse(name: 'Compiler and API Tools', pattern: '**/target/compilelogs/*.xml'), mavenConsole(), javaDoc()]
				}
			}
		}
	}
}
