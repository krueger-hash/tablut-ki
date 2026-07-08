package de.tuberlin.tablut.ai;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores information about the previous board
 * - What pieces have been moved
 * - Hit pieces
 * - Former moves without hit counter
 */
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
