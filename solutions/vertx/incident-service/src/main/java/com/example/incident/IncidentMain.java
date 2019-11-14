package com.example.incident;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;


public class IncidentMain extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(IncidentMain.class.getName());
  private static void deployVerticles(Vertx vertx, JsonObject config, Future<Void> future){

    Future<String> rFuture = Future.future();
    Future<String> cFuture = Future.future();
    Future<String> pFuture = Future.future();

    DeploymentOptions options = new DeploymentOptions();

    options.setConfig(config);
    vertx.deployVerticle(new IncidentResource(), options, rFuture.completer());
    vertx.deployVerticle(new IncidentConsumer(), options, cFuture.completer());
    vertx.deployVerticle(new IncidentProducer(), options, pFuture.completer());

    CompositeFuture.all(rFuture, cFuture, pFuture).setHandler(ar -> {
      if (ar.succeeded()) {
        logger.info("Verticles deployed successfully.");
        future.complete();
      } else {
        logger.error("WARNINIG: Verticles NOT deployed successfully.");
        future.fail(ar.cause());
      }
    });

  }

  private static ConfigRetrieverOptions selectConfigOptions(){
    ConfigRetrieverOptions options = new ConfigRetrieverOptions();

    if (System.getenv("KUBERNETES_NAMESPACE") != null) {
      ConfigStoreOptions appStore = new ConfigStoreOptions()
        .setType("file")
        .setFormat("properties")
        .setConfig(new JsonObject()
          .put("name", System.getenv("APP_CONFIGMAP_NAME"))
          .put("key", System.getenv("APP_CONFIGMAP_KEY"))
          .put("path", "/deployments/config/app-config.properties"));
      options.addStore(appStore);
    } else {
      ConfigStoreOptions props = new ConfigStoreOptions()
        .setType("file")
        .setFormat("properties")
        .setConfig(new JsonObject().put("path", "src/resources/local-app-config.properties"));
      options.addStore(props);
    }

    return options;
  }

  // Entry point for the app
  public static void main(String[] args) {
    io.vertx.core.Vertx vertx = io.vertx.core.Vertx.vertx(new VertxOptions().setMetricsOptions(
      new MicrometerMetricsOptions()
        .setPrometheusOptions(new VertxPrometheusOptions().setEnabled(true))
        .setEnabled(true)));

    Future<Void> future = Future.future();
    ConfigRetriever.create(vertx, selectConfigOptions())
      .getConfig(ar -> {
        if (ar.succeeded()) {
          deployVerticles(vertx, ar.result(), future);
        } else {
          logger.fatal("Failed to retrieve the configuration.");
          future.fail(ar.cause());
        }
      });
  }

}

