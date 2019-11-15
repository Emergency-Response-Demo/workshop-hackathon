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
import org.apache.camel.component.sql.SqlConstants;
import com.redhat.cajun.navy.incident.mapping.SQLToIncidentMapper;

import java.util.ArrayList;
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
            .to("sql:select * from incident where id = :#${header['incidentId']}")
            .process(new SQLToIncidentMapper(true));

        from("direct:createIncident")
            .setProperty("body", simple("${body}"))
            .log("received message was: ${property.body}")
            //write incident to database
            .setHeader(SqlConstants.SQL_RETRIEVE_GENERATED_KEYS, constant(true))
            .setHeader(SqlConstants.SQL_GENERATED_COLUMNS, constant(new String[] {"ID"}))
            .setHeader("messageReceived", constant(System.currentTimeMillis()))
            .to("sql:classpath:sql/insert_incident.sql")
            .log("${headers}")
            //prepare message for Kafka event
            .process(new Processor(){
                @Override
                public void process(Exchange exchange) throws Exception {
                    Map incidentValues = (Map) exchange.getProperty("body");
                    ArrayList<Map> generatedColumns = 
                        (ArrayList<Map>) exchange.getIn().getHeader("CamelSqlGeneratedKeyRows");
                    //report that a new incident has been recorded.
                    Message<IncidentReportedEvent> message = new Message.Builder<>("IncidentReportedEvent", "IncidentService",
                    new IncidentReportedEvent.Builder(generatedColumns.get(0).get("ID").toString())
                            .lat(new BigDecimal(incidentValues.get("lat").toString()))
                            .lon(new BigDecimal(incidentValues.get("lon").toString()))
                            .medicalNeeded(new Boolean(incidentValues.get("medicalNeeded").toString()))
                            .numberOfPeople(new Integer(incidentValues.get("numberOfPeople").toString()))
                            .timestamp(new Long(exchange.getIn().getHeader("messageReceived").toString()))
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
