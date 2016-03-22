package com.ppetrov.game.viewer;

import com.ppetrov.game.model.Map;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Pavel on 22.03.2016.
 */
public class Game extends Application {

    private Map map = new Map(100, 100);

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Game of Life");

        Canvas canvas = new Canvas(500, 500);
        drawGame(canvas.getGraphicsContext2D());

        Group root = new Group();
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root));

        primaryStage.show();
    }

    private void drawGame(GraphicsContext gc) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> drawGameStep(gc));
            }
        }, 0, 1000);
    }

    private void drawGameStep(GraphicsContext gc) {
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                boolean isAlive = this.map.getCell(i, j);
                gc.setFill(isAlive ? Color.GREEN : Color.GRAY);
                gc.fillRect(i * 5, j * 5, 5, 5);
            }
        }
        this.map.nextState();
    }

}
