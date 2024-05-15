package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WorldSimulator extends Application {
    private static final Logger logger = Logger.getLogger(WorldSimulator.class.getName());
    private KafkaProducerHelper producer;
    private Circle target;
    private volatile boolean running = false;

    @Override
    public void start(Stage primaryStage) {
        producer = new KafkaProducerHelper("localhost:9092");

        Pane root = new Pane();

        // Hedefi temsil eden daire
        target = new Circle(10);
        target.setTranslateX(100);
        target.setTranslateY(100);
        root.getChildren().add(target);

        // Play butonu
        Button playButton = new Button("Play");
        playButton.setOnAction(e -> {
            if (!running) {
                running = true;
                new Thread(this::simulateMovement).start();
            }
        });
        playButton.setLayoutX(50);
        playButton.setLayoutY(550);
        root.getChildren().add(playButton);

        // Stop butonu
        Button stopButton = new Button("Stop");
        stopButton.setOnAction(e -> running = false);
        stopButton.setLayoutX(150);
        stopButton.setLayoutY(550);
        root.getChildren().add(stopButton);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("World Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void simulateMovement() {
        double x = 100, y = 100;
        while (running) {
            x += Math.random() * 10 - 5;
            y += Math.random() * 10 - 5;
            final double finalX = x, finalY = y;

            Platform.runLater(() -> {
                target.setTranslateX(finalX);
                target.setTranslateY(finalY);
            });

            // Kafka'ya pozisyon bilgisi gönder
            producer.sendMessage("TargetPointPosition", "key", finalX + "," + finalY);

            // Kafka'ya kule pozisyon bilgisi gönder
            producer.sendMessage("TowerPosition", "tower1", "0,0"); // Sabit kule pozisyonu (örnek olarak 0,0 kullanıldı)
            producer.sendMessage("TowerPosition", "tower2", "200,200"); // Diğer sabit kule pozisyonu (örnek olarak 200,200 kullanıldı)
            
            logger.log(Level.INFO, "Position updated: x={0}, y={1}", new Object[]{finalX, finalY});
            try {
                Thread.sleep(1000); // 1 saniye bekle
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Simulation interrupted", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void stop() {
        producer.close();
        running = false;
        logger.log(Level.INFO, "WorldSimulator stopped.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
