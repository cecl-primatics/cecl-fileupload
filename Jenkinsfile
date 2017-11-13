node {
  checkout scm
  env.PATH = "${tool 'maven352'}/bin:${env.PATH}"
  stage('Package') {
    dir('') {
      sh 'mvn clean package -DskipTests'
    }
  }

  stage('Create Docker Image') {
    dir('') {
      docker.build("primaticsfinancial2017/cecl-poc-fileupload:${env.BUILD_NUMBER}")
    }
  }

  stage ('Run Application') {
    try {
      sh "docker run primaticsfinancial2017/cecl-poc-fileupload:${env.BUILD_NUMBER}"

      // Run tests using Maven
      dir ('') {
       sh 'mvn exec:java -DskipTests'
      }
    } catch (error) {
    } finally {
      // Stop and remove database container here
      sh 'docker stop primaticsfinancial2017/cecl-poc-fileupload:${env.BUILD_NUMBER}'
    }
  }

  stage('Run Tests') {
    try {
      dir('') {
        sh "mvn test"
        docker.build("primaticsfinancial2017/cecl-poc-fileupload:${env.BUILD_NUMBER}").push()
      }
    } catch (error) {

    } finally {
      junit '**/target/surefire-reports/*.xml'
    }
  }
}