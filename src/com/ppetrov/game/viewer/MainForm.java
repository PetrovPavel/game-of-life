package com.ppetrov.game.viewer;

import com.ppetrov.game.model.Game;
import com.ppetrov.game.model.IGameListener;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MainForm extends Application implements IGameListener {

    private Canvas canvas;

    private Game game = new Game();

    private Integer columnUnderCursor;

    private Integer rowUnderCursor;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Game of Life");

        ScrollPane canvasPane = new ScrollPane();
        canvasPane.setStyle("-fx-background-color:transparent;");

        this.canvas = new Canvas();
        GraphicsContext gc = this.canvas.getGraphicsContext2D();
        this.canvas.widthProperty().addListener(observable -> redraw(gc));
        this.canvas.heightProperty().addListener(observable -> redraw(gc));
        canvasPane.setContent(this.canvas);
        canvasPane.setPrefSize(500, 500);

        EventHandler<MouseEvent> canvasMouseEventHandler = event -> {
            double x = event.getX();
            double y = event.getY();
            boolean inDrawingArea = isInDrawingArea(x, y);
            primaryStage.getScene().setCursor(inDrawingArea ? Cursor.HAND : Cursor.DEFAULT);
            if (inDrawingArea) {
                redraw(gc);
                this.rowUnderCursor = getCellRowFromCanvas(y);
                this.columnUnderCursor = getCellColumnFromCanvas(x);
            }
        };
        this.canvas.setOnMouseEntered(canvasMouseEventHandler);
        this.canvas.setOnMouseMoved(canvasMouseEventHandler);
        this.canvas.setOnMouseExited(event -> {
            clearRowsUnderCursor();
            redraw(gc);
            primaryStage.getScene().setCursor(Cursor.DEFAULT);
        });
        this.canvas.setOnMouseClicked(event -> {
            Integer row = getCellRowFromCanvas(event.getY());
            Integer column = getCellColumnFromCanvas(event.getX());
            if (row != null && column != null) {
                MouseButton mouseButton = event.getButton();
                boolean isPrimary = MouseButton.PRIMARY.equals(mouseButton);
                boolean isSecondary = MouseButton.SECONDARY.equals(mouseButton);
                if (isPrimary || isSecondary) {
                    this.game.setCell(row, column, isPrimary);
                    drawCalculatedCell(gc, row, column);
                    redraw(gc);
                }
            }
        });

        this.game.addListener(this);
        this.game.start();

        Label widthLabel = new Label("Field width:");
        Spinner<Integer> widthSpinner = new Spinner<>(10, 150, getFieldWidth());
        Label heightLabel = new Label("Field height:");
        Spinner<Integer> heightSpinner = new Spinner<>(10, 150, getFieldHeight());

        Button applySettingsButton = new Button("Restart");
        applySettingsButton.setMaxWidth(Integer.MAX_VALUE);
        applySettingsButton.setOnAction(event -> {
            this.game.startNewMap(widthSpinner.getValue(), heightSpinner.getValue());
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

    private void clearRowsUnderCursor() {
        this.rowUnderCursor = null;
        this.columnUnderCursor = null;
    }

    @Override
    public void stop() throws Exception {
        this.game.removeListener(this);
        this.game.stop();
        super.stop();
    }

    private void drawGameStep(GraphicsContext gc) {
        redraw(gc);
    }

    private void redraw(GraphicsContext gc) {
        gc.setFill(Color.GRAY);
        gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());

        for (int i = 0; i < getFieldHeight(); i++) {
            for (int j = 0; j < getFieldWidth(); j++) {
                drawCalculatedCell(gc, i, j);
            }
        }

        drawCellsUnderCursor(gc);
    }

    private void drawCellsUnderCursor(GraphicsContext gc) {
        if (this.rowUnderCursor != null && this.columnUnderCursor != null) {
            gc.setFill(new Color(1, 1, 1, 0.5));
            drawCell(gc, this.rowUnderCursor, this.columnUnderCursor);
        }
    }

    private void drawCalculatedCell(GraphicsContext gc, int row, int column) {
        boolean isAlive = this.game.getCell(row, column);
        gc.setFill(isAlive ? Color.DARKGREEN : Color.SANDYBROWN);
        drawCell(gc, row, column);
    }

    private void drawCell(GraphicsContext gc, int row, int column) {
        double cellSize = getCellSize();
        int fieldWidth = getFieldWidth();
        int fieldHeight = getFieldHeight();

        double startX = (this.canvas.getWidth() - cellSize * fieldWidth) / 2;
        double startY = (this.canvas.getHeight() - cellSize * fieldHeight) / 2;

        int borderWidth = 1;

        gc.fillRect(
                startX + column * cellSize,
                startY + row * cellSize,
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
        return this.game.getWidth();
    }

    private int getFieldHeight() {
        return this.game.getHeight();
    }

    @Override
    public void onStepPerformed() {
        Platform.runLater(() -> drawGameStep(this.canvas.getGraphicsContext2D()));
    }

}
