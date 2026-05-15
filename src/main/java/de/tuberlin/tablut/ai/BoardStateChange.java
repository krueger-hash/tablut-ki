package de.tuberlin.tablut.ai;

import java.util.ArrayList;
import java.util.List;

public class BoardStateChange {
    public Move move;
    public List<Hit> hits;
    public int formerMovesWithoutHit;


    BoardStateChange(Move move, List<Hit> hits, int formerMovesWithoutHit){
        this.formerMovesWithoutHit = formerMovesWithoutHit;
        this.move = move;
        this.hits = hits;
    }
}
