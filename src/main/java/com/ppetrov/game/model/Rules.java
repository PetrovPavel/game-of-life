package com.ppetrov.game.model;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Rules {

    public static Rules DEFAULT = new Rules(new int[]{3}, new int[]{2, 3});

    private Set<Integer> born;
    private Set<Integer> survives;

    public Rules(int[] born, int[] survives) {
        this.born = Arrays.stream(born).boxed().collect(Collectors.toSet());
        this.survives = Arrays.stream(survives).boxed().collect(Collectors.toSet());
    }

    public Map nextState(Map map) {
        Boolean[][] nextStateField = map.getField();

        IntStream.range(0, map.getHeight()).forEach(
                row -> IntStream.range(0, map.getWidth()).forEach(
                        column -> {
                            int aliveNeighbours = countOfAliveNeighbours(map, row, column);
                            if (this.born.contains(aliveNeighbours) &&
                                    !map.isSet(row, column)) {
                                nextStateField[row][column] = true;
                            } else if (!this.survives.contains(aliveNeighbours)) {
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
                        filter(j -> !(i == row && j == column) && map.isSet(i, j)).
                        count()).
                mapToInt(Math::toIntExact).sum();
    }

}
