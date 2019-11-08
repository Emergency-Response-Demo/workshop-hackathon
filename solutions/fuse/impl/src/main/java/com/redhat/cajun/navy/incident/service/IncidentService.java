package com.redhat.cajun.navy.incident.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.redhat.cajun.navy.incident.dao.IncidentDao;
import com.redhat.cajun.navy.incident.message.IncidentReportedEvent;
import com.redhat.cajun.navy.incident.message.Message;
import com.redhat.cajun.navy.incident.model.IncidentStatus;
import com.redhat.cajun.navy.incident.entity.Incident;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

@Service
public class IncidentService {

    private static final Logger log = LoggerFactory.getLogger(IncidentService.class);

    @Autowired
    private KafkaTemplate<String, Message<?>> kafkaTemplate;

    @Autowired
    private IncidentDao incidentDao;

    @Value("${sender.destination.incident-reported-event}")
    private String destination;

    @Transactional
    public Incident create(Incident incident) {

        com.redhat.cajun.navy.incident.entity.Incident created = incidentDao.create(toEntity(incident));

        Message<IncidentReportedEvent> message = new Message.Builder<>("IncidentReportedEvent", "IncidentService",
                new IncidentReportedEvent.Builder(created.getIncidentId())
                        .lat(new BigDecimal(incident.getLatitude()))
                        .lon(new BigDecimal(incident.getLongitude()))
                        .medicalNeeded(incident.isMedicalNeeded())
                        .numberOfPeople(incident.getNumberOfPeople())
                        .timestamp(created.getReportedTime())
                        .build())
                .build();

        ListenableFuture<SendResult<String, Message<?>>> future = kafkaTemplate.send(destination, message.getBody().getId(), message);
        future.addCallback(
                result -> log.debug("Sent 'IncidentReportedEvent' message for incident " + message.getBody().getId()),
                ex -> log.error("Error sending 'IncidentReportedEvent' message for incident " + message.getBody().getId(), ex));

        return fromEntity(created);
    }

    @Transactional
    public Incident getIncident(String incidentId){
        return fromEntity(incidentDao.findByIncidentId(incidentId));
    }

    @Transactional
    public void updateIncident(Incident incident) {
        com.redhat.cajun.navy.incident.entity.Incident current = incidentDao.findByIncidentId(incident.getIncidentId());
        if (current == null) {
            log.warn("Incident with id '" + incident.getIncidentId() + "' not found in the database");
            return;
        }
        com.redhat.cajun.navy.incident.entity.Incident toUpdate = toEntity(incident, current);
        try {
            incidentDao.merge(toUpdate);
        } catch (Exception e) {
            log.warn("Exception '" + e.getClass() + "' when updating Incident with id '" + incident.getIncidentId() + "'. Incident record is not updated.");
        }
    }

    @Transactional
    public List<Incident> incidents() {
        return incidentDao.findAll().stream().map(this::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public List<Incident> incidentsByStatus(String status) {
        return incidentDao.findByStatus(status).stream().map(this::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public List<Incident> incidentsByName(String name) {
        return incidentDao.findByName(name).stream().map(this::fromEntity).collect(Collectors.toList());
    }

    @Transactional
    public void reset() {
        incidentDao.deleteAll();
    }

    private com.redhat.cajun.navy.incident.entity.Incident toEntity(Incident incident) {

        String incidentId = UUID.randomUUID().toString();
        long reportedTimestamp = System.currentTimeMillis();

        return new com.redhat.cajun.navy.incident.entity.IncidentBuilder()
                .incidentId(incidentId)
                .latitude(incident.getLatitude())
                .longitude(incident.getLongitude())
                .medicalNeeded(incident.isMedicalNeeded())
                .numberOfPeople(incident.getNumberOfPeople())
                .victimName(incident.getVictimName())
                .victimPhoneNumber(incident.getVictimPhoneNumber())
                .reportedTime(reportedTimestamp)
                //TODO: ???? this doesn't seem right  .status(IncidentStatus.REPORTED.name())
                .build();
    }

    private com.redhat.cajun.navy.incident.entity.Incident toEntity(Incident incident, com.redhat.cajun.navy.incident.entity.Incident current) {

        if (incident == null) {
            return null;
        }
        return new com.redhat.cajun.navy.incident.entity.IncidentBuilder()
                .incidentId(incident.getIncidentId())
                .latitude(incident.getLatitude() == null? current.getLatitude() : incident.getLatitude())
                .longitude(incident.getLongitude() == null? current.getLongitude() : incident.getLongitude())
                .medicalNeeded(incident.isMedicalNeeded() == null? current.isMedicalNeeded() : incident.isMedicalNeeded())
                .numberOfPeople(incident.getNumberOfPeople() == null ? current.getNumberOfPeople() : incident.getNumberOfPeople())
                .victimName(incident.getVictimName() == null? current.getVictimName() : incident.getVictimName())
                .victimPhoneNumber(incident.getVictimPhoneNumber() == null? current.getVictimPhoneNumber() : incident.getVictimPhoneNumber())
                .status(incident.getStatus() == null? current.getStatus() : incident.getStatus())
                .reportedTime(current.getReportedTime())
                .build();
    }

    private Incident fromEntity(com.redhat.cajun.navy.incident.entity.Incident r) {

        if (r == null) {
            return null;
        }
        return new com.redhat.cajun.navy.incident.entity.IncidentBuilder()
                .incidentId(r.getIncidentId())
                .latitude(r.getLatitude())
                .longitude(r.getLongitude())
                .medicalNeeded(r.isMedicalNeeded())
                .numberOfPeople(r.getNumberOfPeople())
                .victimName(r.getVictimName())
                .victimPhoneNumber(r.getVictimPhoneNumber())
                .status(r.getStatus())
                .reportedTime(r.getReportedTime())
                .build();
    }
}