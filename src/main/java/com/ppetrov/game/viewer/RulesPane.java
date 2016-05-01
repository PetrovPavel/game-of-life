package com.ppetrov.game.viewer;

import com.ppetrov.game.model.RuleTemplate;
import com.ppetrov.game.model.Rules;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import rx.Observable;
import rx.observables.JavaFxObservable;

import java.util.stream.IntStream;

public class RulesPane extends VBox {

    private final ToggleGroup toggleGroup;
    private final ToggleButton[] templateButtons;

    public RulesPane() {
        RuleTemplate[] ruleTemplates = RuleTemplate.values();
        this.templateButtons = new ToggleButton[ruleTemplates.length];

        this.toggleGroup = new ToggleGroup();
        IntStream.range(0, this.templateButtons.length).
                mapToObj(index -> ruleTemplates[index]).
                forEach(this::createTemplateButton);
    }

    private void createTemplateButton(RuleTemplate template) {
        ToggleButton templateButton = new ToggleButton(template.getName() + "\n" + template.getRules());
        templateButton.setSelected(template.isDefault());
        templateButton.setToggleGroup(toggleGroup);
        templateButton.setMaxWidth(Integer.MAX_VALUE);
        templateButton.setTextAlignment(TextAlignment.CENTER);
        templateButton.setUserData(template);
        getChildren().add(templateButton);
    }

    public Observable<RuleTemplate> getChanges() {
        return JavaFxObservable.fromObservableValue(this.toggleGroup.selectedToggleProperty()).
                filter(toggle -> toggle != null).
                map(Toggle::getUserData).cast(RuleTemplate.class);
    }

    public void selectIfExists(Rules rules) {
        this.toggleGroup.getToggles().forEach(toggle -> {
            RuleTemplate template = (RuleTemplate) toggle.getUserData();
            toggle.setSelected(template.getRules().equals(rules));
        });
    }

}
