package de.tuberlin.tablut.ai.SearchAlgorithms;

import de.tuberlin.tablut.ai.Move;

public class Trace {
    Move move;
    Trace nextLink;

    public Trace(Move move, Trace nextLink) {
        this.move = move;
        this.nextLink = nextLink;
    }
}
