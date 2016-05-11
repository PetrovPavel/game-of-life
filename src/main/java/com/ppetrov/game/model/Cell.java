package com.ppetrov.game.model;

public class Cell {

    private final boolean alive;
    private final boolean immortal;
    private final int age;

    public Cell(boolean alive) {
        this(alive, false, 0);
    }

    public Cell(boolean alive, boolean immortal, int age) {
        this.alive = alive;
        this.immortal = immortal;
        this.age = age;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public Cell setAlive(boolean alive) {
        return new Cell(alive, false, this.alive != alive ? 0 : this.age);
    }

    public int getAge() {
        return this.age;
    }

    public Cell addYear() {
        return new Cell(this.alive, false, this.age + 1);
    }

    public boolean isImmortal() {
        return this.immortal;
    }

    public Cell immortalize(boolean immortal) {
        return new Cell(this.alive, immortal, this.age);
    }

    public Cell getCopy() {
        return new Cell(this.alive, this.immortal, this.age);
    }

}
