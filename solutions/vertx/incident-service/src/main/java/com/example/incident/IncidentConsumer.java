package com.example.incident;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.consumer.KafkaConsumer;

import java.util.HashMap;
import java.util.Map;

public class IncidentConsumer extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(IncidentConsumer.class.getName());
  private KafkaConsumer<String, String> consumer = null;
  public String updateIncidentCommand = null;

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    updateIncidentCommand = config().getString("kafka.sub");
    consumer = KafkaConsumer.create(vertx, getConfig(config()));

    consumer.handler(record -> {
      DeliveryOptions options = new DeliveryOptions().addHeader("action", "UPDATE_INCIDENT");
      vertx.eventBus().send("in.queue", record.value(), options, reply -> {
        if (reply.succeeded()) {
          logger.debug("Message accepted");
        } else {
          logger.error("Incoming Message not accepted "+record.topic());
          logger.error(record.value());
        }
      });
    });


    consumer.subscribe(updateIncidentCommand, ar -> {
      if (ar.succeeded()) {
        logger.info("subscribed to UpdateIncidentCommand");
      } else {
        logger.fatal("Could not subscribe " + ar.cause().getMessage());
      }
    });
  }


  @Override
  public void stop() throws Exception {
    consumer.unsubscribe(ar -> {

      if (ar.succeeded()) {
        logger.info("Consumer unsubscribed");
      }
    });
  }


  public static Map<String, String> getConfig(JsonObject vertxConfig){

    Map<String, String> config = new HashMap<>();
    config.put("bootstrap.servers", vertxConfig.getString("kafka.connect", "localhost:9092"));
    config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    config.put("group.id", vertxConfig.getString("kafka.group.id"));
    config.put("auto.offset.reset", "earliest");
    config.put("enable.auto.commit", vertxConfig.getBoolean("kafka.autocommit", true).toString());

    return config;
  }

}

