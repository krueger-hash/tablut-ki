package de.tuberlin.tablut.ai;

import lombok.Getter;

public class SearchContext {

    private volatile boolean stopped;

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
    SearchContext(int msTime){
        this.stopped = false;
        this.endTime = System.currentTimeMillis() + msTime;
    }

}
