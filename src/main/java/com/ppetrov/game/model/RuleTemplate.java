package com.ppetrov.game.model;

public enum RuleTemplate {

    DEFAULT("Default", new Rules(new int[]{3}, new int[]{2, 3})),
    SEEDS("Seeds", new Rules(new int[]{2}, new int[]{})),
    LIFE_34("34 Life", new Rules(new int[]{3, 4}, new int[]{3, 4}));

    private String name;
    private Rules rules;

    RuleTemplate(String name, Rules rules) {
        this.name = name;
        this.rules = rules;
    }

    public String getName() {
        return this.name;
    }

    public Rules getRules() {
        return this.rules;
    }

    public boolean isDefault() {
        return DEFAULT.equals(this);
    }

}