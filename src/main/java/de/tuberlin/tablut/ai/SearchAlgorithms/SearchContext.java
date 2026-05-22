package de.tuberlin.tablut.ai.SearchAlgorithms;

import lombok.Getter;

public class SearchContext {
    private boolean stopped;

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
