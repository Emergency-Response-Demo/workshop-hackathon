package com.redhat.cajun.navy.incident.listener;

import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import java.util.LinkedHashMap;

@Component
public class UpdateIncidentListener extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("{{listener.destination.update-incident-command}}")
        .streamCaching()
        .log(LoggingLevel.INFO, "Read message from Kafka body is: ${body}")
        .unmarshal().json(JsonLibrary.Jackson, LinkedHashMap.class)
        .log("updating incident with id: ${body['body']['incident']['id']}")
        .to("sql:classpath:sql/update_incident.sql")
        .log("incident update recorded");            
    }
}
