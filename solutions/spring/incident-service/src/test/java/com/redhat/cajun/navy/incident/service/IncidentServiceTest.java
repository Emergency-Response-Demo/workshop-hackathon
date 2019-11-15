package com.redhat.cajun.navy.incident.service;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.redhat.cajun.navy.incident.dao.IncidentDao;
import com.redhat.cajun.navy.incident.message.IncidentReportedEvent;
import com.redhat.cajun.navy.incident.message.Message;
import com.redhat.cajun.navy.incident.model.Incident;
import com.redhat.cajun.navy.incident.model.IncidentStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.ListenableFuture;

public class IncidentServiceTest {

    @Mock
    private KafkaTemplate<String, Message<?>> kafkaTemplate;

    @Mock
    private IncidentDao incidentDao;

    @Captor
    private ArgumentCaptor<com.redhat.cajun.navy.incident.entity.Incident> entityCaptor;

    @Captor
    private ArgumentCaptor<Message<IncidentReportedEvent>> messageCaptor;

    private IncidentService service;

    @Before
    @SuppressWarnings("unchecked")
    public void init() {
        initMocks(this);
        service = new IncidentService();
        setField(service, null, kafkaTemplate, KafkaTemplate.class);
        setField(service, null, incidentDao, IncidentDao.class);
        setField(service, "destination", "test-topic", String.class);
        ListenableFuture future = mock(ListenableFuture.class);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);
    }

    @Test
    public void testCreateIncident() {

        Incident incident = new Incident.Builder()
                .lat("30.12345")
                .lon("-70.98765")
                .numberOfPeople(2)
                .medicalNeeded(false)
                .victimName("John Doe")
                .victimPhoneNumber("123-456-789")
                .build();
        
        when(incidentDao.create(any(com.redhat.cajun.navy.incident.entity.Incident.class))).thenAnswer((Answer<com.redhat.cajun.navy.incident.entity.Incident>) invocation -> {
            com.redhat.cajun.navy.incident.entity.Incident entity = invocation.getArgument(0);
            setField(entity, "id", 100, null);
            return entity;
        });

        service.create(incident);

        verify(incidentDao).create(entityCaptor.capture());
        com.redhat.cajun.navy.incident.entity.Incident entity = entityCaptor.getValue();
        String incidentId = entity.getIncidentId();
        assertThat(incidentId, notNullValue());
        assertThat(entity.getLatitude(), equalTo(incident.getLat()));
        assertThat(entity.getLongitude(), equalTo(incident.getLon()));
        assertThat(entity.getNumberOfPeople(), equalTo(incident.getNumberOfPeople()));
        assertThat(entity.isMedicalNeeded(), equalTo(incident.isMedicalNeeded()));
        assertThat(entity.getVictimName(), equalTo(incident.getVictimName()));
        assertThat(entity.getVictimPhoneNumber(), equalTo(incident.getVictimPhoneNumber()));
        assertThat(entity.getTimestamp() <= System.currentTimeMillis(), is(true));
        assertThat(entity.getStatus(), equalTo(IncidentStatus.REPORTED.name()));

        verify(kafkaTemplate).send(eq("test-topic"), eq(incidentId), messageCaptor.capture());
        Message<IncidentReportedEvent> message = messageCaptor.getValue();
        assertThat(message.getId(), notNullValue());
        assertThat(message.getInvokingService(), equalTo("IncidentService"));
        assertThat(message.getBody(), notNullValue());
        IncidentReportedEvent event = message.getBody();
        assertThat(event.getId(), notNullValue());
        assertThat(event.getId(), equalTo(incidentId));
        assertThat(event.getLat(), equalTo(new BigDecimal(incident.getLat())));
        assertThat(event.getLon(), equalTo(new BigDecimal(incident.getLon())));
        assertThat(event.getNumberOfPeople(), equalTo(incident.getNumberOfPeople()));
        assertThat(event.isMedicalNeeded(), equalTo(incident.isMedicalNeeded()));
        assertThat(event.getTimestamp(), equalTo(entity.getTimestamp()));
    }

    @Test
    public void testUpdateIncident() {

        Incident toUpdate = new Incident.Builder("testId").status(IncidentStatus.PICKEDUP.name()).build();

        com.redhat.cajun.navy.incident.entity.Incident current = new com.redhat.cajun.navy.incident.entity.Incident.Builder(1L, 1L)
                .incidentId("testId")
                .victimName("John Doe")
                .victimPhoneNumber("111-222-333")
                .latitude("30.12345")
                .longitude("-77.98765")
                .numberOfPeople(2)
                .medicalNeeded(true)
                .reportedTime(System.currentTimeMillis())
                .status(IncidentStatus.REPORTED.name())
                .build();

        com.redhat.cajun.navy.incident.entity.Incident updated = new com.redhat.cajun.navy.incident.entity.Incident.Builder(1L, 1L)
                .incidentId("testId")
                .victimName("John Doe")
                .victimPhoneNumber("111-222-333")
                .latitude("30.12345")
                .longitude("-77.98765")
                .numberOfPeople(2)
                .medicalNeeded(true)
                .reportedTime(System.currentTimeMillis())
                .status(IncidentStatus.PICKEDUP.name())
                .build();

        when(incidentDao.findByIncidentId(any(String.class))).thenReturn(current);
        when(incidentDao.merge(any(com.redhat.cajun.navy.incident.entity.Incident.class))).thenReturn(updated);

        service.updateIncident(toUpdate);

        verify(incidentDao).findByIncidentId(eq("testId"));
        verify(incidentDao).merge(entityCaptor.capture());
        com.redhat.cajun.navy.incident.entity.Incident entity = entityCaptor.getValue();
        assertThat(entity, notNullValue());
        assertThat(entity.getId(), equalTo(1L));
        assertThat(entity.getIncidentId(), equalTo("testId"));
        assertThat(entity.getLatitude(), equalTo("30.12345"));
        assertThat(entity.getLongitude(), equalTo("-77.98765"));
        assertThat(entity.getVictimName(), equalTo("John Doe"));
        assertThat(entity.getVictimPhoneNumber(), equalTo("111-222-333"));
        assertThat(entity.getNumberOfPeople(), equalTo(2));
        assertThat(entity.isMedicalNeeded(), equalTo(true));
        assertThat(entity.getReportedTime(), equalTo(current.getReportedTime()));
        assertThat(entity.getStatus(), equalTo("PICKEDUP"));

    }

    @Test
    public void testFindAllIncidents() {
        com.redhat.cajun.navy.incident.entity.Incident incident1 = new com.redhat.cajun.navy.incident.entity.Incident.Builder(1L, 1L)
                .incidentId("incident123")
                .victimName("John Doe")
                .victimPhoneNumber("111-222-333")
                .latitude("30.12345")
                .longitude("-77.98765")
                .numberOfPeople(2)
                .medicalNeeded(true)
                .reportedTime(System.currentTimeMillis())
                .status(IncidentStatus.REPORTED.name())
                .build();

        com.redhat.cajun.navy.incident.entity.Incident incident2 = new com.redhat.cajun.navy.incident.entity.Incident.Builder(2L, 1L)
                .incidentId("incident987")
                .victimName("John Foo")
                .victimPhoneNumber("999-888-777")
                .latitude("35.12345")
                .longitude("-71.98765")
                .numberOfPeople(6)
                .medicalNeeded(false)
                .reportedTime(System.currentTimeMillis())
                .status(IncidentStatus.PICKEDUP.name())
                .build();

        when(incidentDao.findAll()).thenReturn(Arrays.asList(incident1, incident2));

        List<Incident> incidents = service.incidents();
        assertThat(incidents, notNullValue());
        assertThat(incidents.size(), equalTo(2));
        assertThat(incidents.get(0).getId(), anyOf(equalTo("incident123"), equalTo("incident987")));
        assertThat(incidents.get(1).getId(), anyOf(equalTo("incident123"), equalTo("incident987")));
        assertThat(incidents.get(0).getId(), not(equalTo(incidents.get(1).getId())));
        assertThat(incidents.get(0).getVictimName(), anyOf(equalTo("John Doe"), equalTo("John Foo")));
        assertThat(incidents.get(1).getVictimName(), anyOf(equalTo("John Doe"), equalTo("John Foo")));
        assertThat(incidents.get(0).getVictimName(), not(equalTo(incidents.get(1).getVictimName())));
        assertThat(incidents.get(0).getVictimPhoneNumber(), anyOf(equalTo("111-222-333"), equalTo("999-888-777")));
        assertThat(incidents.get(0).getLat(), anyOf(equalTo("30.12345"), equalTo("35.12345")));
        assertThat(incidents.get(0).getLon(), anyOf(equalTo("-77.98765"), equalTo("-71.98765")));
        assertThat(incidents.get(0).getNumberOfPeople(), anyOf(equalTo(2), equalTo(6)));
        assertThat(incidents.get(0).isMedicalNeeded(), anyOf(equalTo(true), equalTo(false)));
        assertThat(incidents.get(0).isMedicalNeeded(), not(equalTo(incidents.get(1).isMedicalNeeded())));
        assertThat(incidents.get(0).getStatus(), anyOf(equalTo("REPORTED"), equalTo("ASSIGNED")));
        assertThat(incidents.get(0).getStatus(), not(equalTo(incidents.get(1).getStatus())));

        verify(incidentDao).findAll();
    }

    @Test
    public void testFindIncidentsByStatus() {
        com.redhat.cajun.navy.incident.entity.Incident incident1 = new com.redhat.cajun.navy.incident.entity.Incident.Builder(1L, 1L)
                .incidentId("incident123")
                .victimName("John Doe")
                .victimPhoneNumber("111-222-333")
                .latitude("30.12345")
                .longitude("-77.98765")
                .numberOfPeople(2)
                .medicalNeeded(true)
                .reportedTime(System.currentTimeMillis())
                .status(IncidentStatus.REPORTED.name())
                .build();

        when(incidentDao.findByStatus("reported")).thenReturn(Collections.singletonList(incident1));

        List<Incident> incidents = service.incidentsByStatus("reported");
        assertThat(incidents, notNullValue());
        assertThat(incidents.size(), equalTo(1));
        Incident result = incidents.get(0);
        assertThat(result.getId(), equalTo("incident123"));
        assertThat(result.getVictimName(), equalTo("John Doe"));
        assertThat(result.getVictimPhoneNumber(), equalTo("111-222-333"));
        assertThat(result.getLat(), equalTo("30.12345"));
        assertThat(result.getLon(), equalTo("-77.98765"));
        assertThat(result.getNumberOfPeople(), equalTo(2));
        assertThat(result.isMedicalNeeded(), equalTo(true));
        assertThat(result.getStatus(), equalTo("REPORTED"));

        verify(incidentDao).findByStatus("reported");
    }


    @Test
    public void testFindIncidentsById() {
        com.redhat.cajun.navy.incident.entity.Incident incident1 = new com.redhat.cajun.navy.incident.entity.Incident.Builder(1L, 1L)
                .incidentId("incident123")
                .victimName("John Doe")
                .victimPhoneNumber("111-222-333")
                .latitude("30.12345")
                .longitude("-77.98765")
                .numberOfPeople(2)
                .medicalNeeded(true)
                .reportedTime(System.currentTimeMillis())
                .status(IncidentStatus.REPORTED.name())
                .build();


        when(incidentDao.findByIncidentId("incident123")).thenReturn(incident1);
        Incident result = service.getIncident(incident1.getIncidentId());

        assertThat(result.getId(), equalTo("incident123"));
        assertThat(result.getVictimName(), equalTo("John Doe"));
        assertThat(result.getVictimPhoneNumber(), equalTo("111-222-333"));
        assertThat(result.getLat(), equalTo("30.12345"));
        assertThat(result.getLon(), equalTo("-77.98765"));
        assertThat(result.getNumberOfPeople(), equalTo(2));
        assertThat(result.isMedicalNeeded(), equalTo(true));
        assertThat(result.getStatus(), equalTo("REPORTED"));

    }

    @Test
    public void testFindIncidentsByStatusEmptyList() {

        when(incidentDao.findByStatus("reported")).thenReturn(Collections.emptyList());

        List<Incident> incidents = service.incidentsByStatus("reported");
        assertThat(incidents, notNullValue());
        assertThat(incidents.size(), equalTo(0));

        verify(incidentDao).findByStatus("reported");
    }

    @Test
    public void testFindIncidentsByName() {
        com.redhat.cajun.navy.incident.entity.Incident incident1 = new com.redhat.cajun.navy.incident.entity.Incident.Builder(1L, 1L)
                .incidentId("incident123")
                .victimName("John Doe")
                .victimPhoneNumber("111-222-333")
                .latitude("30.12345")
                .longitude("-77.98765")
                .numberOfPeople(2)
                .medicalNeeded(true)
                .reportedTime(System.currentTimeMillis())
                .status(IncidentStatus.REPORTED.name())
                .build();

        when(incidentDao.findByName("John Doe")).thenReturn(Collections.singletonList(incident1));

        List<Incident> incidents = service.incidentsByName("John Doe");
        assertThat(incidents, notNullValue());
        assertThat(incidents.size(), equalTo(1));
        Incident result = incidents.get(0);
        assertThat(result.getId(), equalTo("incident123"));
        assertThat(result.getVictimName(), equalTo("John Doe"));
        assertThat(result.getVictimPhoneNumber(), equalTo("111-222-333"));
        assertThat(result.getLat(), equalTo("30.12345"));
        assertThat(result.getLon(), equalTo("-77.98765"));
        assertThat(result.getNumberOfPeople(), equalTo(2));
        assertThat(result.isMedicalNeeded(), equalTo(true));
        assertThat(result.getStatus(), equalTo("REPORTED"));

        verify(incidentDao).findByName("John Doe");
    }

}
