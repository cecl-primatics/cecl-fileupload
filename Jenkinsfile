node {
  checkout scm
  env.PATH = "${tool 'maven352'}/bin:${env.PATH}"
  stage('Package') {
    dir('webapp') {
      sh 'mvn clean package -DskipTests'
    }
  }

  stage('Create Docker Image') {
    dir('webapp') {
      docker.build("primaticsfinancial/cecl-poc-fileupload:${env.BUILD_NUMBER}")
    }
  }

  stage ('Run Application') {
    try {
      sh "docker run primaticsfinancial/cecl-poc-fileupload:${env.BUILD_NUMBER}"

      // Run tests using Maven
      //dir ('webapp') {
      //  sh 'mvn exec:java -DskipTests'
      //}
    } catch (error) {
    } finally {
      // Stop and remove database container here
      //sh 'docker-compose stop db'
      //sh 'docker-compose rm db'
    }
  }

  stage('Run Tests') {
    try {
      dir('webapp') {
        sh "mvn test"
        docker.build("primaticsfinancial/cecl-poc-fileupload:${env.BUILD_NUMBER}").push()
      }
    } catch (error) {

    } finally {
      junit '**/target/surefire-reports/*.xml'
    }
  }
}