package org.example;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CameraControl implements Runnable {
    private static final Logger logger = Logger.getLogger(CameraControl.class.getName());
    private KafkaConsumer<String, String> consumer;
    private KafkaProducerHelper producer;
    private volatile double cameraAngle = 0; // Kamera açısı başlangıçta 0 belirlenmiştir.
    private volatile boolean running = true;

    public CameraControl() {
        // KafkaConsumer Setup
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "camera-control-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList("TargetBearingPosition"));

        // KafkaProducer Setup
        producer = new KafkaProducerHelper("localhost:9092");
    }

    @Override
    public void run() {
        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    if (record.key().equals("angle")) {
                        cameraAngle = Double.parseDouble(record.value());
                        // Kamera açısını günceller ve durumu gönderir
                        producer.sendMessage("CameraLosStatus", "cameraAngle", String.valueOf(cameraAngle));
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error in CameraControl", e);
        } finally {
            consumer.close();
            producer.close();
            logger.log(Level.INFO, "CameraControl stopped.");
        }
    }

    public void shutdown() {
        running = false;
    }

    public static void main(String[] args) {
        CameraControl cameraControl = new CameraControl();
        Thread thread = new Thread(cameraControl);
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(cameraControl::shutdown));
    }
}
