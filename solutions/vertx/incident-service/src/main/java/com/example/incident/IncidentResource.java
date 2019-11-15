package com.example.incident;

import com.example.incident.message.IncidentReportedEvent;
import com.example.incident.message.UpdateIncidentCommand;
import com.example.incident.model.Incident;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.micrometer.PrometheusScrapingHandler;
import rx.Observable;

public class IncidentResource extends AbstractVerticle {

  private String INCIDENTS_EP = "/incidents";

  private Map<String, Incident> incidents = new LinkedHashMap<>();

  private final Logger logger = LoggerFactory.getLogger(IncidentResource.class.getName());

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    int port = config().getInteger("http.port", 8080);
    vertx.eventBus().consumer("in.queue", this::onMessage);


    // router config
    Router router = Router.router(vertx);
    router.route().handler(BodyHandler.create());

    router.route("/").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response
        .putHeader("content-type", "text/html")
        .end("Incidents API");
    });

    router.post("/incidents").handler(this::addOne);
    router.get(INCIDENTS_EP).handler(this::getAll);
    router.get(INCIDENTS_EP + "/:status").handler(this::getStatus);
    router.get(INCIDENTS_EP + "/incident/:id").handler(this::getById);
    router.get(INCIDENTS_EP + "/victim/byname/:name").handler(this::getByName);
    router.post(INCIDENTS_EP + "/reset").handler(this::reset);

    //vertx.createHttpServer().requestHandler(router).listen(8080);
    // HTTP server top listen on
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(port, ar -> {
        if (ar.succeeded()) {
          startPromise.complete();
          logger.info("Http Server Listening on: "+port);
        } else {
          startPromise.fail(ar.cause());
        }
      });
  }

  private void getAll(RoutingContext routingContext) {
    routingContext.response()
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(Json.encodePrettily(incidents.values()));
  }


  private void getById(RoutingContext routingContext) {
    String id = routingContext.request().getParam("id");
    if (id == null) {
      routingContext.response()
        .setStatusCode(HttpURLConnection.HTTP_NO_CONTENT)
        .end("Response:" + HttpURLConnection.HTTP_NO_CONTENT);
    } else {

      if(incidents.containsKey(id)) {
        Incident incident = incidents.get(id);
        routingContext.response()
          .setStatusCode(HttpURLConnection.HTTP_CREATED)
          .putHeader("content-type", "application/json; charset=utf-8")
          .end(Json.encodePrettily(incident));
      }
    }
    routingContext.response().setStatusCode(204).end();

  }

  private void getByName(RoutingContext routingContext) {
    String name = routingContext.request().getParam("name");
    ArrayList<Incident> list = new ArrayList<>(1);

    if (name == null) {
      routingContext.response().setStatusCode(400).end();
    } else {

      Observable.from(incidents.keySet()).flatMap(s->{
        Incident i = incidents.get(s);
        if(i.getVictimName().equalsIgnoreCase(name))
          list.add(i);
        return Observable.just(i);
      }).subscribe();
    }
    routingContext.response()
      .setStatusCode(HttpURLConnection.HTTP_CREATED)
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(Json.encodePrettily(list));
  }

  private void reset(RoutingContext routingContext) {
    incidents = new LinkedHashMap<>();
    routingContext.response()
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(Json.encodePrettily(incidents.values()));
  }


  private void addOne(RoutingContext routingContext) {
    Incident incident = Json.decodeValue(routingContext.getBodyAsString(), Incident.class);
    logger.info("Processing: "+incident);
    incidents.put(incident.getId(), incident);
    sendIncidentEvent(incident);
    routingContext.response()
      .setStatusCode(201)
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(Json.encodePrettily(incident));
  }

  private void getStatus(RoutingContext routingContext) {
    String status = routingContext.request().getParam("status");
    if (status == null) {
      routingContext.response().setStatusCode(400).end();
    } else {
      routingContext.response()
        .putHeader("content-type", "application/json; charset=utf-8")
        .end(Json.encodePrettily(incidents.values()));
    }
    routingContext.response().setStatusCode(204).end();
  }



  public void onMessage(Message<JsonObject> message) {

    if (!message.headers().contains("action")) {
      message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No action header specified");
      return;
    }

    String action = message.headers().get("action");
    switch (action) {
      case "UPDATE_INCIDENT":
        Incident i = Json.decodeValue(String.valueOf(message.body()), UpdateIncidentCommand.class).getIncident();
        incidents.replace(i.getId(),i);
        break;

      default:
        message.fail(ErrorCodes.BAD_ACTION.ordinal(), "Bad action: " + action);

    }
  }

  private void sendIncidentEvent(Incident incident) {
    com.example.incident.message.Message<IncidentReportedEvent> message = new com.example.incident.message.Message.Builder<>("IncidentReportedEvent", "IncidentService",
      new IncidentReportedEvent.Builder(incident.getId())
        .lat(new BigDecimal(incident.getLat()))
        .lon(new BigDecimal(incident.getLon()))
        .medicalNeeded(incident.isMedicalNeeded())
        .numberOfPeople(incident.getNumberOfPeople())
        .timestamp(incident.getTimestamp())
        .build())
      .build();

    logger.info(message.toString());
    DeliveryOptions options = new DeliveryOptions().addHeader("action", "PUBLISH_EVENT").addHeader("key", incident.getId());
    vertx.eventBus().send("out.queue", message.toString(), options, reply -> {
      if (reply.failed()) {
        System.err.println("Message publish request not accepted while sending update "+message);
      }
    });

  }




}
