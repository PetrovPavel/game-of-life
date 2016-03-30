package com.ppetrov.game.model;

import java.util.stream.IntStream;

public class DefaultRules implements IRules {

    public Map nextState(Map map) {
        Boolean[][] nextStateField = map.getField();

        for (int i = 0; i < map.getHeight(); i++) {
            for (int j = 0; j < map.getWidth(); j++) {
                int aliveNeighbours = countOfAliveNeighbours(map, i, j);
                if (aliveNeighbours < 2 || 3 < aliveNeighbours) {
                    nextStateField[i][j] = false;
                } else if (aliveNeighbours == 3 && !map.getCell(i, j)) {
                    nextStateField[i][j] = true;
                }
            }
        }

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
