package com.ppetrov.game.model;

import rx.Observable;

import java.util.concurrent.TimeUnit;

public class Game {

    public Observable<Map> startGame(Observable<Rules> rules,
                                     Observable<Integer> speed,
                                     Observable<Boolean> pause,
                                     Observable<Boolean> next) {
        return speed
                .switchMap(currentSpeed -> Observable.interval(currentSpeed, TimeUnit.MILLISECONDS))
                .withLatestFrom(pause, (tick, play) -> play)
                .filter(Boolean::booleanValue)
                .mergeWith(next)
                .withLatestFrom(rules, (play, currentRules) -> currentRules)
                .scan(getDefaultMap(), (currentMap, currentRules) -> currentRules.nextState(currentMap));
    }

    private static Map getDefaultMap() {
        return new Map(50, 50);
    }

}
