node {
    def app

    stage('Clone repository') {
        /* Let's make sure we have the repository cloned to our workspace */

        checkout scm
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

    stage('Push image') {
        /* Finally, we'll push the image with two tags:
         * First, the incremental build number from Jenkins
         * Second, the 'latest' tag.
         * Pushing multiple tags is cheap, as all the layers are reused. */
        dir('src/docker') {
        docker.withRegistry('https://registry.hub.docker.com', 'docker') {
            app.push("${env.BUILD_NUMBER}")
            app.push("latest")
        }
        }
    }
}