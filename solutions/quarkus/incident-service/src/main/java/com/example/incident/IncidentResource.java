package com.example.incident;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletionStage;

import com.example.incident.message.IncidentReportedEvent;
import com.example.incident.message.Message;
import com.example.incident.message.UpdateIncidentCommand;
import io.smallrye.reactive.messaging.kafka.KafkaMessage;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.StartupEvent;

import com.example.incident.model.Incident;


@Path("/incidents")
public class IncidentResource {

    private Map<String, Incident> incidents = new LinkedHashMap<>();

    private final Logger logger = LoggerFactory.getLogger(IncidentResource.class.getName());

    /*
    @ConfigProperty(name = "mp.messaging.outgoing.incidentEvent.bootstrap.servers")
    public String bootstrapServers;

    @ConfigProperty(name = "mp.messaging.outgoing.incidentEvent.topic")
    public String incidentEvent;

    @ConfigProperty(name = "mp.messaging.outgoing.incidentEvent.value.serializer")
    public String incidentEventTopicValueSerializer;

    @ConfigProperty(name = "mp.messaging.outgoing.incidentEvent.key.serializer")
    public String incidentEventTopicKeySerializer;

    private Producer<String, String> producer;
*/

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    @Operation(summary = "get all")
    public String getIncidents() {
        return Json.encodePrettily(incidents.values());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{status}")
    @Operation(summary = "get by status")
    public String getStatus(@PathParam("status") String status) {
        Optional<String> optional = incidents.entrySet().stream()
                .filter(e -> e.getValue().getStatus().equalsIgnoreCase(status))
                .map(Map.Entry::getKey)
                .findAny();
        return Json.encodePrettily(optional);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/incident/{id}")
    @Operation(summary = "get by Id")
    public String getIncidentById(@PathParam("id") String id) {
        if(incidents.containsKey(id))
            return Json.encodePrettily(incidents.get(id));
        else return Json.encode("");
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/victim/byname/{name}")
    @Operation(summary = "get victim by name")
    public String getIncidentByVictimName(@PathParam("name") String name) {

        Optional<String> optional = incidents.entrySet().stream()
                .filter(e -> e.getValue().getVictimName().matches("*"+name+"*"))
                .map(Map.Entry::getKey)
                .findAny();

        return Json.encodePrettily(optional);
    }

    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Add a new Incident")
    public Incident add(Incident incident) throws Exception {
        logger.info(incident.toString());
        incidents.put(incident.getId(), incident);
        //sendIncidentEvent(incident);
        return incident;
    }

    @POST
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "reset the incidents")
    public void reset() throws Exception {
        incidents = new LinkedHashMap<>();
    }


/*
    private void sendIncidentEvent(Incident incident) {
        Message<IncidentReportedEvent> message = new com.example.incident.message.Message.Builder<>("IncidentReportedEvent", "IncidentService",
                new IncidentReportedEvent.Builder(incident.getId())
                        .lat(new BigDecimal(incident.getLat()))
                        .lon(new BigDecimal(incident.getLon()))
                        .medicalNeeded(incident.isMedicalNeeded())
                        .numberOfPeople(incident.getNumberOfPeople())
                        .timestamp(incident.getTimestamp())
                        .build())
                .build();
        //producer.send(new ProducerRecord<String, String>(incidentEvent, incident.getId(), message.toString()));
        logger.info("Sent message: " + message);
    }

    @Incoming("topic-incident-command")
    public CompletionStage<Void> onMessage(KafkaMessage<String, String> message)
            throws IOException {

        logger.info("Kafka topic-incident-command message with value = {} arrived", message.getPayload());
        Incident i = Json.decodeValue(String.valueOf(message.getPayload()), UpdateIncidentCommand.class).getIncident();
        incidents.replace(i.getId(),i);

        return message.ack();
    }

    public void init(@Observes StartupEvent ev) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("value.serializer", incidentEventTopicValueSerializer);
        props.put("key.serializer", incidentEventTopicKeySerializer);
        producer = new KafkaProducer<String, String>(props);
    }

*/

}