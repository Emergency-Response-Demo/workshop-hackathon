package com.example.incident;

import com.example.incident.model.Incident;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import rx.Observable;

public class IncidentResource extends AbstractVerticle {

  private String INCIDENTS_EP = "/incidents";

  private Map<String, Incident> incidents = new LinkedHashMap<>();

  @Override
  public void start(Promise<Void> startPromise) throws Exception {


    // router config for endpoints
    Router router = Router.router(vertx);

    router.get(INCIDENTS_EP).handler(this::getAll);
    router.get(INCIDENTS_EP + "/:status").handler(this::getStatus);
    router.get(INCIDENTS_EP + "/incident/:id").handler(this::getById);
    router.get(INCIDENTS_EP + "/victim/byname/:name").handler(this::getByName);

    router.post(INCIDENTS_EP).handler(this::addOne);
    router.post(INCIDENTS_EP + "/reset").handler(this::reset);

    int port = config().getInteger("http.port", 8080);
    // HTTP server top listen on
    vertx.createHttpServer().requestHandler(req -> {
      req.response()
        .putHeader("content-type", "text/plain")
        .end("Incident Service API");
    }).listen(port, http -> {
      if (http.succeeded()) {
        startPromise.complete();
        System.out.println("HTTP server started on port: "+port);
      } else {
        startPromise.fail(http.cause());
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
    final Incident incident = Json.decodeValue(routingContext.getBodyAsString(),
      Incident.class);
    incidents.put(incident.getId(), incident);
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


}
