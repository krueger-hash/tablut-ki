package de.tuberlin.tablut.ai;

public class Trace {
    Move move;
    Trace nextLink;

    public Trace(Move move, Trace nextLink) {
        this.move = move;
        this.nextLink = nextLink;
    }
}
