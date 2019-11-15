package com.redhat.cajun.navy.incident.mapping;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.redhat.cajun.navy.incident.entity.Incident;

import com.redhat.cajun.navy.incident.entity.IncidentBuilder;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
  
public class SQLToIncidentMapper implements Processor {
    private boolean mapOnlyFirst;
    public SQLToIncidentMapper(boolean mapOnlyFirst) {
        this.mapOnlyFirst = mapOnlyFirst;
    }
    public void process(Exchange exchange) throws Exception {
        List<Map> incidentRows = (List<Map>) exchange.getIn().getBody();
        List<Incident> incidents = new LinkedList<Incident>();
        incidentRows.forEach(row -> {
            incidents.add(new IncidentBuilder()
                .id(row.get("incident_id").toString())
                .latitude(row.get("latitude").toString())
                .longitude(row.get("longitude").toString())
                .numberOfPeople(Integer.parseInt(row.get("number_of_people").toString()))
                .medicalNeeded(Boolean.parseBoolean(row.get("medical_needed").toString()))
                .victimName(row.get("victim_name").toString())
                .victimPhoneNumber(row.get("victim_phone_number").toString())
                .reportedTime(Long.parseLong(row.get("reported_time").toString()))
                .version(Integer.parseInt(row.get("version").toString()))
                .status(Incident.StatusEnum.valueOf(row.get("status").toString()))
                .build());
        });
        if(mapOnlyFirst)
            exchange.getIn().setBody(incidents.size()==0? new Incident():incidents.get(0));
        else
            exchange.getIn().setBody(incidents);        
    }
}
