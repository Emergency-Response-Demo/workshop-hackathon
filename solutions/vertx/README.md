Emergency response Mission Service 
=========

Pre-requisites: Running Kafka Cluster, a Configmap

To deploy the service to a running single-node OpenShift cluster:

   ```
$ oc login -u developer -p developer

$ oc new-project MY_PROJECT_NAME

$ mvn clean fabric8:deploy -Popenshift

   ```

== More Information
You can learn more about this booster and rest of the Eclipse Vert.x runtime in the link:http://launcher.fabric8.io/docs/vertx-runtime.html[Eclipse Vert.x Runtime Guide].

NOTE: Run the set of integration tests included with this booster using `mvn clean verify -Popenshift,openshift-it`.


Following ConfigMap needs to exist which is mounted to /deployments/config/app-config-properties via fabric8:deploy
NAME: mission-service
Key: app-config.properties

   ```
kafka.connect=kafka-cluster-kafka-bootstrap.naps-emergency-response.svc:9092
kafka.sub=topic-incident-command
kafka.pub=topic-incident-event
kafka.autocommit=true
kafka.group.id=incident-service-example
http.port=8080
   ```
   
Creating a configmap
 ```
   oc create -f configmap.yml
   
   ```
   
Finally run the following
mvn clean fabric8:deploy -Popenshift   

