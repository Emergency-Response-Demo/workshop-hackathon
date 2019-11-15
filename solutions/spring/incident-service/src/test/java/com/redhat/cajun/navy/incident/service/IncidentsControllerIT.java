package com.redhat.cajun.navy.incident.service;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import com.redhat.cajun.navy.incident.dao.IncidentDao;
import com.redhat.cajun.navy.incident.entity.Incident;
import com.redhat.cajun.navy.incident.listener.UpdateIncidentCommandListener;
import com.redhat.cajun.navy.incident.message.IncidentReportedEvent;
import com.redhat.cajun.navy.incident.message.Message;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IncidentsControllerIT {

    @Value("${local.server.port}")
    private int port;

    @MockBean
    private UpdateIncidentCommandListener updateIncidentCommandListener;

    @Autowired
    private KafkaTemplate<String, Message<?>> kafkaTemplate;

    @Autowired
    private IncidentDao incidentDao;

    @Captor
    private ArgumentCaptor<Message<IncidentReportedEvent>> messageCaptor;

    @Captor
    private ArgumentCaptor<String> destination;

    @Captor
    private ArgumentCaptor<String> key;

    @Before
    public void beforeTest() {
        RestAssured.baseURI = String.format("http://localhost:%d", port);
        ListenableFuture future = mock(ListenableFuture.class);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);
    }

    @Test
    public void testReportIncident() throws Exception {

        String reportedIncident = "{" +
                "\"lat\": \"34.14338\"," +
                "\"lon\": \"-77.86569\"," +
                "\"numberOfPeople\": 3," +
                "\"medicalNeeded\": true," +
                "\"victimName\": \"victim\"," +
                "\"victimPhoneNumber\": \"111-111-111\"" +
                "}";

        Response response = given().header(new Header("Content-type", "application/json"))
                .request().body(reportedIncident).post("/incidents");
        assertThat(response.statusCode(), equalTo(201));
        Mockito.verify(kafkaTemplate).send(destination.capture(), key.capture(), messageCaptor.capture());
        assertThat(destination.getValue(), equalTo("topic-foo"));
        assertThat(key.getValue(), notNullValue());
        Message<IncidentReportedEvent> message = messageCaptor.getValue();
        assertThat(message.getId(), notNullValue());
        assertThat(message.getInvokingService(), equalTo("IncidentService"));
        assertThat(message.getBody(), notNullValue());
        IncidentReportedEvent event = message.getBody();
        assertThat(event.getId(), notNullValue());
        assertThat(event.getId(), equalTo(key.getValue()));
        assertThat(event.getLat(), equalTo(new BigDecimal("34.14338")));
        assertThat(event.getLon(), equalTo(new BigDecimal("-77.86569")));
        assertThat(event.getNumberOfPeople(), equalTo(3));
        assertThat(event.isMedicalNeeded(), equalTo(true));
        assertThat(event.getTimestamp() <= System.currentTimeMillis(), is(true));

        Incident incidentEntity = incidentDao.findByIncidentId(key.getValue());
        assertThat(incidentEntity, notNullValue());
    }
}
