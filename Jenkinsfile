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
  	
  	stage 'Build Docker image & Push'
    sh "${mvnHome}/bin/mvn clean package -Pbuild-docker"
    
    stage('Push Image') {
      dir('src/docker') {
        docker.build("primaticsfinancial2017/cecl-poc-fileupload:${env.BUILD_NUMBER}").push()
     }
     }
     
     stage('Deploy into Openshift') {
        sh '/cicd/oc-tool/oc login 52.15.67.101:8443 --token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJjZWNsLXBvYyIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJqZW5raW5zLXRva2VuLThuMHh3Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6ImplbmtpbnMiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiI4YzIzYjAwNS1jOGVmLTExZTctOTA2My0wYTE5MjZiMmUyZTIiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6Y2VjbC1wb2M6amVua2lucyJ9.GjVKrrUwCiOd9xD909qcb2D4KbSWZaEfxxDD2OE2Ze5zdd6WNyM1UjN2MKIKH4QwmOtm0SwK_MeRseGgAdyTmolD_zsqzXY1nvnQgE-sI9rOu_iugC1cKFkBkQci2q_PrZDhVeghEyjUOWRLN4RSe-1-gZiXrZZNp4mXrIrHJ4tq-FcCGlel4ZCcH7F5TKPMPTyfIUDJPmG1mptwfNcFtDqaT24A4YDw8VqsCD3xKIL4DT8XOYKVv1_pof307uMBMrBLTU9KTO2VrB5rfK4Qy1_tIu5RUfucUwxgIl1fbN4KUM7o2Hmrd7pox5p9NqTGLASGKMhJYow-rsAzzXb0Tw  --insecure-skip-tls-verify=true'
        sh '/cicd/oc-tool/oc deploy cecl-poc-fileupload --latest=true'
    }
    
}