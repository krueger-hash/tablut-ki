package de.tuberlin.tablut.ai.SearchAlgorithms;

import de.tuberlin.tablut.ai.Move;

import java.util.List;


public record SearchReport(
        Move bestMove,
        int value,
        int depth,
        long positions,
        long leafs,
        long millis,
        boolean completed,
        List<Move> bestPath
) {
    public double seconds() {
        return millis / 1000.0;
    }
}
