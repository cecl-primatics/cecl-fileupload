node {
    env.JAVA_HOME="${tool 'jdk8'}"

    stage 'Build'
    git url: 'https://github.com/rama-arun/cecl-fileupload.git', branch: 'master'
    
    def mvnHome = tool "maven352"
    sh "${mvnHome}/bin/mvn clean install"
    step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
}