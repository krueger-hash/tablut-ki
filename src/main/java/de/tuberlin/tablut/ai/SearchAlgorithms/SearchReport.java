package de.tuberlin.tablut.ai.SearchAlgorithms;

import de.tuberlin.tablut.ai.Move;

import java.util.List;

/**
 * Stores all relevant information about a search
 * @param bestMove
 * @param value
 * @param depth
 * @param positions
 * @param leafs
 * @param millis
 * @param completed
 * @param bestPath
 */
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
