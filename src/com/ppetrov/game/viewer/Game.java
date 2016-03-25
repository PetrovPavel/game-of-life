package com.ppetrov.game.viewer;

import com.ppetrov.game.model.Map;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Pavel on 22.03.2016.
 */
public class Game extends Application {

    private int width = 100;

    private int height = 100;

    private Map map = new Map(100, 100);

    private Timer timer = new Timer();

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Game of Life");

        ScrollPane canvasPane = new ScrollPane();
        canvasPane.setStyle("-fx-background-color:transparent;");

        Canvas canvas = new Canvas(500, 500);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawGame(gc);
        canvasPane.setContent(canvas);

        Label widthLabel = new Label("Field width:");
        Spinner<Integer> widthSpinner = new Spinner<>(1, 150, 100);
        Label heightLabel = new Label("Field height:");
        Spinner<Integer> heightSpinner = new Spinner<>(1, 150, 100);

        Button applySettingsButton = new Button("Apply");
        applySettingsButton.setMaxWidth(Integer.MAX_VALUE);
        applySettingsButton.setOnAction(event -> {
            this.width = widthSpinner.getValue();
            this.height = heightSpinner.getValue();
            this.map = new Map(this.width, this.height);
            int cellSize = getCellSize();
            canvas.setWidth(cellSize * this.width);
            canvas.setHeight(cellSize * this.height);
        });

        VBox settingsGroup = new VBox(
                widthLabel, widthSpinner,
                heightLabel, heightSpinner,
                applySettingsButton
        );
        settingsGroup.setStyle("-fx-background-color:transparent;");

        HBox root = new HBox();
        root.getChildren().addAll(canvasPane, settingsGroup);

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        this.timer.cancel();
        super.stop();
    }

    private void drawGame(GraphicsContext gc) {
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> drawGameStep(gc));
            }
        }, 0, 1000);
    }

    private void drawGameStep(GraphicsContext gc) {
        for (int i = 0; i < getFieldWidth(); i++) {
            for (int j = 0; j < getFieldHeight(); j++) {
                boolean isAlive = this.map.getCell(i, j);
                gc.setFill(isAlive ? Color.GREEN : Color.GRAY);
                int cellSize = getCellSize();
                gc.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
            }
        }
        this.map.nextState();
    }

    private int getCellSize() {
        return 5;
    }

    private int getFieldWidth() {
        return this.width;
    }

    private int getFieldHeight() {
        return this.height;
    }

}
