package com.ppetrov.game.model;

public class Cell {

    public static int MAX_AGE = 10;

    private final boolean alive;
    private final int age;

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

    public Cell setAlive(boolean alive) {
        return new Cell(alive, this.alive != alive ? 0 : this.age);
    }

    public int getAge() {
        return this.age;
    }

    public Cell addYear() {
        if (this.age < MAX_AGE) {
            return new Cell(this.alive, this.age + 1);
        } else {
            return new Cell(false);
        }
    }

}
