/*eslint-env node*/

var express = require('express');
const bodyParser = require('body-parser');
const uuid = require('uuid/v1');

var app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

var Map = require("collections/map");
const incidents = new Map();

const PORT = process.env.PORT || 8080;
const kafka_topic_in = process.env.KAFKA_IN_TOPIC || "topic-incident-command";
const kafka_topic_out = process.env.KAFKA_OUT_TOPIC || "topic-incident-event";

const KAFKA_HOST = process.env.KAFKA_BOOTSTRAP_ADDRESS || "localhost:9092";

console.log("Using KAFKA_HOST = ", KAFKA_HOST, " IN = ", kafka_topic_in, " OUT = ", kafka_topic_out);

const options = {
    groupId: 'kafka-node-group',
    autoCommit: true,
    autoCommitMsgCount: 5,
    autoCommitIntervalMs: 5000,
    fetchMaxWaitMs: 100,
    fetchMinBytes: 1,
    fetchMaxBytes: 1024 * 10,
    fromOffset: false,
    fromBeginning: false
};
const kafka = require("kafka-node"),
    Producer = kafka.HighLevelProducer,
    Consumer = kafka.Consumer,
    client = new kafka.KafkaClient('localhost:9092', 'my-client-id', {
        sessionTimeout: 300,
        spinDelay: 100,
        retries: 2
    }),
    producer = new Producer(client),

    consumer = new Consumer(client, [{topic: kafka_topic_in, partition: 0}], options);

consumer.on("message", function (message) {
    console.log("got in message", message);
    try {
        var m = JSON.parse(message.value);
        var orig = incidents.get(m.id);
        // do merge
        var res = {...orig, ...m.body.incident};
        incidents.set(m.id, res);
    } catch (e) {
        console.log("kafka update failed;", e);
    }
});

function sendToKafkaAsJson(topic, message) {

    let payloads = [
        {
            topic: topic,
            messages: JSON.stringify({
                id: message.id,
                timestamp: Date.now(),
                messageType: "IncidentReportedEvent",
                invokingService: "IncidentService",
                body: message
            })
        }
    ];
    producer.send(payloads, (err) => {
        if (err) {
            console.log('[kafka-producer -> ' + kafka_topic_out + ']: broker update failed');
        } else {
            console.log('[kafka-producer -> ' + kafka_topic_out + ']: broker update success');
        }
    });
    producer.on('error', function (err) {
        console.log(err);
        console.log('[kafka-producer -> ' + kafka_topic_out + ']: connection errored');
        throw err;
    });

}

app.post('/incidents', function (req, res) {
    const incident = req.body;
    incident.id = uuid();
    incident.status = "REPORTED";

    console.log("adding incident", incident);
    incidents.set(incident.id, incident);
    sendToKafkaAsJson(kafka_topic_out, incident);
    res.json(incident);
});

app.post('/incidents/reset', function() {
    console.log("doing reset");
    incidents.clear();
});

app.get('/incidents', function (req, res) {
    console.log("get called");
    res.send(incidents.valuesArray());
});

app.get('/incidents/:status', function (req, res) {
    console.log("get by status called for:", req.params.status);
    res.send(incidents.valuesArray().filter(function (i)  {
        return i.status === req.params.status;
    }));
});

//

app.get('/incidents/incident/:id', function (req, res) {
    console.log("get by id called for ", req.params.id);
    const incident = incidents.valuesArray().find(function (i)  {
        return i.id === req.params.id;
    });
    if (incident) {
        res.send(incident);
    } else {
        res.status(404).send();
    }
});

app.get('/incidents/victim/byname/:name', function (req, res) {
    console.log("get by victim name called for:", req.params.name);
    res.send(incidents.valuesArray().filter(function (i)  {
        return i.victimName.toLowerCase().match(req.params.name);
    }));
});

app.listen(PORT, function () {
    console.log('app listening on port', PORT);
});
