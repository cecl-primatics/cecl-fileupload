node {
    env.JAVA_HOME="${tool 'jdk8'}"

    stage 'Checkout'
    git url: 'https://github.com/rama-arun/cecl-fileupload.git', branch: 'master'
    
    stage 'Build'
    def mvnHome = tool "maven352"
    sh "${mvnHome}/bin/mvn clean install"
    
    stage 'Test'
    sh "${mvnHome}/bin/mvn test"
    
    stage 'SonarQube analysis'
    sh "${mvnHome}/bin/mvn sonar:sonar"
  	
  	stage 'Build Docker image'
    sh "${mvnHome}/bin/mvn package -Pbuild-docker"
    
    stage 'Acceptance Tests'
    image.withRun('-p 8081:8081') {c ->
        sh "${mvnHome}/bin/mvn verify"
    }
    
    stage 'Push image'
    docker.withRegistry("https://index.docker.io/v1/", "docker") {
        image.push()
    }
    
}