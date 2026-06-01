package de.tuberlin.tablut.ai.SearchAlgorithms.TranspositionTable;

import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.SearchAlgorithms.ABResult;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TranspositionEntry {
    final int depth;
    final int value;
    final Bound bound;
    final ArrayList<Move> trace;

    public TranspositionEntry(int depth, int value, Bound bound, List<Move> trace) {
        this.depth = depth;
        this.value = value;
        this.bound = bound;
        this.trace = trace == null ? null : new ArrayList<>(trace);
    }

    public ABResult toResult() {
        return new ABResult(value, trace == null ? null : new ArrayList<>(trace));
    }
}
