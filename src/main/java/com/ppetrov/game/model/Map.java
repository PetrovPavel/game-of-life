package com.ppetrov.game.model;

import java.util.Random;
import java.util.stream.IntStream;

public class Map {

    private Cell[][] field;

    public Map(int width, int height) {
        this.field = new Cell[width][height];
        fillRandomly();
    }

    public Map(Boolean[][] field) {
        this.field = fillFromBoolean(field);
    }

    public Map(Cell[][] field) {
        this.field = deepArrayCopy(field);
    }

    public int getWidth() {
        if (this.field.length > 0) {
            return this.field[0].length;
        }
        return 0;
    }

    public int getHeight() {
        return this.field.length;
    }

    public boolean isSet(int row, int column) {
        return this.field[fixRow(row)][fixColumn(column)].isAlive();
    }

    public void setCell(int row, int column, boolean alive) {
        Cell cell = this.field[fixRow(row)][fixColumn(column)];
        this.field[fixRow(row)][fixColumn(column)] = cell.setAlive(alive);
    }

    public int getAge(int row, int column) {
        return this.field[fixRow(row)][fixColumn(column)].getAge();
    }

    public Cell[][] getField() {
        return deepArrayCopy(this.field);
    }

    public void fillRandomly() {
        Random random = new Random();
        IntStream.range(0, this.field.length)
                .forEach(row -> IntStream.range(0, this.field[row].length)
                        .forEach(column -> this.field[row][column] =
                                new Cell(random.nextDouble() < 0.25)
                        )
                );
    }

    public int fixRow(int row) {
        if (row < 0) {
            row = this.field.length + row;
        }
        if (row > this.field.length - 1) {
            row -= this.field.length;
        }
        return row;
    }

    public int fixColumn(int column) {
        if (column < 0) {
            column = this.field[0].length + column;
        }
        if (column > this.field[0].length - 1) {
            column -= this.field[0].length;
        }
        return column;
    }

    private Cell[][] fillFromBoolean(Boolean[][] source) {
        Cell[][] field = new Cell[source.length][source[0].length];
        IntStream.range(0, source.length).forEach(
                row -> IntStream.range(0, source[row].length).forEach(
                        column -> field[row][column] = new Cell(source[row][column])
                )
        );
        return field;
    }

    private Cell[][] deepArrayCopy(Cell[][] source) {
        Cell[][] copy = new Cell[source.length][source[0].length];
        IntStream.range(0, source.length).forEach(
                row -> IntStream.range(0, source[row].length).forEach(
                        column -> {
                            Cell sourceCell = source[row][column];
                            copy[row][column] = new Cell(sourceCell.isAlive(), sourceCell.getAge());
                        }
                )
        );
        return copy;
    }

}
