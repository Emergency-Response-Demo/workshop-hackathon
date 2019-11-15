package com.redhat.cajun.navy.incident;

import java.util.List;

import com.redhat.cajun.navy.incident.model.Incident;
import com.redhat.cajun.navy.incident.service.IncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/incidents")
public class IncidentsController {

    @Autowired
    private IncidentService incidentService;

    @RequestMapping(value = "", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity reportIncident(@RequestBody Incident incident) {
        incidentService.create(incident);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Incident>> incidents() {
        return new ResponseEntity<>(incidentService.incidents(), HttpStatus.OK);
    }

    @RequestMapping(value = "/{status}", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Incident>> incidentsByStatus(@PathVariable String status) {
        return new ResponseEntity<>(incidentService.incidentsByStatus(status), HttpStatus.OK);
    }

    @RequestMapping(value = "/incident/{id}", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<Incident> incident(@PathVariable String id) {
        Incident incident = incidentService.getIncident(id);
        if (incident == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(incident, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/victim/byname/{name}", method = RequestMethod.GET, produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Incident>> incidentsByVictimName(@PathVariable String name) {
        return new ResponseEntity<>(incidentService.incidentsByName(name), HttpStatus.OK);
    }

    @RequestMapping(value = "/reset", method = RequestMethod.POST)
    public ResponseEntity reset() {
        incidentService.reset();
        return new ResponseEntity(HttpStatus.OK);
    }

}
