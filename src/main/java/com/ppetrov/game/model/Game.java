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

    public Observable<Map> startGame(Observable<Boolean> pauseObservable,
                                     Observable<Boolean> nextStepObservable) {
        return Observable.combineLatest(
                ObservableEx.interval(this::getSpeed, TimeUnit.MILLISECONDS),
                pauseObservable,
                (tick, play) -> play
        ).filter(Boolean::booleanValue).
                mergeWith(nextStepObservable).
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
