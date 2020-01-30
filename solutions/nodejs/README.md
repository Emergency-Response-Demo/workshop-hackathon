# NodeJS version of incident service

Word of caution: This is very brute force solution by a coder who is not fluent with JS. 

# How to run locally

`npm run dev`

# How to run this on OpenShift

Login to OpenShift. Change project to 'emergency-response-demo'.

````shell script
oc new-build node:10~https://github.com/tfriman/workshop-hackathon#nodejs --context-dir=solutions/nodejs --name=node-incident-service --labels=app=node-incident-service
````

And then you have to provide Kafka broker address:
````shell script
oc set env dc/incident-service KAFKA_BOOTSTRAP_ADDRESS=kafka-cluster-kafka-bootstrap.emergency-response-demo.svc:9092
````

Change dc/incident-service to use imagestream 'node-incident-service:latest'.

