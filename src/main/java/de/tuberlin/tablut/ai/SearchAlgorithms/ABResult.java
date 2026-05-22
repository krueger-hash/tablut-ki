package de.tuberlin.tablut.ai.SearchAlgorithms;

import de.tuberlin.tablut.ai.Move;
import lombok.Getter;

import java.util.List;

public class ABResult {
    @Getter
    int value;
    List<Move> trace;

    public ABResult(int value, List<Move> trace) {
        this.value = value;
        this.trace = trace;
    }

    public List<Move> getTrace() {
        return trace;
    }

    public Move getBestMoveAtNode() {
        if (trace == null || trace.isEmpty()) {
            return null;
        }
        return trace.getLast();
    }
}
