package com.ppetrov.game.model;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by Pavel on 22.03.2016.
 */
public class Map {

    private boolean[][] field;

    public Map(boolean[][] field) {
        this.field = deepArrayCopy(field);
    }

    public Map(int width, int height) {
        this.field = new boolean[width][height];
        fillRandomly();
    }

    public boolean isEmpty() {
        for (boolean[] row : this.field) {
            for (boolean cell : row) {
                if (cell) {
                    return false;
                }
            }
        }
        return true;
    }

    public void nextState() {
        boolean[][] prevStateField = deepArrayCopy(this.field);

        for (int i = 0; i < prevStateField.length; i++) {
            for (int j = 0; j < prevStateField[i].length; j++) {
                int aliveNeighbours = countOfAliveNeighbours(prevStateField, i, j);
                if (aliveNeighbours < 2 || 3 < aliveNeighbours) {
                    setCell(i, j, false);
                } else if (aliveNeighbours == 3 && !this.field[i][j]) {
                    setCell(i, j, true);
                }
            }
        }
    }

    public boolean getCell(int row, int column) {
        return getCell(this.field, row, column);
    }

    private boolean getCell(boolean[][] field, int row, int column) {
        row = fixRow(row);
        column = fixColumn(row, column);

        return field[row][column];
    }

    public boolean[][] getField() {
        return deepArrayCopy(this.field);
    }

    private void fillRandomly() {
        Random random = new Random();
        for (int i = 0; i < this.field.length; i++) {
            for (int j = 0; j < this.field[i].length; j++) {
                this.field[i][j] = random.nextDouble() < 0.25;
            }
        }
    }

    private int countOfAliveNeighbours(boolean[][] field, int row, int column) {
        int count = 0;

        if (getCell(field, row - 1, column - 1)) {
            count++;
        }
        if (getCell(field, row - 1, column)) {
            count++;
        }
        if (getCell(field, row - 1, column + 1)) {
            count++;
        }
        if (getCell(field, row, column - 1)) {
            count++;
        }
        if (getCell(field, row, column + 1)) {
            count++;
        }
        if (getCell(field, row + 1, column - 1)) {
            count++;
        }
        if (getCell(field, row + 1, column)) {
            count++;
        }
        if (getCell(field, row + 1, column + 1)) {
            count++;
        }

        return count;
    }

    private void setCell(int row, int column, boolean value) {
        row = fixRow(row);
        column = fixColumn(row, column);

        this.field[row][column] = value;
    }

    private int fixRow(int row) {
        if (row < 0) {
            row = this.field.length - 1;
        }
        if (row > this.field.length - 1) {
            row = 0;
        }
        return row;
    }

    private int fixColumn(int row, int column) {
        if (column < 0) {
            column = this.field[row].length - 1;
        }
        if (column > this.field[row].length - 1) {
            column = 0;
        }
        return column;
    }

    private boolean[][] deepArrayCopy(boolean[][] source) {
        boolean[][] copy = new boolean[source.length][];
        for (int i = 0; i < source.length; i++) {
            copy[i] = Arrays.copyOf(source[i], source[i].length);
        }
        return copy;
    }

}
