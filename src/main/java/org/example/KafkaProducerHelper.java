package org.example;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KafkaProducerHelper {
    private static final Logger logger = Logger.getLogger(KafkaProducerHelper.class.getName());
    private Producer<String, String> producer;

    public KafkaProducerHelper(String bootstrapServers) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        producer = new KafkaProducer<>(props);
    }

    public void sendMessage(String topic, String key, String value) {
        try {
            producer.send(new ProducerRecord<>(topic, key, value)).get();
            producer.flush();
            logger.log(Level.INFO, "Message sent to topic {0}: key={1}, value={2}", new Object[]{topic, key, value});
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error sending message to topic " + topic, e);
        }
    }

    public void close() {
        producer.close();
        logger.log(Level.INFO, "Kafka producer closed.");
    }
}
