package de.tuberlin.tablut.ai;

import lombok.Getter;

import java.util.Stack;

public class SearchContext {

    private boolean stopped;

    public Stack<Move> bestSequence;

    @Getter
    private final long endTime;

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
    SearchContext(){
        this(3600_000); // default Zeitlimit 1h
    }
    SearchContext(int msTime){
        this.stopped = false;
        this.endTime = System.currentTimeMillis() + msTime;
        this.bestSequence = new Stack<Move>();
    }



}
