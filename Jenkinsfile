node {
    env.JAVA_HOME="${tool 'jdk8'}"

    stage 'Checkout'
    git url: 'https://github.com/cecl-primatics/cecl-fileupload.git', branch: 'master'
    
    stage 'Build'
    def mvnHome = tool "maven352"
    sh "${mvnHome}/bin/mvn clean install"
    
    stage 'Test'
    sh "${mvnHome}/bin/mvn test"
    
    stage 'SonarQube analysis'
    sh "${mvnHome}/bin/mvn sonar:sonar"
  	
  	stage 'Build Docker image'
    sh "${mvnHome}/bin/mvn clean package -Pbuild-docker"
    
    stage('Push Image') {
    try {
      dir('src/docker') {
        docker.build("primaticsfinancial2017/cecl-poc-fileupload:${env.BUILD_NUMBER}").push()
      }
    } catch (error) {

    }
  }
    
}