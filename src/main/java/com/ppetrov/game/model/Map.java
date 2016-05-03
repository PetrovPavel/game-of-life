package com.ppetrov.game.model;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class Map {

    private Boolean[][] field;

    public Map(Boolean[][] field) {
        this.field = deepArrayCopy(field);
    }

    public Map(int width, int height) {
        this.field = new Boolean[height][width];
        fillRandomly();
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
        return this.field[fixRow(row)][fixColumn(column)];
    }

    public void setCell(int row, int column, boolean value) {
        this.field[fixRow(row)][fixColumn(column)] = value;
    }

    public Boolean[][] getField() {
        return deepArrayCopy(this.field);
    }

    public void fillRandomly() {
        Random random = new Random();
        IntStream.range(0, this.field.length)
                .forEach(row -> IntStream.range(0, this.field[row].length)
                        .forEach(column -> this.field[row][column] = random.nextDouble() < 0.25)
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

    private Boolean[][] deepArrayCopy(Boolean[][] source) {
        Boolean[][] copy = new Boolean[source.length][];
        IntStream.range(0, source.length).forEach(
                row -> copy[row] = Arrays.copyOf(source[row], source[row].length)
        );
        return copy;
    }

}
