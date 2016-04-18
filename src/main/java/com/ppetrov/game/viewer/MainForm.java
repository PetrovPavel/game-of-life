package com.ppetrov.game.viewer;

import com.ppetrov.game.model.DefaultRules;
import com.ppetrov.game.model.Game;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import rx.Observable;
import rx.Subscription;
import rx.observables.JavaFxObservable;
import rx.subscribers.JavaFxSubscriber;

public class MainForm extends Application {

    private FieldCanvas mainCanvas;
    private FieldCanvas templateCanvas;

    private Game game;
    private Subscription gameSubscription;

    public MainForm() {
        this.game = new Game(new DefaultRules());
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Game of Life");

        Pane root = new HBox();
        VBox leftPane = new VBox();
        root.getChildren().add(leftPane);

        createMainCanvas(leftPane);
        createSettingsPane(leftPane);
        createTemplatePane(root);

        startGame();

        HBox.setHgrow(leftPane, Priority.ALWAYS);

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        root.requestFocus();
    }

    public void createMainCanvas(VBox leftPane) {
        this.mainCanvas = new FieldCanvas(leftPane);
        this.mainCanvas.setPrefSize(500, 500);
        this.mainCanvas.setVGrow(Priority.ALWAYS);
    }

    @Override
    public void stop() throws Exception {
        stopGame();
        super.stop();
    }

    private void createSettingsPane(Pane parent) {
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

        parent.getChildren().add(settingsGroup);
    }

    private void createTemplatePane(Pane parent) {
        VBox templatePane = new VBox();
        parent.getChildren().add(templatePane);

        templatePane.getChildren().add(new Label("Template:"));

        this.templateCanvas = new FieldCanvas(templatePane);
        this.templateCanvas.setPrefSize(100, 100);
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
