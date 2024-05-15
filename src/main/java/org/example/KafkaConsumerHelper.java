package org.example;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KafkaConsumerHelper implements Runnable {
    private static final Logger logger = Logger.getLogger(KafkaConsumerHelper.class.getName());
    private Consumer<String, String> consumer;
    private String topic;
    private volatile boolean running = true;

    public KafkaConsumerHelper(String bootstrapServers, String groupId, String topic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", groupId);
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("auto.offset.reset", "latest");

        consumer = new KafkaConsumer<>(props);
        this.topic = topic;
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(Collections.singletonList(topic));

            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset());
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in Kafka consumer", e);
        } finally {
            consumer.close();
            logger.log(Level.INFO, "Kafka consumer closed.");
        }
    }

    public void shutdown() {
        running = false;
    }
}
