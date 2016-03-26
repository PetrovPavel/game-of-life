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
import javafx.scene.layout.Priority;
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

    private Canvas canvas;

    private Map map = new Map(100, 100);

    private Timer timer = new Timer();

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Game of Life");

        ScrollPane canvasPane = new ScrollPane();
        canvasPane.setStyle("-fx-background-color:transparent;");

        this.canvas = new Canvas();
        GraphicsContext gc = this.canvas.getGraphicsContext2D();
        this.canvas.widthProperty().addListener(observable -> drawGameStep(gc));
        this.canvas.heightProperty().addListener(observable -> drawGameStep(gc));
        canvasPane.setContent(this.canvas);
        canvasPane.setPrefSize(500, 500);

        drawGame(gc);

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
        });

        VBox settingsGroup = new VBox(
                widthLabel, widthSpinner,
                heightLabel, heightSpinner,
                applySettingsButton
        );
        settingsGroup.setStyle("-fx-background-color:transparent;");

        this.canvas.widthProperty().bind(
                canvasPane.widthProperty().
                subtract(settingsGroup.getWidth()).
                subtract(2)
        );
        this.canvas.heightProperty().bind(
                canvasPane.heightProperty().
                subtract(2)
        );

        HBox root = new HBox();
        root.getChildren().addAll(canvasPane, settingsGroup);
        HBox.setHgrow(canvasPane, Priority.ALWAYS);

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
        gc.setFill(Color.GRAY);
        gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
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
        return (int) Math.ceil(Math.min(
                this.canvas.getWidth() / this.map.getWidth(),
                this.canvas.getHeight() / this.map.getHeight()
        ));
    }

    private int getFieldWidth() {
        return this.width;
    }

    private int getFieldHeight() {
        return this.height;
    }

}
