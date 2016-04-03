package com.ppetrov.game.model;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

public class Map {

    private Boolean[][] field;

    public Map(Boolean[][] field) {
        this.field = deepArrayCopy(field);
    }

    public Map(int width, int height) {
        this.field = new Boolean[height][width];
        clear();
    }

    public void clear() {
        Stream.of(this.field).forEach(row -> Arrays.fill(row, false));
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

    public boolean getCell(int row, int column) {
        row = fixRow(row);
        column = fixColumn(row, column);

        return this.field[row][column];
    }

    public void setCell(int row, int column, boolean value) {
        row = fixRow(row);
        column = fixColumn(row, column);

        this.field[row][column] = value;
    }

    public Boolean[][] getField() {
        return deepArrayCopy(this.field);
    }

    public void fillRandomly() {
        Random random = new Random();
        for (int i = 0; i < this.field.length; i++) {
            for (int j = 0; j < this.field[i].length; j++) {
                this.field[i][j] = random.nextDouble() < 0.25;
            }
        }
    }

    private int fixRow(int row) {
        if (row == -1) {
            row = this.field.length - 1;
        }
        if (row == this.field.length) {
            row = 0;
        }
        return row;
    }

    private int fixColumn(int row, int column) {
        if (column == -1) {
            column = this.field[row].length - 1;
        }
        if (column == this.field[row].length) {
            column = 0;
        }
        return column;
    }

    private Boolean[][] deepArrayCopy(Boolean[][] source) {
        Boolean[][] copy = new Boolean[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = Arrays.copyOf(source[i], source[i].length);
        }
        return copy;
    }

}
