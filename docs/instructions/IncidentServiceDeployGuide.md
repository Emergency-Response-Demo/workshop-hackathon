# Incident Service Deploy Guide
## Building the Incident Service Image
There are many ways to deploy an application into OpenShift. The most common methods are:
* s2i pointing to a git repository
* using the fabric8 module
* pipelines

Feel free to investigate the different options. The following text will describe how to use s2i combined with a local filesystem. This option is simple and allows you to easily discover what is done under the hood of OpenShift.

First change to the project containing the emergency response project.
```
oc project emergency-response-demo
```


Then create an imagestream, using the following command
```
echo 'apiVersion: v1
kind: ImageStream
metadata:
  labels:
    application: incident-service-<insert your alias>
  name: incident-service-<insert your alias>'|oc create -f -
```
An image stream is an abstraction over one or more container images, where you can provide informations about registry locations, labels, etc.

Next you need a build config, which will tell OpenShift which build strategy, labels and so on you want for your application.
```
echo 'kind: "BuildConfig"
apiVersion: "v1"
metadata:
  name: "incident-service-build" 
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
      name: "incident-service-<insert your alias>:latest"'| oc create -f -
```
In this case, we will use a Java8 s2i image, and the output of the build will be output to your newly created image stream.

Now you are ready to build your application, using s2i. From _$WORKING_DIR_, simply run the command
```
oc start-build incident-service-build --from-file impl/target/incident-service-fuse-impl-1.0.0-SNAPSHOT.jar
```
This will trigger your build. You can investigate how the build is doing by running
```
$ oc get build
NAME                       TYPE      FROM             STATUS     STARTED        DURATION
incident-service-build-1   Source    Binary@5206ef1   Complete   11 hours ago   4m55s
```
which will list the current builds and their status. To investigate the log wile the build is running, use
```
oc logs build/incident-service-build-1 -f
```
Replace the name of the build with the one you got running _oc get build_. Once the build has finished successfully, you are ready to exchange the existing implementation of the Incident Service with your implementation.

If you want to know more about build strategies, please refer to the section [Builds and Image Streams](https://docs.openshift.com/container-platform/3.11/architecture/core_concepts/builds_and_image_streams.html) in the documentation.

## Replacing the Incident Service Image with your implementation
If you have implemented the health check service endpoints, you can skip directly to [Replacing the Incident Service](#replacing-the-incident-service). Otherwise read on
### Removing health checks
Login to the OpenShift console, select projects and click on the project named _emergency-response-demo_. Next from the menu on the left select Workloads -> DeploymentConfigs. Then click on incident-service. OpenShift will make a new deployment of the incident service each time any change is made to the configuration. You really want to avoid that at the moment, since you need to make several changes.

Therefore as a first action you select _Pause Rollouts_ in the _Actions_ menu.
![image of pausing rollouts](../assets/pause_rollouts.png)

With that in place, let's make the necessary changes. First you can disable the health checks. Be aware that this will have the effect that the container is listed as ready as soon as your application is spun up. It will only be listed as failing if your application is crashing with a system exit.

Click _Edit Deployment Config_ in the _Actions_ menu.
![image of editing health checks](../assets/edit_health_checks.png)

In the YAML editor find elements readinessProbe and livenessProbe and remove them. Ignorance is a bliss ;) 'Save' to confirm the changes to the configuration and then 'Reload' to see your changes.

![image of removing health checks](../assets/remove_readiness_probe.png)

### Replacing the Incident Service
Now it's time to replace the incident service implementation.

Find out the image stream definition, is can be something like this:

```
		from:
		  kind: ImageStreamTag
		  namespace: emergency-response-demo
		  name: 'incident-service:1.0.0.Final'
```

Replace 'incident-service:1.0.0.Final' with your 'image-stream-name:latest' and click _Save_ to confirm your changes.

If you paused the rollouts earlier, now is the time to resume to see the effect of your hard work. To do so click _Resume Rollouts_ in the _Actions_ menu.
![image of resuming rollouts](../assets/resume_rollouts.png).

### Monitoring the Incident Service
Now you can monitor the rollout of the Incident Service. Click on the _Overview_ menu item and see the beauty of OpenShift combined with your code unfold.
![image of resuming rollouts](../assets/see_application_rollout.png).

 Under incident-service you will see your application being deployed. Once the grey ring becomes solid blue, your application is ready for testing; though you might want to inspect the log of the pod if you didn't implement health checks.

 ## Handling configuration
 The incident service comes configured already. All configurations come in a config map named incident-service. You can see the content of the file by invoking the command
 ```
oc get cm incident-service -o yaml
```
which will show you the configuration of the service.

Depending on the need of your application, you can either inject this config map as a file or as system properties. Refer to [the documentation on configmaps](https://access.redhat.com/documentation/en-us/openshift_container_platform/3.11/html/developer_guide/dev-guide-configmaps) for more details.

If you want to edit the deployment config by hand, you can use
```
oc edit dc incident-service
```
to do so. Better take a backup of your config before changing it
```
oc get dc incident-service -o yaml > incident-service.yaml
```
