node {
    env.JAVA_HOME="${tool 'jdk8'}"

    stage 'Checkout'
    git url: 'https://github.com/rama-arun/cecl-fileupload.git', branch: 'master'
    
    stage 'Build'
    sh "${mvnHome}/bin/mvn clean install"
    
    stage 'Test'
    sh "${mvnHome}/bin/mvn test"
    
    stage('SonarQube analysis') {
    def scannerHome = tool 'sonarqube';
    withSonarQubeEnv('sonarqube') {
      sh "${scannerHome}/bin/sonar-scanner"
    	}
  	}
  	
  	stage 'Build Docker image'
    def image = docker.build('primaticsfinancial2017/cecl-fileupload-service:latest', '.')
    
    stage 'Acceptance Tests'
    image.withRun('-p 8081:8081') {c ->
        sh "${mvnHome}/bin/mvn verify"
    }
    
    stage 'Push image'
    docker.withRegistry("https://index.docker.io/v1/", "docker") {
        image.push()
    }
    
}