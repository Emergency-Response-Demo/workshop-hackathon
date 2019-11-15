package com.redhat.cajun.navy.incident.dao;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.UUID;

import com.redhat.cajun.navy.incident.entity.Incident;
import com.redhat.cajun.navy.incident.model.IncidentStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = IncidentDao.class))
public class IncidentDaoTest {

    @Autowired
    private IncidentDao incidentDao;

    @Autowired
    private PlatformTransactionManager transactionManager;

    public void init() {
      incidentDao.deleteAll();
    }

    @Test
    @Transactional
    public void testSaveReportedIncident() {
        Incident incident = new Incident.Builder()
                .incidentId(UUID.randomUUID().toString())
                .latitude("30.12345")
                .longitude("-70.98765")
                .numberOfPeople(3)
                .medicalNeeded(false)
                .victimName("John Doe")
                .victimPhoneNumber("123-456-789")
                .status("Reported")
                .build();

        incidentDao.create(incident);
        assertThat(incident.getId(), not(equalTo(0)));
    }

    @Test
    @Transactional
    public void testFindReportedIncidentById() {

        Incident incident = new Incident.Builder()
                .incidentId(UUID.randomUUID().toString())
                .latitude("30.12345")
                .longitude("-70.98765")
                .numberOfPeople(3)
                .medicalNeeded(false)
                .victimName("John Doe")
                .victimPhoneNumber("123-456-789")
                .status("Reported")
                .reportedTime(System.currentTimeMillis())
                .build();

        incidentDao.create(incident);

        Incident found = incidentDao.findByIncidentId(incident.getIncidentId());
        assertThat(found, notNullValue());
        assertThat(found.getId(), equalTo(incident.getId()));
        assertThat(found.getIncidentId(), equalTo(incident.getIncidentId()));
    }

    @Test
    public void testUpdateReportedIncident() {

        //end the current transaction
        TestTransaction.end();

        Incident incident = new Incident.Builder()
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

        new TransactionTemplate(transactionManager).execute(s -> {
            incidentDao.create(incident);
            return null;
        });

        Incident updated = new Incident.Builder(incident.getId(), incident.getVersion())
                .incidentId("testId")
                .victimName("John Doe")
                .victimPhoneNumber("111-222-333")
                .latitude("30.12345")
                .longitude("-77.98765")
                .numberOfPeople(2)
                .medicalNeeded(true)
                .reportedTime(incident.getTimestamp())
                .status(IncidentStatus.PICKEDUP.name())
                .build();

        new TransactionTemplate(transactionManager).execute(s -> {
            Incident current = incidentDao.findByIncidentId("testId");
            assertThat(current, notNullValue());
            assertThat(current.getStatus(), equalTo("REPORTED"));
            incidentDao.merge(updated);
            return null;
        });

        new TransactionTemplate(transactionManager).execute(s -> {
            Incident current = incidentDao.findByIncidentId("testId");
            assertThat(current, notNullValue());
            assertThat(current.getStatus(), equalTo("PICKEDUP"));
            return null;
        });
    }

    @Test
    @Transactional
    public void testFindAllReportedIncidents() {

        //end the current transaction
        TestTransaction.end();

        new TransactionTemplate(transactionManager).execute(s -> {
            incidentDao.deleteAll();
            return null;
        });

        Incident incident1 = new Incident.Builder()
                .incidentId(UUID.randomUUID().toString())
                .latitude("30.12345")
                .longitude("-70.98765")
                .numberOfPeople(3)
                .medicalNeeded(false)
                .victimName("John Doe")
                .victimPhoneNumber("123-456-789")
                .status("Reported")
                .reportedTime(System.currentTimeMillis())
                .build();

        Incident incident2 = new Incident.Builder()
                .incidentId(UUID.randomUUID().toString())
                .latitude("35.12345")
                .longitude("-75.98765")
                .numberOfPeople(4)
                .medicalNeeded(false)
                .victimName("John Foo")
                .victimPhoneNumber("123-456-789")
                .status("Reported")
                .reportedTime(System.currentTimeMillis())
                .build();


        new TransactionTemplate(transactionManager).execute(s -> {
            incidentDao.create(incident1);
            incidentDao.create(incident2);
            return null;
        });

        new TransactionTemplate(transactionManager).execute(s -> {
            List<Incident> result = incidentDao.findAll();
            assertThat(result, notNullValue());
            assertThat(result.size(), equalTo(2));
            return null;
        });
    }

    @Test
    @Transactional
    public void testFindReportedIncidentsByStatus() {

        //end the current transaction
        TestTransaction.end();

        new TransactionTemplate(transactionManager).execute(s -> {
            incidentDao.deleteAll();
            return null;
        });

        Incident incident1 = new Incident.Builder()
                .incidentId(UUID.randomUUID().toString())
                .latitude("30.12345")
                .longitude("-70.98765")
                .numberOfPeople(3)
                .medicalNeeded(false)
                .victimName("John Doe")
                .victimPhoneNumber("123-456-789")
                .status("REPORTED")
                .reportedTime(System.currentTimeMillis())
                .build();

        Incident incident2 = new Incident.Builder()
                .incidentId(UUID.randomUUID().toString())
                .latitude("35.12345")
                .longitude("-75.98765")
                .numberOfPeople(4)
                .medicalNeeded(false)
                .victimName("John Foo")
                .victimPhoneNumber("123-456-789")
                .status("ASSIGNED")
                .reportedTime(System.currentTimeMillis())
                .build();


        new TransactionTemplate(transactionManager).execute(s -> {
            incidentDao.create(incident1);
            incidentDao.create(incident2);
            return null;
        });

        new TransactionTemplate(transactionManager).execute(s -> {
            List<Incident> result = incidentDao.findByStatus("reported");
            assertThat(result, notNullValue());
            assertThat(result.size(), equalTo(1));
            assertThat(result.get(0).getVictimName(), equalTo("John Doe"));
            assertThat(result.get(0).getStatus(), equalTo("REPORTED"));
            return null;
        });
    }

    @Test
    @Transactional
    public void testFindReportedIncidentsByStatusEmptyList() {

        //end the current transaction
        TestTransaction.end();

        new TransactionTemplate(transactionManager).execute(s -> {
            incidentDao.deleteAll();
            return null;
        });

        Incident incident1 = new Incident.Builder()
                .incidentId(UUID.randomUUID().toString())
                .latitude("30.12345")
                .longitude("-70.98765")
                .numberOfPeople(3)
                .medicalNeeded(false)
                .victimName("John Doe")
                .victimPhoneNumber("123-456-789")
                .status("REPORTED")
                .reportedTime(System.currentTimeMillis())
                .build();

        Incident incident2 = new Incident.Builder()
                .incidentId(UUID.randomUUID().toString())
                .latitude("35.12345")
                .longitude("-75.98765")
                .numberOfPeople(4)
                .medicalNeeded(false)
                .victimName("John Foo")
                .victimPhoneNumber("123-456-789")
                .status("ASSIGNED")
                .reportedTime(System.currentTimeMillis())
                .build();


        new TransactionTemplate(transactionManager).execute(s -> {
            incidentDao.create(incident1);
            incidentDao.create(incident2);
            return null;
        });

        new TransactionTemplate(transactionManager).execute(s -> {
            List<Incident> result = incidentDao.findByStatus("rescued");
            assertThat(result, notNullValue());
            assertThat(result.size(), equalTo(0));
            return null;
        });
    }

    @Test
    @Transactional
    public void testFindIncidentByName() {
        //end the current transaction
        TestTransaction.end();

        new TransactionTemplate(transactionManager).execute(s -> {
            incidentDao.deleteAll();
            return null;
        });

        Incident incident1 = new Incident.Builder()
                .incidentId(UUID.randomUUID().toString())
                .latitude("30.12345")
                .longitude("-70.98765")
                .numberOfPeople(3)
                .medicalNeeded(false)
                .victimName("John Doe")
                .victimPhoneNumber("123-456-789")
                .status("REPORTED")
                .reportedTime(System.currentTimeMillis())
                .build();

        Incident incident2 = new Incident.Builder()
                .incidentId(UUID.randomUUID().toString())
                .latitude("35.12345")
                .longitude("-75.98765")
                .numberOfPeople(4)
                .medicalNeeded(false)
                .victimName("John Foo")
                .victimPhoneNumber("123-456-789")
                .status("ASSIGNED")
                .reportedTime(System.currentTimeMillis())
                .build();


        new TransactionTemplate(transactionManager).execute(s -> {
            incidentDao.create(incident1);
            incidentDao.create(incident2);
            return null;
        });

        new TransactionTemplate(transactionManager).execute(s -> {
            List<Incident> result = incidentDao.findByName("John Foo");
            assertThat(result, notNullValue());
            assertThat(result.size(), equalTo(1));
            return null;
        });

        new TransactionTemplate(transactionManager).execute(s -> {
            List<Incident> result = incidentDao.findByName("%Foo");
            assertThat(result, notNullValue());
            assertThat(result.size(), equalTo(1));
            return null;
        });

        new TransactionTemplate(transactionManager).execute(s -> {
            List<Incident> result = incidentDao.findByName("%foo");
            assertThat(result, notNullValue());
            assertThat(result.size(), equalTo(1));
            return null;
        });

        new TransactionTemplate(transactionManager).execute(s -> {
            List<Incident> result = incidentDao.findByName("%Fo%");
            assertThat(result, notNullValue());
            assertThat(result.size(), equalTo(1));
            return null;
        });

        new TransactionTemplate(transactionManager).execute(s -> {
            List<Incident> result = incidentDao.findByName("%Foo%");
            assertThat(result, notNullValue());
            assertThat(result.size(), equalTo(1));
            return null;
        });

        new TransactionTemplate(transactionManager).execute(s -> {
            List<Incident> result = incidentDao.findByName("John%");
            assertThat(result, notNullValue());
            assertThat(result.size(), equalTo(2));
            return null;
        });

        new TransactionTemplate(transactionManager).execute(s -> {
            List<Incident> result = incidentDao.findByName("john%");
            assertThat(result, notNullValue());
            assertThat(result.size(), equalTo(2));
            return null;
        });

        new TransactionTemplate(transactionManager).execute(s -> {
            List<Incident> result = incidentDao.findByName("Peter%");
            assertThat(result, notNullValue());
            assertThat(result.size(), equalTo(0));
            return null;
        });
    }
}
