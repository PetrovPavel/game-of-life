package com.ppetrov.game.viewer;

import com.ppetrov.game.model.Game;
import com.ppetrov.game.model.Rules;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Side;
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

    private Subscription gameSubscription;
    private Subscription brushSubscription;

    private Observable<Rules> rules;
    private Observable<Integer> speed;
    private Observable<Boolean> pause;
    private Observable<Boolean> next;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Game of Life");

        Pane root = new HBox();
        VBox leftPane = new VBox();
        root.getChildren().add(leftPane);

        createMainCanvas(leftPane);
        createGameFlowPane(leftPane);
        createSettingsPane(root);

        HBox.setHgrow(leftPane, Priority.ALWAYS);

        Scene scene = new Scene(root);
        scene.getStylesheets().add("styles/main.css");

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(750);
        primaryStage.setMinHeight(700);

        root.requestFocus();

        startGame();

        primaryStage.show();
    }

    private void createMainCanvas(VBox leftPane) {
        this.mainCanvas = new FieldCanvas();
        VBox.setVgrow(this.mainCanvas, Priority.ALWAYS);
        leftPane.getChildren().add(this.mainCanvas);
    }

    @Override
    public void stop() throws Exception {
        stopGame();
        super.stop();
    }

    private void createGameFlowPane(Pane parent) {
        Button resumeButton = new Button();
        resumeButton.setTooltip(new Tooltip("Resume"));
        resumeButton.setGraphic(new ImageView("/icons/resume.png"));

        Button pauseButton = new Button();
        pauseButton.setTooltip(new Tooltip("Pause"));
        pauseButton.setGraphic(new ImageView("/icons/pause.png"));

        this.pause = Observable.just(true).mergeWith(
                JavaFxObservable.fromActionEvents(pauseButton)
                        .map(event -> false)
                        .mergeWith(JavaFxObservable.fromActionEvents(resumeButton).map(event -> true))
        );

        Label speedLabel = new Label();
        Slider speedSlider = new Slider(-1000, -100, -500);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(100);
        speedSlider.setBlockIncrement(100);
        speedSlider.setTooltip(new Tooltip("Speed"));

        this.speed = JavaFxObservable.fromObservableValue(speedSlider.valueProperty())
                .map(change -> Math.abs(change.intValue()));
        speedLabel.textProperty().bind(
                JavaFxSubscriber.toBinding(speed.map(this::getSpeedInSecondsString))
        );

        Button nextStepButton = new Button();
        nextStepButton.setTooltip(new Tooltip("Next Step"));
        nextStepButton.setGraphic(new ImageView("/icons/step.png"));
        this.next = JavaFxObservable.fromActionEvents(nextStepButton).map(event -> true);

        Button restartButton = new Button();
        restartButton.setTooltip(new Tooltip("Restart Game"));
        restartButton.setGraphic(new ImageView("/icons/restart.png"));
        restartButton.setOnAction(event -> restartGame());

        ToolBar toolBar = new ToolBar(
                resumeButton, pauseButton,
                speedLabel, speedSlider,
                nextStepButton,
                restartButton
        );

        HBox settingsGroup = new HBox(toolBar);
        settingsGroup.setAlignment(Pos.CENTER);

        parent.getChildren().add(settingsGroup);
    }

    private void createSettingsPane(Pane parent) {
        TabPane tabPane = new TabPane();
        tabPane.setSide(Side.LEFT);

        createBrushTab(tabPane);
        createRulesTab(tabPane);
        createSettingsTab(tabPane);

        parent.getChildren().add(tabPane);
    }

    private void createBrushTab(TabPane tabPane) {
        Tab brushTab = new Tab("Brush");
        brushTab.setClosable(false);

        BrushPane brushPane = new BrushPane();
        this.brushSubscription = brushPane.getMapChanges().subscribe(this.mainCanvas::setBrush);

        brushTab.setContent(brushPane);
        tabPane.getTabs().add(brushTab);
    }

    private void createRulesTab(TabPane tabPane) {
        Tab rulesTab = new Tab("Rules");
        rulesTab.setClosable(false);

        RulesPane rulesPane = new RulesPane();
        this.rules = rulesPane.getRulesChanges();

        rulesTab.setContent(rulesPane);
        tabPane.getTabs().add(rulesTab);
    }

    private void createSettingsTab(TabPane tabPane) {
        Tab settingsTab = new Tab("Settings");
        settingsTab.setClosable(false);

        VBox settingsPane = new VBox();

        Label ageLabel = new Label("Max age:");
        Slider ageSlider = new Slider(0, 100, 20);
        ageSlider.setShowTickMarks(true);
        ageSlider.setMajorTickUnit(20);
        ageSlider.setBlockIncrement(20);

        JavaFxObservable.fromObservableValue(ageSlider.valueProperty())
                .map(Number::intValue);

        settingsPane.getChildren().addAll(ageLabel, ageSlider);

        settingsTab.setContent(settingsPane);
        tabPane.getTabs().add(settingsTab);
    }

    private String getSpeedInSecondsString(double speedInMillis) {
        return String.format("%.2fs", speedInMillis / 1000);
    }

    private void restartGame() {
        stopGame();
        startGame();
    }

    private void startGame() {
        this.gameSubscription = new Game().startGame(this.rules, this.speed, this.pause, this.next)
                .subscribe(map -> {
                    this.mainCanvas.setMap(map);
                    Platform.runLater(this.mainCanvas::redraw);
                });
    }

    private void stopGame() {
        unsubscribe(this.brushSubscription);
        unsubscribe(this.gameSubscription);
    }

    private void unsubscribe(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

}
