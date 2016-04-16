package com.ppetrov.game.model;

import rx.Observable;

import java.util.concurrent.TimeUnit;

public class Game {

    private IRules rules;
    private boolean paused;
    private int speed;

    public Game(IRules rules) {
        this.rules = rules;
        this.speed = 500;
    }

    public Observable<Map> startGame() {
        return Observable.interval(0, getSpeed(), TimeUnit.MILLISECONDS).
                filter(tick -> !this.paused).
                scan(getDefaultMap(), (currentMap, tick) -> this.rules.nextState(currentMap));
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void pauseResume() {
        this.paused = !this.paused;
    }

    public int getSpeed() {
        return this.speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    private static Map getDefaultMap() {
        return new Map(50, 50);
    }

}
