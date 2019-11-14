# Incident API spec

Any implementation of the Incident Service must comply with the following guidelines in order to be fully operationable.

![high level description of the incident service](resources/incident_highlevel.png)

* The service exposes a rest API as specified in the [OpenAPI specification](https://raw.githubusercontent.com/Emergency-Response-Demo/incident-service/master/openapi.json). Any implementation must comply with this interface.
* All incidents received must be persisted and returned on later calls.
* There are no requirements on specific storage solutions, but keep in mind that we work with microservices, so solutions which enable moving workloads easily are preferred.
* The incident service must notify other services on any incidents received. This is done by posting to a Kafka Topic.
* The incident service must listen on a Kafka topic to get any updates to an incident.

```
example of UpdateIncidentCommand:
{
  "id":"b23a343c-2f2d-471c-baf1-e245b2a12ee8",
  "messageType":"IncidentReportedEvent",
  "invokingService":"IncidentService",
  "timestamp":1573743054009,
  "body":
  {
    "incident":
    {
      "id": 1
      "incidentId":"1111",
      "latitude":444,
      "longitude":444,
      "numberOfPeople":12,
      "medicalNeeded":true,
      "reportedTime":1573464689
    }
  }
}

{"id":"b23a343c-2f2d-471c-baf1-e245b2a12ee8","messageType":"IncidentReportedEvent","invokingService":"IncidentService","timestamp":1573743054009,"body":{"incident":{"id": 1,"incidentId":"1111","latitude":999,"longitude":444,"numberOfPeople":12,"medicalNeeded":true,"victimPhoneNumber": "444","incidentId": "1111","reportedTime":1573464689, "reportedTime": "1573464689", "version": 1, "status": "REQUESTED"}}}
```

[//]: # "TOOD describe the Kafka topic interface. (topic-incident-command, topic-incident-event)"
