package com.ppetrov.game.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class MapTest {

    @Test
    public void testIsEmpty() throws Exception {
        Map map = new Map(new Boolean[][]{{true, false}, {false, false}});
        assertFalse(map.isEmpty());
        map = new Map(new Boolean[][]{{false, false}, {false, false}});
        assertTrue(map.isEmpty());
    }

    @Test
    public void testNextState() throws Exception {
        Boolean[][] blockField = new Boolean[][]{
                {false, false, false, false},
                {false, true, true, false},
                {false, true, true, false},
                {false, false, false, false}};
        Map block = new Map(blockField);

        block.nextState();
        assertArrayEquals(blockField, block.getField());

        Boolean[][] blinkerField = new Boolean[][]{
                {false, false, false, false, false},
                {false, false, false, false, false},
                {false, true, true, true, false},
                {false, false, false, false, false},
                {false, false, false, false, false}
        };
        Boolean[][] blinkerFieldNextStep = new Boolean[][]{
                {false, false, false, false, false},
                {false, false, true, false, false},
                {false, false, true, false, false},
                {false, false, true, false, false},
                {false, false, false, false, false}
        };
        Map blinker = new Map(blinkerField);

        blinker.nextState();
        assertArrayEquals(blinkerFieldNextStep, blinker.getField());
        blinker.nextState();
        assertArrayEquals(blinkerField, blinker.getField());
    }

    @Test
    public void testGetCell() throws Exception {
        Map map = new Map(new Boolean[][]{{true, false}, {false, true}});
        assertTrue(map.getCell(-1, -1));
        assertTrue(map.getCell(2, 2));
    }

    @Test
    public void testSetCell() throws Exception {
        Map map = new Map(new Boolean[][]{{true, false}, {false, true}});
        map.setCell(0, 0, false);
        map.setCell(1, 1, false);
        assertFalse(map.getCell(0, 0));
        assertFalse(map.getCell(1, 1));
    }

}