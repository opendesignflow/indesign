// Take the string and echo it.
def transformIntoStep(jobFullName) {
    return {
        build job: jobFullName , wait: false, propagate: false
    }
}

// Indesign
node {
    //-- Github trigger
    properties([pipelineTriggers([[$class: 'GitHubPushTrigger']])])

    //-- JDK
    jdk = tool name: 'adopt-jdk11'
    env.JAVA_HOME = "${jdk}"

    //-- Maven
    def mvnHome = tool 'maven3'
    mavenOptions = '-B -U'

    stage('Clean') {
        checkout scm
        sh 'chmod +x gradlew'
        sh './gradlew clean'
    //sh "${mvnHome}/bin/mvn ${mavenOptions} clean"
    }

    stage('Build') {
        sh './gradlew generateOOXOO --no-daemon'
        sh './gradlew build --no-daemon'
    //sh "${mvnHome}/bin/mvn ${mavenOptions}  -DskipTests=true install"
    //junit '**/target/surefire-reports/TEST-*.xml'
    }

    stage('Test') {
    }

    if (env.BRANCH_NAME == 'dev' || env.BRANCH_NAME == 'master') {
        stage('Deploy') {
            //sh "${mvnHome}/bin/mvn ${mavenOptions} -DskipTests=true deploy"
            // step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
            sh './gradlew publish'
        }

        // Trigger sub builds on dev
        if (env.BRANCH_NAME == 'dev') {
            stage('Downstream') {
                //'../instruments/dev' , '../ioda-core/dev'
                def downstreams = []
                def stepsForParallel = [:]
                for (x in downstreams) {
                    def ds = x
                    stepsForParallel[ds] = transformIntoStep(ds)
                }

                parallel stepsForParallel
            }
        }
  } else {
        stage('Package') {
            sh "${mvnHome}/bin/mvn -B -DskipTests=true -Dmaven.test.failure.ignore package"
            step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])
        }
    }
}
