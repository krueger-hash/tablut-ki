package de.tuberlin.tablut.ai;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Stack;

public class SearchContext {
    private boolean stopped;

//    public Stack<Move> moveStack;
//    public ArrayList<Move> bestSequence;
//
//    public int bestValueDuringIteration;
//    public ABResult bestAB;

    private static final int ALPHA_INIT = -1_000_000;
    private static final int BETA_INIT = 1_000_000;


    @Getter
    private final long endTime;
    @Getter
    private long leafs;
    @Getter
    private long positions;

    public boolean shouldStop() {
        if(stopped){
            return true;
        }
        if(System.currentTimeMillis() >= endTime){
            stopped = true;
            return true;
        }
        return false;
    }
    public SearchContext(){
        System.err.println("WARNING: Default time in SearchContext!");
        this(3600_000);
    }
    public SearchContext(int msTime){
        this.stopped = false;
        this.endTime = System.currentTimeMillis() + msTime;
        this.leafs = 0;
        this.positions = 0;
    }

    public void incrementLeafs() {
        this.leafs++;
    }

    public void incrementPositions() {
        this.positions++;
    }


}
