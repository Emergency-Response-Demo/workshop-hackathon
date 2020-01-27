---
To run
```
mvn clean install
```
Then
```
java -jar impl/target/incident-service-fuse-impl-1.0.0-SNAPSHOT.jar
```
For a description of the rationale behind the project structure and why it is as it is, please refer to ./docs/blog.adoc.

The swagger definition stub/src/spec/beer-catalog-API.json is taken from https://github.com/microcks/api-lifecycle and is the work of Laurent Broudoux & Nicolas Masse. It can be used under the license agreement stated on their git repo.

Starting kafka locally on a mac (taken from https://medium.com/@zengcode/apache-kafka-apache-camel-c9996a07d2f3)
```
brew search kafka
brew install kafka
zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties & kafka-server-start /usr/local/etc/kafka/server.properties
kafka:
    server : localhost
    port : 9092
    topic : my-topic
    channel : my-chanel
kafka-topics --create --zookeeper localhost:2181 --topic my-topic --partitions 10 --replication-factor 1
kafka-topics --delete --zookeeper localhost:2181 --topic my-topic
```
more on camel and kafka https://camel.apache.org/components/latest/kafka-component.html
