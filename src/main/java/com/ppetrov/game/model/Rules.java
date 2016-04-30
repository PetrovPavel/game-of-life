package com.ppetrov.game.model;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Rules {

    private final Set<Integer> born;
    private final Set<Integer> survives;

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

    public int[] getBorn() {
        return ArrayUtils.toPrimitive(this.born.toArray(new Integer[this.born.size()]));
    }

    public int[] getSurvives() {
        return ArrayUtils.toPrimitive(this.survives.toArray(new Integer[this.survives.size()]));
    }

    private int countOfAliveNeighbours(Map map, int row, int column) {
        return IntStream.rangeClosed(row - 1, row + 1).
                mapToLong(i -> IntStream.rangeClosed(column - 1, column + 1).
                        filter(j -> !(i == row && j == column) && map.isSet(i, j)).
                        count()).
                mapToInt(Math::toIntExact).sum();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("B");
        this.born.forEach(sb::append);
        sb.append("/S");
        this.survives.forEach(sb::append);
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Rules)) {
            return false;
        } else if (obj == this) {
            return true;
        } else {
            Rules that = (Rules) obj;
            return this.born.equals(that.born) && this.survives.equals(that.survives);
        }
    }

}
