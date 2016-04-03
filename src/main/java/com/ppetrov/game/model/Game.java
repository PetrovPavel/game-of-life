package com.ppetrov.game.model;

import rx.Observable;

import java.util.concurrent.TimeUnit;

public class Game {

    private IRules rules;

    public Game(IRules rules) {
        this.rules = rules;
    }

    public Observable<Map> startGame(Map map, int speed) {
        return Observable.interval(0, speed, TimeUnit.MILLISECONDS).
                scan(map, (currentMap, tick) -> nextState(currentMap));
    }

    private Map nextState(Map currentMap) {
        return this.rules.nextState(currentMap);
    }

}
