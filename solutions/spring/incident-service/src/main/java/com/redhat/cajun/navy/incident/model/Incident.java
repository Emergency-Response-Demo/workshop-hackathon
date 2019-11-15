package com.redhat.cajun.navy.incident.model;

public class Incident {

    private String id;

    private String lat;

    private String lon;

    private Integer numberOfPeople;

    private Boolean medicalNeeded;

    private String victimName;

    private String victimPhoneNumber;

    private Long timestamp;

    private String status;

    public String getId() {
        return id;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public Integer getNumberOfPeople() {
        return numberOfPeople;
    }

    public Boolean isMedicalNeeded() {
        return medicalNeeded;
    }

    public String getVictimName() {
        return victimName;
    }

    public String getVictimPhoneNumber() {
        return victimPhoneNumber;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public static class Builder {

        private final Incident incident;

        public Builder() {
            incident = new Incident();
        }

        public Builder(String id) {
            incident = new Incident();
            incident.id = id;
        }

        public Builder lat(String lat) {
            incident.lat = lat;
            return this;
        }

        public Builder lon(String lon) {
            incident.lon = lon;
            return this;
        }

        public Builder numberOfPeople(Integer numberOfPeople) {
            incident.numberOfPeople = numberOfPeople;
            return this;
        }

        public Builder medicalNeeded(Boolean medicalNeeded) {
            incident.medicalNeeded = medicalNeeded;
            return this;
        }

        public Builder victimName(String victimName) {
            incident.victimName = victimName;
            return this;
        }

        public Builder victimPhoneNumber(String victimPhoneNumber) {
            incident.victimPhoneNumber = victimPhoneNumber;
            return this;
        }

        public Builder timestamp(Long timestamp) {
            incident.timestamp = timestamp;
            return this;
        }

        public Builder status(String status) {
            incident.status = status;
            return this;
        }

        public Incident build() {
            return incident;
        }

    }
}
