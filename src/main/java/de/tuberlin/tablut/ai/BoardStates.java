package de.tuberlin.tablut.ai;

import java.util.ArrayList;

public class BoardStates {
    public final Move move;
    public final ArrayList<Hit> hits;
    public final int movesWithoutHit;




    public BoardStates(Move move, ArrayList<Hit> hits, int movesWithoutHit) {
        this.move = move;
        this.hits = hits;
        this.movesWithoutHit = movesWithoutHit;
    }
}
