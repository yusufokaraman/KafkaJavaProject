package org.example;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RadarControl implements Runnable {
    private static final Logger logger = Logger.getLogger(RadarControl.class.getName());
    private KafkaConsumer<String, String> consumer;
    private KafkaProducerHelper producer;
    private volatile boolean running = true;

    public RadarControl() {
        // KafkaConsumer Setup
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "radar-control-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("TargetPointPosition"));

        // KafkaProducer Setup
        producer = new KafkaProducerHelper("localhost:9092");
    }

    @Override
    public void run() {
        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    String[] position = record.value().split(",");
                    if (position.length == 2) {
                        try {
                            double x = Double.parseDouble(position[0]);
                            double y = Double.parseDouble(position[1]);

                            // Hesaplamalar: hedefin açısal pozisyonunu ve mesafesini hesapla
                            double angle = Math.atan2(y, x) * (180 / Math.PI);
                            double distance = Math.sqrt(x * x + y * y);

                            // RadarOutput konusuna mesaj gönder
                            producer.sendMessage("TargetBearingPosition", "angle", String.valueOf(angle));
                            producer.sendMessage("TargetBearingPosition", "distance", String.valueOf(distance));
                        } catch (NumberFormatException e) {
                            logger.log(Level.WARNING, "Invalid position data: " + record.value(), e);
                        }
                    } else {
                        logger.log(Level.WARNING, "Invalid position data: " + record.value());
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in RadarControl", e);
        } finally {
            consumer.close();
            producer.close();
            logger.log(Level.INFO, "RadarControl stopped.");
        }
    }

    public void shutdown() {
        running = false;
    }

    public static void main(String[] args) {
        RadarControl radarControl = new RadarControl();
        Thread thread = new Thread(radarControl);
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(radarControl::shutdown));
    }
}
