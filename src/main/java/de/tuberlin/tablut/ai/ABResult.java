package de.tuberlin.tablut.ai;

import java.util.ArrayList;

public class ABResult {
    int value;
    ArrayList<Move> trace;
    SearchContext context;
//    Trace trace;

    public ABResult(int value, ArrayList<Move> trace) {
        this(value, trace, null);
    }

    public ABResult(int value, ArrayList<Move> trace, SearchContext context) {
        this.value = value;
        this.trace = trace;
        this.context = context;
    }

    public int getValue() {
        return value;
    }

    public ArrayList<Move> getTrace() {
        return trace;
    }

    public SearchContext getContext() {
        return context;
    }

    public Move getBestMove() {
        if (trace == null || trace.isEmpty()) {
            return null;
        }
        return trace.getLast();
    }
}
