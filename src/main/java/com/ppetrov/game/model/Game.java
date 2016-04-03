package com.ppetrov.game.model;

import rx.Observable;

import java.util.concurrent.TimeUnit;

public class Game {

    private Map map;
    private IRules rules;
    private int speed;

    public Game(IRules rules) {
        this.map = new Map(50, 50);
        this.rules = rules;
        this.speed = 500;
    }

    public int getWidth() {
        return this.map.getWidth();
    }

    public int getHeight() {
        return this.map.getHeight();
    }

    public boolean getCell(int row, int column) {
        return this.map.getCell(row, column);
    }

    public void setCell(int row, int column, boolean isAlive) {
        this.map.setCell(row, column, isAlive);
    }

    public void fillMap() {
        this.map.fillRandomly();
    }

    public void clearMap() {
        this.map.clear();
    }

    public void startNewMap(int width, int height) {
        this.map = new Map(width, height);
    }

    public void setSpeed(int period) {
        this.speed = period;
    }

    public Observable<Map> start() {
        return Observable.interval(0, this.speed, TimeUnit.MILLISECONDS).
                map(tick -> nextStep());
    }

    public Map nextStep() {
        return this.map = this.rules.nextState(this.map);
    }

}
