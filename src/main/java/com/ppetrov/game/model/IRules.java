package com.ppetrov.game.model;

public interface IRules {

    Map nextState(Map map);
    IRules setBorn(int count, boolean born);
    IRules setSurvives(int count, boolean survives);

}
