package com.ppetrov.game.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Rules implements IRules {

    public static Rules DEFAULT = new Rules(new int[]{3}, new int[]{2, 3});

    private Set<Integer> born;
    private Set<Integer> survives;

    public Rules(int[] born, int[] survives) {
        this.born = Arrays.stream(born).boxed().collect(Collectors.toSet());
        this.survives = Arrays.stream(survives).boxed().collect(Collectors.toSet());
    }

    private Rules(Set<Integer> born, Set<Integer> survives) {
        this.born = born;
        this.survives = survives;
    }

    @Override
    public Map nextState(Map map) {
        Boolean[][] nextStateField = map.getField();

        IntStream.range(0, map.getHeight()).forEach(
                row -> IntStream.range(0, map.getWidth()).forEach(
                        column -> {
                            int aliveNeighbours = countOfAliveNeighbours(map, row, column);
                            if (this.born.contains(aliveNeighbours) &&
                                    !map.getCell(row, column)) {
                                nextStateField[row][column] = true;
                            } else if (!this.survives.contains(aliveNeighbours)) {
                                nextStateField[row][column] = false;
                            }
                        }
                )
        );

        return new Map(nextStateField);
    }

    @Override
    public IRules setBorn(int count, boolean born) {
        if (born && !this.born.contains(count)) {
            Set<Integer> bornCopy = new HashSet<>(this.born);
            bornCopy.add(count);
            return new Rules(bornCopy, this.survives);
        } else if (!born && this.born.contains(count)) {
            Set<Integer> bornCopy = new HashSet<>(this.born);
            bornCopy.remove(count);
            return new Rules(bornCopy, this.survives);
        }
        return this;
    }

    @Override
    public IRules setSurvives(int count, boolean survives) {
        if (survives && !this.survives.contains(count)) {
            Set<Integer> survivesCopy = new HashSet<>(this.survives);
            survivesCopy.add(count);
            return new Rules(this.born, survivesCopy);
        } else if (!survives && this.survives.contains(count)) {
            Set<Integer> survivesCopy = new HashSet<>(this.survives);
            survivesCopy.remove(count);
            return new Rules(this.born, survivesCopy);
        }
        return this;
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
