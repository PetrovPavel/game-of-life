package com.ppetrov.game.viewer;

import com.ppetrov.game.model.RuleTemplate;
import com.ppetrov.game.model.Rules;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import rx.Observable;
import rx.observables.JavaFxObservable;

import java.util.stream.IntStream;

public class RulesPane extends VBox {

    private VBox bornSurvivesPane;
    private TogglePane bornPane;
    private TogglePane survivesPane;

    private VBox templatesPane;
    private ToggleGroup templatesGroup;
    private ToggleButton[] templateButtons;

    public RulesPane() {
        createBornSurvivesPane();
        createTemplatesPane();

        subscribeOnTemplatesChanges();
        subscribeOnRulesChanges();

        getChildren().addAll(this.bornSurvivesPane, this.templatesPane);
    }

    private void createBornSurvivesPane() {
        this.bornSurvivesPane = new VBox();

        this.bornPane = new TogglePane(9, 3);
        this.bornPane.select(3);

        this.survivesPane = new TogglePane(9, 3);
        this.survivesPane.select(2, 3);

        this.bornSurvivesPane.getChildren().addAll(
                new Label("Born:"), this.bornPane,
                new Label("Survives:"), this.survivesPane);
    }

    private void createTemplatesPane() {
        this.templatesPane = new VBox(new Label("Templates:"));
        this.templatesGroup = new ToggleGroup();

        RuleTemplate[] ruleTemplates = RuleTemplate.values();
        this.templateButtons = new ToggleButton[ruleTemplates.length];

        IntStream.range(0, this.templateButtons.length).
                mapToObj(index -> ruleTemplates[index]).
                forEach(this::createTemplateButton);
    }

    private void createTemplateButton(RuleTemplate template) {
        ToggleButton templateButton = new ToggleButton(template.getName() + "\n" + template.getRules());
        templateButton.setSelected(template.isDefault());
        templateButton.setToggleGroup(templatesGroup);
        templateButton.setMaxWidth(Integer.MAX_VALUE);
        templateButton.setTextAlignment(TextAlignment.CENTER);
        templateButton.setUserData(template);
        this.templatesPane.getChildren().add(templateButton);
    }

    private void subscribeOnRulesChanges() {
        getRulesChanges().subscribe(this::selectIfExists);
    }

    private void subscribeOnTemplatesChanges() {
        getTemplatesChanges().subscribe(template -> {
            Rules rules = template.getRules();
            this.bornPane.select(rules.getBorn());
            this.survivesPane.select(rules.getSurvives());
        });
    }

    public Observable<Rules> getRulesChanges() {
        return Observable.just(RuleTemplate.DEFAULT.getRules()).mergeWith(
                Observable.combineLatest(
                        this.bornPane.getSelectionChanges(),
                        this.survivesPane.getSelectionChanges(),
                        Rules::new
                )
        );
    }

    public void selectIfExists(Rules rules) {
        this.templatesGroup.getToggles().forEach(toggle -> {
            RuleTemplate template = (RuleTemplate) toggle.getUserData();
            toggle.setSelected(template.getRules().equals(rules));
        });
    }

    public Observable<RuleTemplate> getTemplatesChanges() {
        return JavaFxObservable.fromObservableValue(this.templatesGroup.selectedToggleProperty()).
                filter(toggle -> toggle != null).
                map(Toggle::getUserData).cast(RuleTemplate.class);
    }

}
