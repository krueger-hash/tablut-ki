package de.tuberlin.tablut.ai;

import lombok.Getter;


public record SearchReport(
        Move bestMove,
        int value,
        int depth,
        long positions,
        long leafs,
        long millis,
        boolean completed,
        ABResult result
) {
    public double seconds() {
        return millis / 1000.0;
    }
}
