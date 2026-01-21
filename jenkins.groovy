def checkDeploymentCompletion(){
    sh('kubectl rollout status deployment/blaybus-app -n blaybus')
}

node {
    stage ('Clone') {
        echo "깃 클론 시작"
        git branch: "main", url:'https://github.com/2026-blaybus-hackathon/BACKEND.git'
        echo "깃 클론 끝"
    }
    stage('Build & Push image') {
        echo "빌드 시작"
        withCredentials([usernamePassword(credentialsId: 'docker-hub', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
            sh("./gradlew jib")
        }
        echo "빌드 끝"
    }
    stage ('Deploy'){
        echo "배포 시작"
        if(params.DEPLOY_ACTION == 'apply manifest') {
            sh('kubectl apply -f deployment -n blaybus')
            checkDeploymentCompletion()
        } else if(params.DEPLOY_ACTION == 'rollout restart') {
            sh('kubectl rollout restart deployment/blaybus-app -n blaybus')
            checkDeploymentCompletion()
        }
        echo "배포 종료"
    }
}
