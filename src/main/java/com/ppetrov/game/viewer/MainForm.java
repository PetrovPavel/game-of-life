package com.ppetrov.game.viewer;

import com.ppetrov.game.model.DefaultRules;
import com.ppetrov.game.model.Game;
import com.ppetrov.game.model.Map;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import rx.Observable;
import rx.Subscription;
import rx.observables.JavaFxObservable;
import rx.subscribers.JavaFxSubscriber;

public class MainForm extends Application {

    private Canvas canvas;
    private int speed;

    private Game game;
    private Map map;
    private Subscription gameSubscription;

    private Integer columnUnderCursor;
    private Integer rowUnderCursor;

    public MainForm() {
        this.game = new Game(new DefaultRules());
        this.speed = 500;
        this.map = new Map(50, 50);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Game of Life");

        ScrollPane canvasPane = createCanvasPane(primaryStage);
        Pane settingsGroup = createSettingsPane();

        subscribeOnGame();

        this.canvas.widthProperty().bind(
                canvasPane.widthProperty().
                        subtract(settingsGroup.getWidth()).
                        subtract(2)
        );
        this.canvas.heightProperty().bind(
                canvasPane.heightProperty().
                        subtract(2)
        );

        Pane root = new VBox();
        root.getChildren().addAll(canvasPane, settingsGroup);
        VBox.setVgrow(canvasPane, Priority.ALWAYS);

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        root.requestFocus();
    }

    private ScrollPane createCanvasPane(Stage primaryStage) {
        ScrollPane canvasPane = new ScrollPane();
        createCanvas(primaryStage);

        canvasPane.setContent(this.canvas);
        canvasPane.setStyle("-fx-background-color:transparent;");
        canvasPane.setPrefSize(500, 500);

        return canvasPane;
    }

    private void createCanvas(Stage primaryStage) {
        this.canvas = new Canvas();
        this.canvas.widthProperty().addListener(observable -> redraw());
        this.canvas.heightProperty().addListener(observable -> redraw());

        EventHandler<MouseEvent> canvasMouseEventHandler = event -> {
            double x = event.getX();
            double y = event.getY();
            boolean inDrawingArea = isInDrawingArea(x, y);
            primaryStage.getScene().setCursor(inDrawingArea ? Cursor.HAND : Cursor.DEFAULT);
            this.rowUnderCursor = getCellRowFromCanvas(y);
            this.columnUnderCursor = getCellColumnFromCanvas(x);
            redraw();
        };
        this.canvas.setOnMouseEntered(canvasMouseEventHandler);
        this.canvas.setOnMouseMoved(canvasMouseEventHandler);
        this.canvas.setOnMouseExited(event -> {
            primaryStage.getScene().setCursor(Cursor.DEFAULT);
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

    private Pane createSettingsPane() {
        ImageView pauseImageView = new ImageView("/pause.png");
        ImageView resumeImageView = new ImageView("/resume.png");

        Button pauseResumeButton = new Button();
        Tooltip pauseResumeTooltip = new Tooltip("Pause");
        pauseResumeButton.setTooltip(pauseResumeTooltip);
        pauseResumeButton.setGraphic(pauseImageView);
        pauseResumeButton.setOnAction(event -> {
            if (isSubscribedOnGame()) {
                unsubscribeFromGame();
                pauseResumeTooltip.setText("Resume");
                pauseResumeButton.setGraphic(resumeImageView);
            } else {
                subscribeOnGame();
                pauseResumeTooltip.setText("Pause");
                pauseResumeButton.setGraphic(pauseImageView);
            }
        });

        Label speedLabel = new Label();
        Slider speedSlider = new Slider(-1000, -100, -500);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(100);
        speedSlider.setBlockIncrement(100);
        speedSlider.setTooltip(new Tooltip("Speed"));
        Observable<Integer> sliderProperty =
                JavaFxObservable.fromObservableValue(speedSlider.valueProperty()).
                        map(change -> Math.abs(change.intValue()));
        sliderProperty.subscribe(speed -> {
            this.speed = speed;
            reSubscribeOnGame();
        });
        speedLabel.textProperty().bind(JavaFxSubscriber.toBinding(
                sliderProperty.map(this::getSpeedInSecondsString)
        ));

        Button nextStepButton = new Button();
        nextStepButton.setTooltip(new Tooltip("Next Step"));
        nextStepButton.setGraphic(new ImageView("/step.png"));
        nextStepButton.setOnAction(event -> nextStep());

        Button restartButton = new Button();
        restartButton.setTooltip(new Tooltip("Restart Game"));
        restartButton.setGraphic(new ImageView("/restart.png"));
        restartButton.setOnAction(event -> {
            this.map = new Map(
                    (int) (this.canvas.getWidth() / getCellSize()),
                    (int) (this.canvas.getHeight() / getCellSize())
            );
            reSubscribeOnGame();
            redraw();
        });

        ToolBar toolBar = new ToolBar(
                pauseResumeButton,
                speedLabel, speedSlider,
                nextStepButton, new Separator(),
                restartButton
        );

        HBox settingsGroup = new HBox(toolBar);
        settingsGroup.setAlignment(Pos.CENTER);
        toolBar.setStyle("-fx-background-color:transparent;");
        return settingsGroup;
    }

    private String getSpeedInSecondsString(double speedInMillis) {
        return String.format("%.2fs", speedInMillis / 1000);
    }

    private Observable<Map> startGame() {
        return this.game.startGame(this.map, this.speed);
    }

    private void reSubscribeOnGame() {
        if (isSubscribedOnGame()) {
            reSubscribeOnGame(startGame());
        }
    }

    private void subscribeOnGame() {
        subscribeOnGame(startGame());
    }

    private void reSubscribeOnGame(Observable<Map> observable) {
        unsubscribeFromGame();
        subscribeOnGame(observable);
    }

    private void subscribeOnGame(Observable<Map> observable) {
        this.gameSubscription = observable.
                subscribe(map -> {
                    this.map = map;
                    Platform.runLater(this::redraw);
                });
    }

    private void nextStep() {
        startGame().take(2).
                subscribe(map -> {
                    this.map = map;
                    Platform.runLater(this::redraw);
                });
    }

    private void unsubscribeFromGame() {
        if (isSubscribedOnGame()) {
            this.gameSubscription.unsubscribe();
        }
    }

    private boolean isSubscribedOnGame() {
        return this.gameSubscription != null && !this.gameSubscription.isUnsubscribed();
    }

    private void clearRowsUnderCursor() {
        this.rowUnderCursor = null;
        this.columnUnderCursor = null;
        redraw();
    }

    private void redraw() {
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
