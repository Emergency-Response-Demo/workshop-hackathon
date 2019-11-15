# Incident Service for the Red Hat Cajun Navy Demo 

## External dependencies
* PostgreSQL
* Kafka

## Building
* Update your Maven settings.xml to use the team Nexus server by adding the following profile:
```
    <profiles>
        <profile>
            <id>openshift</id>
            <repositories>
                <repository>
                    <id>team.nexus</id>
                    <name>Red Hat Cajun Navy Demo Team Nexus</name>
                    <releases>
                        <enabled>false</enabled>
                        <updatePolicy>always</updatePolicy>
                        <checksumPolicy>warn</checksumPolicy>
                    </releases>
                    <snapshots>
                        <enabled>true</enabled>
                        <updatePolicy>never</updatePolicy>
                        <checksumPolicy>fail</checksumPolicy>
                    </snapshots>
                    <url>https://nexus-nexus.apps.753d.openshift.opentlc.com/nexus/content/repositories/releases/</url>
                    <layout>default</layout>
                </repository>
            </repositories>
        </profile>
    </profiles>
```
* `mvn clean package -Popenshift`

## Deploying to OpenShift
1. Configure Maven to use Nexus as described above.
2. `oc login`
3. `oc project <your project>`
4. `mvn clean package fabric8:deploy -D fabric8.namespace=naps-emergency-response -Popenshift`

   The `-D fabric8-namspace` argument is optional. If left out, the fabric8:deploy plugin will deploy to the active OpenShift project.

The maven command above will do the following:
1. Maven will build the code locally, outputting a jar in the target/ directory.
2. On the first run, the fabric8 plugin will create a BuildController, DeploymentController, ImageStream, Service and Route in OpenShift.

   The BuildController defines an S2I Binary build that takes the jar as input.
  
3. The fabric8 plugin will start an OpenShift Build using the BuildController. 
4. The Build will push to the ImageStream, which will trigger the DeploymentController will trigger to deploy a Pod running the code.

## Configuration

This service pulls its Spring Boot configuration from an OpenShift ConfigMap.

The spring-cloud-starter-cubernetes-config library reads the ConfigMap and makes it available to Spring Boot. The library is specified like this in pom.xml:
```
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-kubernetes-config</artifactId>
        </dependency>
```

The name of the ConfigMap must match the value of the spring.application.name property defined in application.properties. Its content looks similar to the example below. The example also shows how to configure the database connection info:

```
apiVersion: v1
kind: ConfigMap
data:
  application.properties: |-
    ---

    spring.datasource.url=jdbc:postgresql://postgresql.naps-emergency-response.svc:5432/naps_emergency_response
    spring.datasource.username=naps
    spring.datasource.password=naps
metadata:
    name: incident-service
```

### Configuration When Running Locally

By default, spring-cloud-starter-kubernetes-config will attempt to pull in configuration from the OpenShift ConfigMap even when the 
application is run on a development machine outside of the OpenShift cluster. It will use any active `oc login` 
session to pull the values out of the cluster.

To disable that behavior and run with a locally defined configuration,
1. Create a local version of application.properties with the required properties (see the example above or the Ansible scripts that set up the real cluster).
2. Start the application like this:

```$xslt
java -jar target/responder-service-0.0.1-SNAPSHOT.jar --spring.config.location=file:./etc/application-local.properties --spring.cloud.bootstrap.location=file:./src/test/resources/bootstrap.properties
```

The command above references a boostrap config file (bootstrap.properties), which is already provided under src/test/resources. The bootstrap configuration is read before the regular configuration and disables the ConfigMap behavior by setting `spring.cloud.kubernetes.config.enabled=false`.

... or you could just run in OpenShift. :)

## Swagger 
* Swagger ui: http://localhost:8080/swagger-ui.html
* Swagger docs: http://localhost:8080/v2/api-docs