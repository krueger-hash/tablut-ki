package de.tuberlin.tablut.ai;

import java.util.ArrayList;

public class BoardStates {
    public static Move move;
    public static ArrayList<Hit> hits;
    public static int movesWithoutHit;


    BoardStates(Move move, ArrayList<Hit> hits, int movesWithoutHit){
        BoardStates.movesWithoutHit = movesWithoutHit;
        BoardStates.move = move;
        BoardStates.hits = hits;
    }
}
