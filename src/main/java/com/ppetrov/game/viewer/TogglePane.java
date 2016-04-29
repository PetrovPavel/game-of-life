package com.ppetrov.game.viewer;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import rx.Observable;
import rx.observables.JavaFxObservable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TogglePane extends GridPane {

    private final ToggleButton[] buttons;
    private final int countInLine;

    /**
     * Creates a TogglePane containing ToggleButtons
     *
     * @param count       amount of ToggleButtons
     * @param countInLine amount of ToggleButtons in one line
     */
    public TogglePane(int count, int countInLine) {
        this.buttons = new ToggleButton[count];
        this.countInLine = countInLine;
        create();
    }

    public void select(int... numbers) {
        for (int number : numbers) {
            this.buttons[number - 1].setSelected(true);
        }
    }

    /**
     * @return Observable that emits Set of currently selected buttons
     */
    public Observable<Set<Integer>> getSelectionChanges() {
        List<Observable<Integer>> buttonSelections = new ArrayList<>();
        for (int i = 0; i < this.buttons.length; i++) {
            final int number = i + 1;
            buttonSelections.add(
                    Observable.just(number).mergeWith(
                            JavaFxObservable.fromActionEvents(this.buttons[i]).
                                    map(event -> number)
                    )
            );
        }
        return Observable.combineLatest(
                buttonSelections,
                numbers -> {
                    HashSet<Integer> selectedNumbers = new HashSet<>();
                    for (Object number : numbers) {
                        Integer castedNumber = (Integer) number;
                        if (this.buttons[castedNumber - 1].isSelected()) {
                            selectedNumbers.add(castedNumber);
                        }
                    }
                    return selectedNumbers;
                }
        );
    }

    private void create() {
        for (int i = 0; i < this.buttons.length; i++) {
            int number = i + 1;
            this.buttons[i] = new ToggleButton(String.valueOf(number));

            GridPane.setConstraints(this.buttons[i], i % this.countInLine, i / this.countInLine);
            GridPane.setHalignment(this.buttons[i], HPos.CENTER);
            GridPane.setValignment(this.buttons[i], VPos.CENTER);
            GridPane.setHgrow(this.buttons[i], Priority.ALWAYS);

            getChildren().add(this.buttons[i]);
        }
    }

}
