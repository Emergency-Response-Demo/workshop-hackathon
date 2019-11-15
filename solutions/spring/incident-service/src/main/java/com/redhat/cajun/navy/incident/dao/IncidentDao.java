package com.redhat.cajun.navy.incident.dao;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.redhat.cajun.navy.incident.entity.Incident;
import org.springframework.stereotype.Component;

@Component
public class IncidentDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Incident create(Incident incident) {
        entityManager.persist(incident);
        return incident;
    }

    public Incident findByIncidentId(String incidentId) {
        if (incidentId == null || incidentId.isEmpty()) {
            return null;
        }
        List<Incident> incidents = entityManager.createNamedQuery("Incident.byIncidentId", Incident.class)
                .setParameter("incidentId", incidentId)
                .getResultList();
        if (incidents.isEmpty()) {
            return null;
        }
        return incidents.get(0);
    }

    public Incident merge(Incident incident) {
        Incident r = entityManager.merge(incident);
        entityManager.flush();
        return r;
    }

    public List<Incident> findAll() {
        return entityManager.createNamedQuery("Incident.findAll", Incident.class).getResultList();
    }

    public List<Incident> findByStatus(String status) {
        return entityManager.createNamedQuery("Incident.byStatus", Incident.class)
                .setParameter("status", status.toUpperCase()).getResultList();
    }

    public List<Incident> findByName(String pattern) {
        return entityManager.createNamedQuery("Incident.findByName", Incident.class)
                .setParameter("pattern", pattern.toLowerCase()).getResultList();
    }

    public void deleteAll() {
        entityManager.createNamedQuery("Incident.deleteAll").executeUpdate();
    }

}
