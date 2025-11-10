pipeline {
  agent any
  options {
    timestamps()
    disableConcurrentBuilds()
  }
  environment {
    GH_TOKEN_CRED = 'github-token'
    DOCKER_CRED   = 'docker-registry'
    REGISTRY      = 'docker.io'
    IMAGE_REPO    = 'your-docker-user/kms'
    GIT_EMAIL     = 'ci-bot@example.com'
    GIT_NAME      = 'ci-bot'
    MICRO_ROOT    = 'microservices'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
        sh 'git fetch --all --prune'
      }
    }

    stage('Detect changed services') {
      steps {
        script {
          sh 'chmod +x ci/changed-services.sh || true'
          def baseRef = "origin/${env.BRANCH_NAME}"
          def out = sh(script: "ci/changed-services.sh ${env.MICRO_ROOT} ${baseRef} HEAD || true", returnStdout: true).trim()
          echo out ? "Changed services:\n${out}" : "No changed services."
          env.CHANGED = out
        }
      }
    }

    stage('Unit tests (changed)') {
      when { allOf { branch 'dev'; expression { env.CHANGED?.trim() } } }
      steps {
        script {
          def svcs = env.CHANGED.split("\\r?\\n") as List
          def branches = [:]
          for (svc in svcs) {
            branches[svc] = {
              dir("${env.MICRO_ROOT}/${svc}") {
                sh 'chmod +x ./gradlew || true'
                sh './gradlew --no-daemon clean test'
              }
            }
          }
          parallel branches
        }
      }
    }

    stage('Build & push images (changed)') {
      when { allOf { branch 'dev'; expression { env.CHANGED?.trim() } } }
      steps {
        script {
          withCredentials([usernamePassword(credentialsId: env.DOCKER_CRED, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
            sh 'echo "$DOCKER_PASS" | docker login ${REGISTRY} -u "$DOCKER_USER" --password-stdin'
            sh 'chmod +x ci/docker-build.sh || true'
            def sha = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
            def svcs = env.CHANGED.split("\\r?\\n") as List
            for (svc in svcs) {
              sh """
                IMAGE_REPO='${IMAGE_REPO}' TAG='latest' GIT_SHA='${sha}' MICRO_ROOT='${MICRO_ROOT}' ci/docker-build.sh '${svc}'
                docker push ${IMAGE_REPO}-${svc}:latest
                docker push ${IMAGE_REPO}-${svc}:${sha}
              """
            }
          }
        }
      }
    }

    stage('Promote dev → test') {
      when { branch 'dev' }
      steps {
        script {
          withCredentials([string(credentialsId: env.GH_TOKEN_CRED, variable: 'GH_TOKEN')]) {
            sh """
              git config user.email '${GIT_EMAIL}'
              git config user.name  '${GIT_NAME}'
              git checkout dev
              git pull origin dev
              git checkout test || git checkout -b test
              git pull origin test || true
              # merge bez fast-forward, commit z [skip ci] by nie nakręcać pętli
              git merge --no-ff dev -m 'ci: promote dev → test [skip ci]'
              # push z tokenem
              REPO="\$(git config --get remote.origin.url | sed -E 's#(git@|https?://)github.com[:/](.*)#\\2#' | sed 's/.git\$//')"
              git push "https://x-access-token:${GH_TOKEN}@github.com/\${REPO}" HEAD:test
            """
          }
        }
      }
    }

    stage('Full test suite on test') {
      when { branch 'test' }
      steps {
        sh """
          # rootowy gradle, jeśli masz; jeśli nie – odpalimy wszystkie serwisy po kolei
          if [ -f ./gradlew ]; then
            chmod +x ./gradlew
            ./gradlew --no-daemon clean test
            # ./gradlew integrationTest || true   # odkomentuj jeśli istnieje task
          else
            echo "Root gradle not found – running per service"
            for d in ${MICRO_ROOT}/*-service ; do
              [ -d "$d" ] || continue
              (cd "$d" && chmod +x ./gradlew || true && ./gradlew --no-daemon clean test || exit 1)
            done
          fi
        """
      }
    }

    stage('Promote test → master') {
      when { branch 'test' }
      steps {
        script {
          withCredentials([string(credentialsId: env.GH_TOKEN_CRED, variable: 'GH_TOKEN')]) {
            sh """
              git config user.email '${GIT_EMAIL}'
              git config user.name  '${GIT_NAME}'
              git checkout test
              git pull origin test
              git checkout master || git checkout -b master
              git pull origin master || true
              git merge --no-ff test -m 'ci: promote test → master [skip ci]'
              REPO="\$(git config --get remote.origin.url | sed -E 's#(git@|https?://)github.com[:/](.*)#\\2#' | sed 's/.git\$//')"
              git push "https://x-access-token:${GH_TOKEN}@github.com/\${REPO}" HEAD:master
            """
          }
        }
      }
    }

    stage('Master: build & push all images') {
      when { branch 'master' }
      steps {
        script {
          withCredentials([usernamePassword(credentialsId: env.DOCKER_CRED, usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
            sh 'echo "$DOCKER_PASS" | docker login ${REGISTRY} -u "$DOCKER_USER" --password-stdin'
            sh 'chmod +x ci/docker-build.sh ci/changed-services.sh || true'
            def sha = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
            def all = sh(script: "ci/changed-services.sh ${env.MICRO_ROOT} ALL", returnStdout: true).trim().split("\\r?\\n") as List
            for (svc in all) {
              sh """
                IMAGE_REPO='${IMAGE_REPO}' TAG='latest' GIT_SHA='${sha}' MICRO_ROOT='${MICRO_ROOT}' ci/docker-build.sh '${svc}'
                docker push ${IMAGE_REPO}-${svc}:latest
                docker push ${IMAGE_REPO}-${svc}:${sha}
              """
            }
          }
        }
      }
    }
  }

  post {
    failure { echo "Build failed on ${env.BRANCH_NAME}" }
  }
}
