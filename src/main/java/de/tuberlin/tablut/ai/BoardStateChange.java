package de.tuberlin.tablut.ai;

import java.util.ArrayList;

public class BoardStateChange {
    public Move move;
    public ArrayList<Hit> hits;
    public int formerMovesWithoutHit;


    BoardStateChange(Move move, ArrayList<Hit> hits, int formerMovesWithoutHit){
        this.formerMovesWithoutHit = formerMovesWithoutHit;
        this.move = move;
        this.hits = hits;
    }
}
