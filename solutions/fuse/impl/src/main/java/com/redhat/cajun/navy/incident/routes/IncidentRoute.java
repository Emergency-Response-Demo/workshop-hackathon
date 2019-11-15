package com.redhat.cajun.navy.incident.routes;

import com.redhat.cajun.navy.incident.entity.IncidentBuilder;
import org.apache.camel.Processor;
import org.apache.camel.Exchange;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;
import com.redhat.cajun.navy.incident.message.IncidentReportedEvent;
import com.redhat.cajun.navy.incident.message.Message;
import java.math.BigDecimal;
import org.apache.camel.model.rest.RestBindingMode;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Map;

import com.redhat.cajun.navy.incident.entity.Incident;

@Component
public class IncidentRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        restConfiguration().component("servlet")
            .bindingMode(RestBindingMode.json)
            .skipBindingOnErrorCode(false);

        from("direct:getIncidents")
            .to("sql:select * from incident")
            .process(new SQLToIncidentMapper(false));

        from("direct:getIncidentByStatus")
            .to("sql:select * from incident where status = :#${header['status']}")
            .process(new SQLToIncidentMapper(false));

        from("direct:getIncidentsByVictimName")
            .to("sql:select * from incident where victim_name = :#${header['name']}")
            .process(new SQLToIncidentMapper(false));

        from("direct:getIncident")
            .to("sql:select * from incident where incident_id = :#${header['incidentId']}")
            .process(new SQLToIncidentMapper(true));

        from("direct:createIncident")
            .streamCaching()
            .log("received message was: ${body}")
            //write incident to database
            .to("sql:classpath:sql/insert_incident.sql")
            //prepare message for Kafka event
            .process(new Processor(){
                @Override
                public void process(Exchange exchange) throws Exception {
                    Map incidentValues = (Map) exchange.getIn().getBody();
                    //report that a new incident has been recorded.
                    Message<IncidentReportedEvent> message = new Message.Builder<>("IncidentReportedEvent", "IncidentService",
                    new IncidentReportedEvent.Builder(incidentValues.get("incidentId").toString())
                            .lat(new BigDecimal(incidentValues.get("latitude").toString()))
                            .lon(new BigDecimal(incidentValues.get("longitude").toString()))
                            .medicalNeeded(new Boolean(incidentValues.get("medicalNeeded").toString()))
                            .numberOfPeople(new Integer(incidentValues.get("numberOfPeople").toString()))
                            .timestamp(new Long(incidentValues.get("reportedTime").toString()))
                            .build())
                    .build();
                    exchange.getIn().setBody(message);
                }
            })
            //send to Kafka
            .marshal().json(JsonLibrary.Jackson)
            .to("{{sender.destination.incident-reported-event}}");
            
    }
}
