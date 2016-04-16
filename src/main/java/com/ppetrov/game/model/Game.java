package com.ppetrov.game.model;

import rx.Observable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Game {

    private IRules rules;
    private AtomicBoolean paused;

    public Game(IRules rules) {
        this.rules = rules;
        this.paused = new AtomicBoolean(false);
    }

    public Observable<Map> startGame(Map map, int speed) {
        return Observable.interval(0, speed, TimeUnit.MILLISECONDS).
                filter(thick -> !this.paused.get()).
                scan(map, (currentMap, tick) -> nextState(currentMap));
    }

    public boolean isPaused() {
        return this.paused.get();
    }

    public void pause() {
        this.paused.set(true);
    }

    public void resume() {
        this.paused.set(false);
    }

    private Map nextState(Map currentMap) {
        return this.rules.nextState(currentMap);
    }

}
