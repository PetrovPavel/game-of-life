package com.ppetrov.game.viewer;

import com.ppetrov.game.model.DefaultRules;
import com.ppetrov.game.model.Game;
import com.ppetrov.game.model.Map;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
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

    private int speed;

    private FieldCanvas mainCanvas;

    private Game game;
    private Map map;
    private Subscription gameSubscription;

    private Canvas templateCanvas;

    public MainForm() {
        this.game = new Game(new DefaultRules());
        this.speed = 500;
        this.map = new Map(50, 50);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Game of Life");

        createMainCanvas(primaryStage);
        Pane settingsPane = createSettingsPane();
        Pane templatePane = createTemplatePane();

        subscribeOnGame();

        ScrollPane mainCanvasPane = this.mainCanvas.getPane();
        Canvas canvas = this.mainCanvas.getCanvas();
        canvas.widthProperty().bind(
                mainCanvasPane.widthProperty().
                        subtract(settingsPane.getWidth()).
                        subtract(2)
        );
        canvas.heightProperty().bind(
                mainCanvasPane.heightProperty().
                        subtract(2)
        );

        VBox leftPane = new VBox();
        leftPane.getChildren().addAll(mainCanvasPane, settingsPane);
        VBox.setVgrow(mainCanvasPane, Priority.ALWAYS);

        Pane root = new HBox();
        root.getChildren().addAll(leftPane, templatePane);
        HBox.setHgrow(leftPane, Priority.ALWAYS);

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        root.requestFocus();
    }

    private void createMainCanvas(Stage primaryStage) {
        this.mainCanvas = new FieldCanvas(primaryStage);
        this.mainCanvas.setMap(this.map);
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
            Canvas canvas = this.mainCanvas.getCanvas();
            double cellSize = this.mainCanvas.getCellSize();
            this.map = new Map(
                    (int) (canvas.getWidth() / cellSize),
                    (int) (canvas.getHeight() / cellSize)
            );
            this.mainCanvas.setMap(this.map);
            reSubscribeOnGame();
            this.mainCanvas.redraw();
        });

        ToolBar toolBar = new ToolBar(
                pauseResumeButton,
                speedLabel, speedSlider,
                nextStepButton,
                restartButton
        );

        HBox settingsGroup = new HBox(toolBar);
        settingsGroup.setAlignment(Pos.CENTER);
        toolBar.setStyle("-fx-background-color:transparent;");
        return settingsGroup;
    }

    private Pane createTemplatePane() {
        Label templateLabel = new Label("Template:");

        this.templateCanvas = new Canvas(100, 100);
        this.mainCanvas.getCanvas().widthProperty().addListener(observable -> redrawTemplateCanvas());
        this.mainCanvas.getCanvas().heightProperty().addListener(observable -> redrawTemplateCanvas());

        VBox templatePane = new VBox();
        templatePane.getChildren().addAll(templateLabel, this.templateCanvas);

        return templatePane;
    }

    private void redrawTemplateCanvas() {
        GraphicsContext gc = this.templateCanvas.getGraphicsContext2D();
        gc.setFill(Color.GRAY);
        gc.fillRect(0, 0, this.templateCanvas.getWidth(), this.templateCanvas.getHeight());
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
                    this.mainCanvas.setMap(this.map);
                    Platform.runLater(this.mainCanvas::redraw);
                });
    }

    private void nextStep() {
        startGame().take(2).
                subscribe(map -> {
                    this.map = map;
                    this.mainCanvas.setMap(this.map);
                    Platform.runLater(this.mainCanvas::redraw);
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

}
