package com.redhat.cajun.navy.incident.entity;

import java.math.BigDecimal;

import com.redhat.cajun.navy.incident.entity.IncidentReportedEvent;

public class IncidentReportedEventMessageBuilder {

    private IncidentReportedEventMessage ire;

    public IncidentReportedEventMessageBuilder(String id, String invokingService) {
        ire = new IncidentReportedEventMessage();
        ire.setId(id);
        ire.setBody(new IncidentReportedEvent());
        ire.getBody().setId(id);
        ire.setInvokingService(invokingService);
        ire.setMessageType("IncidentReportedEvent");
    }

    public IncidentReportedEventMessageBuilder lat(BigDecimal lat) {
        ire.getBody().setLat(lat);
        return this;
    }

    public IncidentReportedEventMessageBuilder lon(BigDecimal lon) {
        ire.getBody().setLon(lon);
        return this;
    }

    public IncidentReportedEventMessageBuilder numberOfPeople(int numberOfPeople) {
        ire.getBody().setNumberOfPeople(numberOfPeople);
        return this;
    }

    public IncidentReportedEventMessageBuilder medicalNeeded(boolean medicalNeeded) {
        ire.getBody().setMedicalNeeded(medicalNeeded);
        return this;
    }

    public IncidentReportedEventMessageBuilder timestamp(long timestamp) {
        ire.setTimestamp(timestamp);
        ire.getBody().setTimestamp(timestamp);
        return this;
    }

    public IncidentReportedEventMessage build() {
        return ire;
    }
}
