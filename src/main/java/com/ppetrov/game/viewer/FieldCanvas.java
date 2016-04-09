package com.ppetrov.game.viewer;

import com.ppetrov.game.model.Map;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class FieldCanvas {

    private Map map;

    private Stage stage;

    private ScrollPane pane;
    private Canvas canvas;

    private Integer columnUnderCursor;
    private Integer rowUnderCursor;

    public FieldCanvas(Stage stage) {
        this.stage = stage;
        create();
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public ScrollPane getPane() {
        return this.pane;
    }

    public Canvas getCanvas() {
        return this.canvas;
    }

    private void create() {
        this.pane = new ScrollPane();
        createFieldCanvas();

        this.pane.setContent(this.canvas);
        this.pane.setStyle("-fx-background-color:transparent;");

        this.pane.setPrefSize(500, 500);
    }

    private void createFieldCanvas() {
        this.canvas = new Canvas();
        this.canvas.widthProperty().addListener(observable -> redraw());
        this.canvas.heightProperty().addListener(observable -> redraw());

        EventHandler<MouseEvent> canvasMouseEventHandler = event -> {
            double x = event.getX();
            double y = event.getY();
            boolean inDrawingArea = isInDrawingArea(x, y);
            this.stage.getScene().setCursor(inDrawingArea ? Cursor.HAND : Cursor.DEFAULT);
            this.rowUnderCursor = getCellRowFromCanvas(y);
            this.columnUnderCursor = getCellColumnFromCanvas(x);
            redraw();
        };
        this.canvas.setOnMouseEntered(canvasMouseEventHandler);
        this.canvas.setOnMouseMoved(canvasMouseEventHandler);
        this.canvas.setOnMouseExited(event -> {
            this.stage.getScene().setCursor(Cursor.DEFAULT);
            clearRowsUnderCursor();
        });

        this.canvas.setOnMouseDragged(event -> {
            this.rowUnderCursor = getCellRowFromCanvas(event.getY());
            this.columnUnderCursor = getCellColumnFromCanvas(event.getX());
            if (this.rowUnderCursor != null && this.columnUnderCursor != null) {
                MouseButton mouseButton = event.getButton();
                boolean isPrimary = MouseButton.PRIMARY.equals(mouseButton);
                boolean isSecondary = MouseButton.SECONDARY.equals(mouseButton);
                if (isPrimary || isSecondary) {
                    this.map.setCell(this.rowUnderCursor, this.columnUnderCursor, isPrimary);
                    redraw();
                }
            } else {
                clearRowsUnderCursor();
            }
        });
    }

    public void redraw() {
        GraphicsContext gc = this.canvas.getGraphicsContext2D();
        gc.setFill(Color.GRAY);
        gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());

        for (int i = 0; i < getFieldHeight(); i++) {
            for (int j = 0; j < getFieldWidth(); j++) {
                drawCalculatedCell(i, j);
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

    private void drawCalculatedCell(int row, int column) {
        GraphicsContext gc = this.canvas.getGraphicsContext2D();
        boolean isAlive = this.map.getCell(row, column);
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

    private void clearRowsUnderCursor() {
        this.rowUnderCursor = null;
        this.columnUnderCursor = null;
        redraw();
    }

    private Integer getCellColumnFromCanvas(double x) {
        double cellSize = getCellSize();
        int fieldWidth = getFieldWidth();
        double startX = (this.canvas.getWidth() - cellSize * fieldWidth) / 2;

        int column = (int) Math.floor((x - startX) / cellSize);
        if (0 <= column && column < fieldWidth) {
            return column;
        }

        return null;
    }

    private Integer getCellRowFromCanvas(double y) {
        double cellSize = getCellSize();
        int fieldHeight = getFieldHeight();
        double startY = (this.canvas.getHeight() - cellSize * fieldHeight) / 2;

        int row = (int) Math.floor((y - startY) / cellSize);
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

    public double getCellSize() {
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
