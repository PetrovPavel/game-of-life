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
import rx.Observable;
import rx.observables.JavaFxObservable;

import java.util.stream.IntStream;

public class FieldCanvas extends ScrollPane {

    private Canvas canvas;

    private Map map;

    private Integer columnUnderCursor;
    private Integer rowUnderCursor;

    private Map brush = new Map(new Boolean[][]{{true}});

    public FieldCanvas() {
        createFieldCanvas();
        setContent(this.canvas);

        this.canvas.widthProperty().bind(widthProperty().subtract(2));
        this.canvas.heightProperty().bind(heightProperty().subtract(2));
    }

    public void setMap(Map map) {
        this.map = map;
    }

    private void createFieldCanvas() {
        this.canvas = new Canvas();
        this.canvas.widthProperty().addListener(observable -> redraw());
        this.canvas.heightProperty().addListener(observable -> redraw());

        EventHandler<MouseEvent> canvasMouseEventHandler = event -> {
            calcCellUnderCursor(event);
            updateCursor(event);
            redraw();
        };
        this.canvas.setOnMouseEntered(canvasMouseEventHandler);
        this.canvas.setOnMouseMoved(canvasMouseEventHandler);
        this.canvas.setOnMouseExited(event -> clearRowsUnderCursor());

        this.canvas.setOnMouseDragged(this::changeMap);
        this.canvas.setOnMousePressed(this::changeMap);
    }

    public Observable<Map> getMapChanges() {
        return JavaFxObservable.fromNodeEvents(this.canvas, MouseEvent.MOUSE_DRAGGED).
                mergeWith(JavaFxObservable.fromNodeEvents(this.canvas, MouseEvent.MOUSE_PRESSED)).
                map(event -> this.map);
    }

    public void setBrush(Map brush) {
        this.brush = brush;
    }

    private void changeMap(MouseEvent event) {
        calcCellUnderCursor(event);
        if (this.rowUnderCursor != null && this.columnUnderCursor != null) {
            MouseButton mouseButton = event.getButton();
            boolean isPrimary = MouseButton.PRIMARY.equals(mouseButton);
            boolean isSecondary = MouseButton.SECONDARY.equals(mouseButton);
            if (isPrimary || isSecondary) {
                int brushHeight = this.brush.getHeight();
                int brushWidth = this.brush.getWidth();

                IntStream.range(0, brushHeight).forEach(row ->
                        IntStream.range(0, brushHeight).filter(column -> this.brush.isSet(row, column)).
                                forEach(column -> this.map.setCell(
                                        this.rowUnderCursor + row - brushHeight / 2,
                                        this.columnUnderCursor + column - brushWidth / 2,
                                        isPrimary
                                        )
                                )
                );
                redraw();
            }
        } else {
            clearRowsUnderCursor();
        }
    }

    private void calcCellUnderCursor(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        this.rowUnderCursor = getCellRowFromCanvas(y);
        this.columnUnderCursor = getCellColumnFromCanvas(x);
    }

    private void updateCursor(MouseEvent event) {
        boolean inDrawingArea = isInDrawingArea(event.getX(), event.getY());
        this.canvas.setCursor(inDrawingArea ? Cursor.HAND : Cursor.DEFAULT);
    }

    public void redraw() {
        GraphicsContext gc = this.canvas.getGraphicsContext2D();
        gc.setFill(Color.GRAY);
        gc.fillRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());

        for (int row = 0; row < getFieldHeight(); row++) {
            for (int column = 0; column < getFieldWidth(); column++) {
                drawCalculatedCell(row, column);
            }
        }

        drawCellsUnderCursor(gc);
    }

    private void drawCellsUnderCursor(GraphicsContext gc) {
        if (this.rowUnderCursor != null && this.columnUnderCursor != null) {
            gc.setFill(new Color(1, 1, 1, 0.5));
            int brushHeight = this.brush.getHeight();
            int brushWidth = this.brush.getWidth();


            IntStream.range(0, brushHeight).forEach(row ->
                    IntStream.range(0, brushHeight).filter(column -> this.brush.isSet(row, column)).
                            forEach(column -> drawCell(gc,
                                    this.map.fixRow(this.rowUnderCursor + row - brushHeight / 2),
                                    this.map.fixColumn(this.columnUnderCursor + column - brushWidth / 2))
                            )
            );
        }
    }

    private void drawCalculatedCell(int row, int column) {
        GraphicsContext gc = this.canvas.getGraphicsContext2D();
        boolean isAlive = this.map.isSet(row, column);
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
        return this.map != null ? this.map.getWidth() : 0;
    }

    private int getFieldHeight() {
        return this.map != null ? this.map.getHeight() : 0;
    }

}
