// Indesign
node {
 
  properties([pipelineTriggers([[$class: 'GitHubPushTrigger']])])
  def mvnHome = tool 'maven3'

  stage('Clean') {
    checkout scm
    sh "${mvnHome}/bin/mvn -B clean"
  }

  stage('Build') {
    sh "${mvnHome}/bin/mvn -B  compile test-compile"
  }

  stage('Test') {

    if (env.BRANCH_NAME == 'master') {
      sh "${mvnHome}/bin/mvn -B -Dmaven.test.failure.ignore test"
    else {
      sh "${mvnHome}/bin/mvn -B test"
    }
    junit '**/target/surefire-reports/TEST-*.xml'
  }

  if (env.BRANCH_NAME == 'dev' || env.BRANCH_NAME == 'master') {
    stage('Deploy') {
        sh "${mvnHome}/bin/mvn -B -DskipTests=true -Dmaven.test.failure.ignore deploy"
        step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
    }

  } else {
    stage('Package') {
        sh "${mvnHome}/bin/mvn -B -DskipTests=true -Dmaven.test.failure.ignore package"
        step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
    }
  }

 


}
