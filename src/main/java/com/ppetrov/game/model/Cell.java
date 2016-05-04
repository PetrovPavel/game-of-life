package com.ppetrov.game.model;

public class Cell {

    private boolean alive;
    private int age;

    public Cell(boolean alive) {
        this(alive, 0);
    }

    public Cell(boolean alive, int age) {
        this.alive = alive;
        this.age = age;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public void setAlive(boolean alive) {
        if (this.alive != alive) {
            this.age = 0;
        }
        this.alive = alive;
    }

    public int getAge() {
        return this.age;
    }

    public void addYear() {
        this.age++;
    }

}
