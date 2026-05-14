package de.tuberlin.tablut.ai;

import java.util.ArrayList;
import java.util.LinkedList;

public class ABResult {
    int value;
    ArrayList<Move> trace;
//    Trace trace;

    public ABResult(int value, ArrayList<Move> trace) {
        this.value = value;
        this.trace = trace;
    }
}
