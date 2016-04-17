package com.ppetrov.game.model;

import com.ppetrov.game.observable.ObservableEx;
import rx.Observable;

import java.util.concurrent.TimeUnit;

public class Game {

    private IRules rules;
    private boolean paused;
    private boolean next;
    private int speed;

    public Game(IRules rules) {
        this.rules = rules;
        this.speed = 500;
    }

    public Observable<Map> startGame() {
        return ObservableEx.interval(this::getSpeed, TimeUnit.MILLISECONDS).
                filter(tick -> !isPaused()).
                mergeWith(ObservableEx.loopWhen(this::isNext)).
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

    public void step() {
        this.next = true;
    }

    private boolean isNext() {
        boolean next = this.next;
        if (next) {
            this.next = false;
        }
        return next;
    }

    private static Map getDefaultMap() {
        return new Map(50, 50);
    }

}
