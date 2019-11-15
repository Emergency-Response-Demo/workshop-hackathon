package com.example.incident.model;

import java.util.UUID;

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


  public Incident() {
    id = UUID.randomUUID().toString();
    status = IncidentStatus.REPORTED.toString();
    timestamp = System.currentTimeMillis();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLat() {
    return lat;
  }

  public void setLat(String lat) {
    this.lat = lat;
  }

  public String getLon() {
    return lon;
  }

  public void setLon(String lon) {
    this.lon = lon;
  }

  public Integer getNumberOfPeople() {
    return numberOfPeople;
  }

  public void setNumberOfPeople(Integer numberOfPeople) {
    this.numberOfPeople = numberOfPeople;
  }

  public Boolean isMedicalNeeded() {
    return medicalNeeded;
  }

  public void isMedicalNeeded(Boolean medicalNeeded) {
    this.medicalNeeded = medicalNeeded;
  }

  public String getVictimName() {
    return victimName;
  }

  public void setVictimName(String victimName) {
    this.victimName = victimName;
  }

  public String getVictimPhoneNumber() {
    return victimPhoneNumber;
  }

  public void setVictimPhoneNumber(String victimPhoneNumber) {
    this.victimPhoneNumber = victimPhoneNumber;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }



}
