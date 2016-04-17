package com.ppetrov.game.viewer;

import com.ppetrov.game.model.DefaultRules;
import com.ppetrov.game.model.Game;
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

    private FieldCanvas mainCanvas;
    private Canvas templateCanvas;

    private Game game;
    private Subscription gameSubscription;

    public MainForm() {
        this.game = new Game(new DefaultRules());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Game of Life");

        createMainCanvas(primaryStage);
        Pane settingsPane = createSettingsPane();
        Pane templatePane = createTemplatePane();

        startGame();

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

    @Override
    public void stop() throws Exception {
        stopGame();
        super.stop();
    }

    private void createMainCanvas(Stage primaryStage) {
        this.mainCanvas = new FieldCanvas(primaryStage);
    }

    private Pane createSettingsPane() {
        ImageView pauseImageView = new ImageView("/pause.png");
        ImageView resumeImageView = new ImageView("/resume.png");

        Button pauseResumeButton = new Button();
        Tooltip pauseResumeTooltip = new Tooltip("Pause");
        pauseResumeButton.setTooltip(pauseResumeTooltip);
        pauseResumeButton.setGraphic(pauseImageView);
        pauseResumeButton.setOnAction(event -> {
            this.game.pauseResume();
            if (this.game.isPaused()) {
                pauseResumeTooltip.setText("Resume");
                pauseResumeButton.setGraphic(resumeImageView);
            } else {
                pauseResumeTooltip.setText("Pause");
                pauseResumeButton.setGraphic(pauseImageView);
            }
        });

        Label speedLabel = new Label();
        Slider speedSlider = new Slider(-1000, -100, -this.game.getSpeed());
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(100);
        speedSlider.setBlockIncrement(100);
        speedSlider.setTooltip(new Tooltip("Speed"));
        Observable<Integer> sliderProperty =
                JavaFxObservable.fromObservableValue(speedSlider.valueProperty()).
                        map(change -> Math.abs(change.intValue()));
        sliderProperty.subscribe(this.game::setSpeed);
        speedLabel.textProperty().bind(JavaFxSubscriber.toBinding(
                sliderProperty.map(this::getSpeedInSecondsString)
        ));

        Button nextStepButton = new Button();
        nextStepButton.setTooltip(new Tooltip("Next Step"));
        nextStepButton.setGraphic(new ImageView("/step.png"));
        nextStepButton.setOnAction(event -> this.game.step());

        Button restartButton = new Button();
        restartButton.setTooltip(new Tooltip("Restart Game"));
        restartButton.setGraphic(new ImageView("/restart.png"));
        restartButton.setOnAction(event -> restartGame());

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

    private void restartGame() {
        stopGame();
        startGame();
    }

    private void startGame() {
        this.gameSubscription = this.game.startGame().subscribe(map -> {
            this.mainCanvas.setMap(map);
            Platform.runLater(this.mainCanvas::redraw);
        });
    }

    private void stopGame() {
        if (this.gameSubscription != null && !this.gameSubscription.isUnsubscribed()) {
            this.gameSubscription.unsubscribe();
        }
    }

}
