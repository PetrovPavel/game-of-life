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
     * @param indexes 0-based indexes of toggle buttons
     */
    public void select(int... indexes) {
        IntStream.of(indexes).forEach(index -> this.buttons[index].setSelected(true));
    }

    /**
     * @return Observable that emits Set of currently selected buttons
     */
    public Observable<int[]> getSelectionChanges() {
        List<Observable<Integer>> buttonIndexes =
                IntStream.range(0, this.buttons.length).
                        mapToObj(index -> Observable.just(index).mergeWith(
                                JavaFxObservable.fromActionEvents(this.buttons[index]).map(event -> index))
                        ).collect(Collectors.toList());

        return Observable.combineLatest(
                buttonIndexes,
                indexes -> Stream.of(indexes).mapToInt(index -> (Integer) index).
                        filter(index -> this.buttons[index].isSelected()).
                        toArray()
        );
    }

    private void create() {
        IntStream.range(0, this.buttons.length).forEach(
                index -> {
                    this.buttons[index] = new ToggleButton(String.valueOf(index));
                    this.buttons[index].setMaxWidth(Integer.MAX_VALUE);

                    GridPane.setConstraints(this.buttons[index], index % this.countInLine, index / this.countInLine);
                    GridPane.setHalignment(this.buttons[index], HPos.CENTER);
                    GridPane.setValignment(this.buttons[index], VPos.CENTER);
                    GridPane.setHgrow(this.buttons[index], Priority.ALWAYS);

                    getChildren().add(this.buttons[index]);
                }
        );
    }

}
