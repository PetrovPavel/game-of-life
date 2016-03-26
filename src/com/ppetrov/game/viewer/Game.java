package com.ppetrov.game.viewer;

import com.ppetrov.game.model.Map;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.input.MouseEvent;
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

    private Canvas canvas;

    private Map map = new Map(50, 50);

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

        EventHandler<MouseEvent> canvasMouseEventHandler = event -> {
            boolean inDrawingArea = isInDrawingArea(event.getX(), event.getY());
            primaryStage.getScene().setCursor(inDrawingArea ? Cursor.HAND : Cursor.DEFAULT);
        };
        this.canvas.setOnMouseEntered(canvasMouseEventHandler);
        this.canvas.setOnMouseMoved(canvasMouseEventHandler);
        this.canvas.setOnMouseExited(event -> primaryStage.getScene().setCursor(Cursor.DEFAULT));
        this.canvas.setOnMouseClicked(event -> {
            Integer row = getCellRowFromCanvas(event.getX());
            Integer column = getCellColumnFromCanvas(event.getY());
            if (row != null && column != null) {
                boolean isAlive = this.map.getCell(row, column);
                this.map.setCell(row, column, !isAlive);
                drawCell(gc, row, column);
            }
        });

        drawGame(gc);

        Label widthLabel = new Label("Field width:");
        Spinner<Integer> widthSpinner = new Spinner<>(10, 150, getFieldWidth());
        Label heightLabel = new Label("Field height:");
        Spinner<Integer> heightSpinner = new Spinner<>(10, 150, getFieldHeight());

        Button applySettingsButton = new Button("Restart");
        applySettingsButton.setMaxWidth(Integer.MAX_VALUE);
        applySettingsButton.setOnAction(event -> {
            this.map = new Map(widthSpinner.getValue(), heightSpinner.getValue());
            drawGameStep(gc);
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
                drawCell(gc, i, j);
            }
        }

        this.map.nextState();
    }

    private void drawCell(GraphicsContext gc, int row, int column) {
        double cellSize = getCellSize();
        int fieldWidth = getFieldWidth();
        int fieldHeight = getFieldHeight();

        double startX = (this.canvas.getWidth() - cellSize * fieldWidth) / 2;
        double startY = (this.canvas.getHeight() - cellSize * fieldHeight) / 2;

        int borderWidth = 1;

        boolean isAlive = this.map.getCell(row, column);

        gc.setFill(isAlive ? Color.DARKGREEN : Color.SANDYBROWN);
        gc.fillRect(
                startX + row * cellSize,
                startY + column * cellSize,
                cellSize - borderWidth,
                cellSize - borderWidth
        );
    }

    private Integer getCellColumnFromCanvas(double x) {
        double cellSize = getCellSize();
        int fieldWidth = getFieldWidth();
        double startX = (this.canvas.getWidth() - cellSize * fieldWidth) / 2;

        int column = (int) ((x - startX) / cellSize);
        if (0 <= column && column < fieldWidth) {
            return column;
        }

        return null;
    }

    private Integer getCellRowFromCanvas(double y) {
        double cellSize = getCellSize();
        int fieldHeight = getFieldHeight();
        double startY = (this.canvas.getHeight() - cellSize * fieldHeight) / 2;

        int row = (int) ((y - startY) / cellSize);
        if (0 <= row && row < fieldHeight) {
            return row;
        }

        return null;
    }

    private boolean isInDrawingArea(double x, double y) {
        Integer row = getCellColumnFromCanvas(x);
        Integer column = getCellRowFromCanvas(y);
        return row != null && column != null;
    }

    private double getCellSize() {
        return Math.min(
                this.canvas.getWidth() / getFieldWidth(),
                this.canvas.getHeight() / getFieldHeight()
        );
    }

    private int getFieldWidth() {
        return this.map.getWidth();
    }

    private int getFieldHeight() {
        return this.map.getHeight();
    }

}
