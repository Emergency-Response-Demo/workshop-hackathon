package com.example.incident;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.util.HashMap;
import java.util.Map;

public class IncidentProducer extends AbstractVerticle {

  private final Logger logger = LoggerFactory.getLogger(IncidentProducer.class.getName());
  private Map<String, String> config = new HashMap<>();
  private KafkaProducer<String,String> producer = null;
  private String incidentReportedEvent = null;

  @Override
  public void start() throws Exception {

    config.put("bootstrap.servers", config().getString("kafka.connect", "localhost:9092"));
    config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

    incidentReportedEvent = config().getString("kafka.pub");
    producer = KafkaProducer.create(vertx,config);
    vertx.eventBus().consumer("out.queue", this::onMessage);
  }

  public void onMessage(Message<JsonObject> message) {

    if (!message.headers().contains("action")) {
      message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No action header specified");
      return;
    }


    String action = message.headers().get("action");
    String key = message.headers().get("key");
    switch (action) {
      case "PUBLISH_EVENT":
        sendMessage(incidentReportedEvent, key, String.valueOf(message.body()));
        message.reply("Message sent "+ incidentReportedEvent);
        break;

      default:
        message.fail(ErrorCodes.BAD_ACTION.ordinal(), "Bad action: " + action);

    }
  }


  public void sendMessage(String topic, String key, String body){
    KafkaProducerRecord<String, String> record = KafkaProducerRecord.create(topic, key, body);
    producer.write(record, done -> {
      if (done.succeeded()) {
        logger.info("Message " + record.value() + " written on topic=" + record.topic() +
          ", partition=" + record.partition());
      }
    });
  }

}
