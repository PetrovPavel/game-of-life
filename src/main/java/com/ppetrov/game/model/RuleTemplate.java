package com.ppetrov.game.model;

public enum RuleTemplate {

    DEFAULT("Life", new Rules(new int[]{3}, new int[]{2, 3})),
    SEEDS("Seeds", new Rules(new int[]{2}, new int[]{})),
    LIFE_34("34 Life", new Rules(new int[]{3, 4}, new int[]{3, 4})),
    DIAMOEBA("Diamoeba", new Rules(new int[]{3, 5, 6, 7, 8}, new int[]{5, 6, 7, 8})),
    TWO_X_TWO("2x2", new Rules(new int[]{3, 6}, new int[]{1, 2, 5})),
    HIGH_LIFE("High Life", new Rules(new int[]{3, 6}, new int[]{2, 3})),
    DAY_AND_NIGHT("Day & Night", new Rules(new int[]{3, 6, 7, 8}, new int[]{3, 4, 6, 7, 8}));

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
