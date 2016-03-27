package com.ppetrov.game.model;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class Game {

    private Map map;

    private Timer timer;

    private boolean isPause = true;

    private Set<IGameListener> listeners;

    public Game() {
        this.map = new Map(50, 50);
        this.listeners = new HashSet<>();
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

    public void startNewMap(int width, int height) {
        this.map = new Map(width, height);
    }

    public void start() {
        this.isPause = false;
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                performStep();
            }
        }, 0, 1000);
    }

    public void pause() {
        this.timer.cancel();
        this.isPause = true;
    }

    public boolean isPause() {
        return this.isPause;
    }

    public void stop() {
        this.timer.cancel();
    }

    public void addListener(IGameListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(IGameListener listener) {
        this.listeners.remove(listener);
    }

    protected void performStep() {
        this.map.nextState();
        this.listeners.forEach(IGameListener::onGameStepPerformed);
    }

}
