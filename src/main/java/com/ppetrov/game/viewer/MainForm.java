package com.ppetrov.game.viewer;

import com.ppetrov.game.model.Game;
import com.ppetrov.game.model.Map;
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
        primaryStage.show();
        root.requestFocus();

        startGame();
    }

    private void createMainCanvas(VBox leftPane) {
        this.mainCanvas = new FieldCanvas(leftPane);
        this.mainCanvas.setPrefSize(500, 500);
        VBox.setVgrow(this.mainCanvas, Priority.ALWAYS);
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
                JavaFxObservable.fromActionEvents(pauseButton).
                        map(event -> false).
                        mergeWith(
                                JavaFxObservable.fromActionEvents(resumeButton).map(event -> true)
                        )
        );

        Label speedLabel = new Label();
        Slider speedSlider = new Slider(-1000, -100, -500);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(100);
        speedSlider.setBlockIncrement(100);
        speedSlider.setTooltip(new Tooltip("Speed"));

        this.speed = JavaFxObservable.fromObservableValue(speedSlider.valueProperty()).
                map(change -> Math.abs(change.intValue()));
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

        parent.getChildren().add(tabPane);
    }

    private void createBrushTab(TabPane tabPane) {
        Tab brushTab = new Tab("Brush");
        brushTab.setClosable(false);

        VBox brushPane = new VBox();

        FieldCanvas brushCanvas = new FieldCanvas(brushPane);
        brushCanvas.setPrefSize(100, 100);
        brushCanvas.setMap(new Map(new Boolean[][]{
                {false, false, false, false, false},
                {false, false, false, false, false},
                {false, false, true, false, false},
                {false, false, false, false, false},
                {false, false, false, false, false}
        }));

        this.brushSubscription = brushCanvas.getMapChanges().subscribe(this.mainCanvas::setBrush);

        brushTab.setContent(brushPane);
        tabPane.getTabs().add(brushTab);
    }

    private void createRulesTab(TabPane tabPane) {
        Tab rulesTab = new Tab("Rules");
        rulesTab.setClosable(false);

        VBox rulesPane = new VBox();

        TogglePane bornPane = new TogglePane(8, 4);
        bornPane.select(3);

        TogglePane survivesPane = new TogglePane(8, 4);
        survivesPane.select(2, 3);

        rulesPane.getChildren().addAll(
                new Label("Born:"), bornPane,
                new Label("Survives:"), survivesPane);

        rulesTab.setContent(rulesPane);
        tabPane.getTabs().add(rulesTab);

        this.rules = Observable.just(Rules.DEFAULT).mergeWith(
                Observable.combineLatest(
                        bornPane.getSelectionChanges(),
                        survivesPane.getSelectionChanges(),
                        Rules::new
                )
        );
    }

    private String getSpeedInSecondsString(double speedInMillis) {
        return String.format("%.2fs", speedInMillis / 1000);
    }

    private void restartGame() {
        stopGame();
        startGame();
    }

    private void startGame() {
        this.gameSubscription = new Game().startGame(this.rules, this.speed, this.pause, this.next).
                subscribe(map -> {
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
