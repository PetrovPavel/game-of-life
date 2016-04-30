package com.ppetrov.game.viewer;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import rx.Observable;
import rx.observables.JavaFxObservable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * TogglePane is a GridPane with ToggleButtons.
 * ToggleButtons are not in the same ToggleGroup,
 * so any amount of them can be selected at the same time.
 * All buttons are center-aligned both horizontally and vertically,
 * and always grow horizontally.
 */
public class TogglePane extends GridPane {

    private final ToggleButton[] buttons;
    private final int countInLine;

    /**
     * Create a TogglePane containing ToggleButtons
     *
     * @param count       amount of ToggleButtons
     * @param countInLine amount of ToggleButtons in one line
     */
    public TogglePane(int count, int countInLine) {
        this.buttons = new ToggleButton[count];
        this.countInLine = countInLine;
        create();
    }

    /**
     * Select toggle buttons with specified indexes
     *
     * @param numbers 1-based indexes of toggle buttons
     */
    public void select(int... numbers) {
        IntStream.of(numbers).forEach(number -> this.buttons[number - 1].setSelected(true));
    }

    /**
     * @return Observable that emits Set of currently selected buttons
     */
    public Observable<int[]> getSelectionChanges() {
        List<Observable<Integer>> buttonIndexes =
                IntStream.range(0, this.buttons.length).
                        mapToObj(i -> Observable.just(i + 1).mergeWith(
                                JavaFxObservable.fromActionEvents(this.buttons[i]).map(event -> i + 1))
                        ).collect(Collectors.toList());

        return Observable.combineLatest(
                buttonIndexes,
                numbers -> Stream.of(numbers).mapToInt(number -> (Integer) number).
                        filter(number -> this.buttons[number - 1].isSelected()).
                        toArray()
        );
    }

    private void create() {
        IntStream.range(0, this.buttons.length).forEach(
                i -> {
                    int number = i + 1;
                    this.buttons[i] = new ToggleButton(String.valueOf(number));

                    GridPane.setConstraints(this.buttons[i], i % this.countInLine, i / this.countInLine);
                    GridPane.setHalignment(this.buttons[i], HPos.CENTER);
                    GridPane.setValignment(this.buttons[i], VPos.CENTER);
                    GridPane.setHgrow(this.buttons[i], Priority.ALWAYS);

                    getChildren().add(this.buttons[i]);
                }
        );
    }

}
