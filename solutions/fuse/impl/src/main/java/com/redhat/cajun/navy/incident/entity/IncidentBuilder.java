package com.redhat.cajun.navy.incident.entity;

public class IncidentBuilder {
    private final Incident incident;
    public IncidentBuilder() {
        incident = new Incident();
    }

    public IncidentBuilder id(String id) {
        incident.setId(id);
        return this;
    }

    public IncidentBuilder latitude(String latitude) {
        incident.setLat(latitude);
        return this;
    }

    public IncidentBuilder longitude(String longitude) {
        incident.setLon(longitude);
        return this;
    }

    public IncidentBuilder numberOfPeople(Integer numberOfPeople) {
        incident.setNumberOfPeople(numberOfPeople);
        return this;
    }

    public IncidentBuilder medicalNeeded(Boolean medicalNeeded) {
        incident.setMedicalNeeded(medicalNeeded);
        return this;
    }

    public IncidentBuilder victimName(String victimName) {
        incident.setVictimName(victimName);
        return this;
    }

    public IncidentBuilder victimPhoneNumber(String victimPhoneNumber) {
        incident.setVictimPhoneNumber(victimPhoneNumber);
        return this;
    }

    public IncidentBuilder reportedTime(long timestamp) {
        incident.setReportedTime(timestamp);
        return this;
    }

    public IncidentBuilder version(int version) {
        incident.setVersion(version);
        return this;
    }

    public IncidentBuilder status(Incident.StatusEnum status) {
        incident.setStatus(status);
        return this;
    }

    public Incident build() {
        return incident;
    }
}