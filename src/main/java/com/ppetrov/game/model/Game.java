package com.ppetrov.game.model;

import rx.Observable;

import java.util.concurrent.TimeUnit;

public class Game {

    private IRules rules;

    public Game(IRules rules) {
        this.rules = rules;
    }

    public Observable<Map> startGame(Observable<Integer> speed, Observable<Boolean> pause, Observable<Boolean> next) {
        return speed.switchMap(currentSpeed ->
                Observable.interval(currentSpeed, TimeUnit.MILLISECONDS)).
                withLatestFrom(pause, (tick, play) -> play).
                filter(Boolean::booleanValue).
                mergeWith(next).
                scan(getDefaultMap(), (currentMap, tick) -> this.rules.nextState(currentMap));
    }

    private static Map getDefaultMap() {
        return new Map(50, 50);
    }

}
