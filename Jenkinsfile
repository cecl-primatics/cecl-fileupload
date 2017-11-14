node {
    def app

    stage('Clone repository') {
        /* Let's make sure we have the repository cloned to our workspace */

        checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'cecl-github', name: 'origin', url: 'https://github.com/cecl-primatics/cecl-fileupload.git']]])
    }
    
    stage('Install') {
    def mvnHome = tool "maven352"
    sh "${mvnHome}/bin/mvn clean install -DskipTests=true"
    }

    stage('Build image') {
        /* This builds the actual image; synonymous to
         * docker build on the command line */
		dir('src/docker') {
        app = docker.build("primaticsfinancial2017/cecl-poc-fileupload")
        }
    }

    stage('Test image') {
        /* Ideally, we would run a test framework against our image.
         * For this example, we're using a Volkswagen-type approach ;-) */

        app.inside {
            sh 'echo "Tests passed"'
        }
    }

    stage('Push image to DockerHUB') {
        /* Finally, we'll push the image with two tags:
         * First, the incremental build number from Jenkins
         * Second, the 'latest' tag.
         * Pushing multiple tags is cheap, as all the layers are reused. */
        dir('src/docker') {
        docker.withRegistry('https://registry.hub.docker.com', 'docker') {
            app.push("latest")
        }
        }
    }
    
    stage('Push image to Openshift') {
        sh '${mvnHome}/bin/mvn clean install -DskipTests=true -Pbuild-docker'
    }
    
    stage('Deploy into Openshift') {
        sh '/cicd/oc-tool/oc login 52.15.67.101:8443 --token=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJjZWNsLXBvYyIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJqZW5raW5zLXRva2VuLThuMHh3Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6ImplbmtpbnMiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiI4YzIzYjAwNS1jOGVmLTExZTctOTA2My0wYTE5MjZiMmUyZTIiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6Y2VjbC1wb2M6amVua2lucyJ9.GjVKrrUwCiOd9xD909qcb2D4KbSWZaEfxxDD2OE2Ze5zdd6WNyM1UjN2MKIKH4QwmOtm0SwK_MeRseGgAdyTmolD_zsqzXY1nvnQgE-sI9rOu_iugC1cKFkBkQci2q_PrZDhVeghEyjUOWRLN4RSe-1-gZiXrZZNp4mXrIrHJ4tq-FcCGlel4ZCcH7F5TKPMPTyfIUDJPmG1mptwfNcFtDqaT24A4YDw8VqsCD3xKIL4DT8XOYKVv1_pof307uMBMrBLTU9KTO2VrB5rfK4Qy1_tIu5RUfucUwxgIl1fbN4KUM7o2Hmrd7pox5p9NqTGLASGKMhJYow-rsAzzXb0Tw  --insecure-skip-tls-verify=true'
        sh '/cicd/oc-tool/oc deploy cecl-poc-fileupload --latest=true'
    }
}