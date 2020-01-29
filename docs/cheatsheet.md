Deploying to OpenShift
====

Source to Image (s2i)
====

Example build:

```bash
oc new-build java:8~https://github.com/tfriman/workshop-hackathon --context-dir=solutions/vertx/incident-service --name=vertx-incident-service --labels=app=vertx-incident-service
```

Easiest is to run this on `emergency-response-demo` project/namespace and then tweak the incident service deployment config to use 'vertx-incident-service:latest' as input image stream.

Build pipelines
====
Example build pipeline build config, that will take source from git and push the changes to main `emergency-response-demo` project/namespace

```
apiVersion: v1
kind: BuildConfig
metadata:
  labels:
    build: {{ pipeline_buildconfig_name }}
  name: {{ pipeline_buildconfig_name }}
  annotations:
spec:
  runPolicy: Serial
  strategy:
    jenkinsPipelineStrategy:
      jenkinsfile: |-
        def git_repo = "{{ incident_service_git_repo }}"
        def git_branch = "{{ incident_service_git_branch }}"
        def version = ""
        def groupId = ""
        def artifactId = ""
        def commitId = ""
        def namespace_jenkins = "{{ namespace_tools }}"
        def namespace_app = "{{ namespace }}"
        def app_build = "{{ buildconfig_name }}"
        def app_imagestream = "{{ imagestream_name }}"
        def app_name = "{{ application_name }}"

        node ('maven-with-nexus') {
          stage ('Compile') {
            echo "Starting build"
            git url: "${git_repo}", branch: "${git_branch}"
            def pom = readMavenPom file: 'pom.xml'
            version = pom.version
            groupId = pom.groupId
            artifactId = pom.artifactId
            def commitIdFull = sh( script: 'git rev-parse HEAD', returnStdout: true )
            commitId = commitIdFull[0..8]
            echo "Building version ${version}, commitId ${commitId}"
            sh "mvn clean compile -Dcom.redhat.xpaas.repo.redhatga=true"
          }

          stage ('Unit Tests') {
            sh "mvn test -Dcom.redhat.xpaas.repo.redhatga=true"
          }

          stage ('Build Application') {
            sh "mvn package -DskipTests=true -Dcom.redhat.xpaas.repo.redhatga=true"
          }

          stage ('Maven Integration Tests') {
            sh "mvn integration-test -DskipUTs=true -Dcom.redhat.xpaas.repo.redhatga=true"
          }

          stage ('Build Image') {
            openshift.withCluster() { // Use "default" cluster or fallback to OpenShift cluster detection
              def bc = openshift.selector("bc", "${app_build}")
              def builds = bc.startBuild("--from-file=target/${artifactId}-${version}.jar")
              timeout (15) {
                builds.watch {
                  if ( it.count() == 0 ) {
                    return false
                  }
                  // Print out the build's name and terminate the watch
                  echo "Detected new builds created by buildconfig: ${it.names()}"
                  return true
                }
                while (builds.object().status.phase != "Complete") {
                  echo "${builds.names()} ;  status = ${builds.object().status.phase}"
                  sleep 5
                }
              }
            }
          }

          stage ('Deploy') {
            openshift.withCluster() {
              openshift.withProject( "${namespace_app}") {
                openshift.tag("${namespace_jenkins}/${app_imagestream}:latest", "${namespace_app}/${app_imagestream}:latest")
                openshift.tag("${namespace_jenkins}/${app_imagestream}:latest", "${namespace_jenkins}/${app_imagestream}:${commitId}")
                openshift.tag("${namespace_app}/${app_imagestream}:latest", "${namespace_app}/${app_imagestream}:${commitId}")
                def dc_app = openshift.selector("dc", "${app_name}")
                timeout (5) {
                  dc_app.untilEach(1) {
                    return it.object().status.readyReplicas == 1
                  }
                }
              }
            }
          }
        }
    type: JenkinsPipeline
  triggers:
  - github:
      secret: {{ github_secret }}
    type: GitHub
  - generic:
      secret: {{ generic_secret }}
    type: Generic


```