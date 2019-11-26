Incident Service
=========

This is the quarkus version for the incident service. 
It uses openapi and rest and Kafka extensions

To test out:
http://localhost:8080/swagger-ui


To deploy the service to a running single-node OpenShift cluster:

   ```
$ oc login -u developer -p developer

$ oc new-project MY_PROJECT_NAME

   ```

== Config

   ```
# Configuration file
# key = value
quarkus.http.port=8080
quarkus.swagger-ui.always-include=true
quarkus.smallrye-openapi.path=/swagger
quarkus.log.console.enable=true
quarkus.log.console.level=DEBUG
quarkus.log.level=INFO
quarkus.http.cors=true

# TODO: Add for DataGrid
quarkus.infinispan-client.server-list=datagrid-service:11222

mp.messaging.outgoing.incidentEvent.bootstrap.servers=my-cluster-kafka-bootstrap:9092
mp.messaging.outgoing.incidentEvent.connector=smallrye-kafka
mp.messaging.outgoing.incidentEvent.topic=topic-incident-event
mp.messaging.outgoing.incidentEvent.value.serializer=org.apache.kafka.common.serialization.StringSerializer
mp.messaging.outgoing.incidentEvent.key.serializer=org.apache.kafka.common.serialization.StringSerializer

mp.messaging.incoming.topic-incident-command.connector=smallrye-kafka
mp.messaging.incoming.topic-incident-command.value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.incoming.topic-incident-command.key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.incoming.topic-incident-command.bootstrap.servers=my-cluster-kafka-bootstrap:9092
mp.messaging.incoming.topic-incident-command.group.id=incident-service-quarkus
mp.messaging.incoming.topic-incident-command.auto.offset.reset=earliest
mp.messaging.incoming.topic-incident-command.enable.auto.commit=true
mp.messaging.incoming.topic-incident-command.request.timeout.ms=30000

   ```
  
== Running locally:
   ```
# Make sure Kafka is running e.g 
bin/zookeeper-server-start.sh config/zookeeper.properties
bin/kafka-server-start.sh config/server.properties

# Creating the topic
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic topic-incident-event

# Listening to the topic on the console
bin/kafka-console-consumer.sh --topic topic-incident-event --bootstrap-server localhost:2181 --from-beginning


   ```


== Running on Openshift:
Finally run the following on localmachine while connected to openshift.

```

./mvnw package -Pnative
./mvnw package -Pnative -Dquarkus.native.container-build=true
OR
./mvnw package -Pnative -Dquarkus.native.container-runtime=Docker

docker build -f src/main/docker/Dockerfile.native -t incident-service .

oc new-build --binary --name=incident-service -l app=incident-service
oc patch bc/incident-service -p '{"spec":{"strategy":{"dockerStrategy":{"dockerfilePath":"src/main/docker/Dockerfile.native"}}}}'
oc start-build incident-service --from-dir=. --follow
oc new-app --image-stream=incident-service:latest
oc expose svc/incident-service

 ```
 
 Deploy uber jar
 ```
echo 'apiVersion: v1
kind: ImageStream
metadata:
  labels:
    application: incident-service-<initials>
  name: incident-service-<initials>'|oc create -f -

echo 'kind: "BuildConfig"
apiVersion: "v1"
metadata:
  name: "incident-service-build-<initials>"
spec:
  runPolicy: "Serial" 
  strategy: 
    sourceStrategy:
      from:
        kind: "ImageStreamTag"
        name: "java:8"
        namespace: openshift
  output: 
    to:
      kind: "ImageStreamTag"
      name: "incident-service-<initials>:latest"'| oc create -f -

mvn clean package -DuberJar -Dmaven.test.skip=true

oc start-build incident-service-build-<initials> --from-file target/incident-service-1.0.0-SNAPSHOT-runner.jar
```

 == Building to Native in OpenShift with S2I:
Inspired by <https://quarkus.io/guides/deploying-to-openshift-s2i>

```

 # To build the image on OpenShift
oc new-app quay.io/quarkus/ubi-quarkus-native-s2i:19.2.1~https://github.com/josequaresma/workshop-hackathon.git --context-dir=solutions/quarkus/incident-service --name=incident-service-native

# Build from command above will fail due to lack of memory, so we extend that and re-run the build
oc patch bc/incident-service-native -p '{"spec":{"resources":{"limits":{"cpu":"4", "memory":"6Gi"}}}}'
oc start-build incident-service-native --follow

# To create the route
oc expose svc/incident-service-native

# Get the route URL
export URL="http://$(oc get route | grep incident-service-native | awk '{print $2}')"
echo $URL
curl $URL/hello/greeting/quarkus

```
