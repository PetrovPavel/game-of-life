package com.ppetrov.game.model;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Rules implements IRules {

    public static Rules DEFAULT = new Rules(new int[]{3}, new int[]{2, 3});

    private int[] born;
    private int[] survives;

    public Rules(int[] born, int[] survives) {
        this.born = Arrays.copyOf(born, born.length);
        this.survives = Arrays.copyOf(survives, survives.length);
    }

    public Map nextState(Map map) {
        Boolean[][] nextStateField = map.getField();

        IntStream.range(0, map.getHeight()).forEach(
                row -> IntStream.range(0, map.getWidth()).forEach(
                        column -> {
                            int aliveNeighbours = countOfAliveNeighbours(map, row, column);
                            if (IntStream.of(this.born).anyMatch(val -> val == aliveNeighbours) &&
                                    !map.getCell(row, column)) {
                                nextStateField[row][column] = true;
                            } else if (IntStream.of(this.survives).noneMatch(val -> val == aliveNeighbours)) {
                                nextStateField[row][column] = false;
                            }
                        }
                )
        );

        return new Map(nextStateField);
    }

    private int countOfAliveNeighbours(Map map, int row, int column) {
        return IntStream.rangeClosed(row - 1, row + 1).
                mapToLong(i -> IntStream.rangeClosed(column - 1, column + 1).
                        filter(j -> isNeighbour(map, row, column, i, j)).
                        count()).
                mapToInt(Math::toIntExact).sum();
    }

    private boolean isNeighbour(Map map, int row, int column, int i, int j) {
        return !(i == row && j == column) && map.getCell(i, j);
    }
}
