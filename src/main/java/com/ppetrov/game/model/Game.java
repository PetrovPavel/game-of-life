package com.ppetrov.game.model;

import rx.Observable;

import java.util.concurrent.TimeUnit;

public class Game {

    private Map map = new Map(50, 50);

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

    public void startNewMap(int width, int height) {
        this.map = new Map(width, height);
    }

    public Observable<Long> start() {
        return Observable.
                interval(0, 1, TimeUnit.SECONDS).
                doOnEach(tick -> this.map.nextState());
    }

}
