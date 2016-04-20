package com.ppetrov.game.model;

import com.ppetrov.game.observable.ObservableEx;
import rx.Observable;

import java.util.concurrent.TimeUnit;

public class Game {

    private IRules rules;
    private int speed;

    public Game(IRules rules) {
        this.rules = rules;
        this.speed = 500;
    }

    public Observable<Map> startGame(Observable<Boolean> pause, Observable<Boolean> next) {
        return ObservableEx.interval(this::getSpeed, TimeUnit.MILLISECONDS).
                withLatestFrom(pause, (tick, play) -> play).
                filter(Boolean::booleanValue).
                mergeWith(next).
                scan(getDefaultMap(), (currentMap, tick) -> this.rules.nextState(currentMap));
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
