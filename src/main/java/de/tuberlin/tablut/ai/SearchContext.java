package de.tuberlin.tablut.ai;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Stack;

public class SearchContext {

    final boolean isMaxing;
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
        System.err.println("WARNING: Default Time and Player BLACK in SearchContext!");
        this(3600_000,Player.BLACK); // default Zeitlimit 1h
    }
    SearchContext(int msTime){
        System.err.println("WARNING: Default BLACK in SearchContext!");
        this(msTime, Player.BLACK);
    }
    SearchContext(Player side){
        System.err.println("WARNING: Default time in SearchContext!");
        this(3600_000, side);
    }
    SearchContext(int msTime, Player current){
        this.stopped = false;
        this.endTime = System.currentTimeMillis() + msTime;
//        this.moveStack = new Stack<Move>();
//        this.bestSequence = new ArrayList<Move>();
        this.isMaxing = current == BoardEvaluator.MAX_PLAYER;
//        if(isMaxing){this.bestValueDuringIteration = ALPHA_INIT;}
//        else{this.bestValueDuringIteration = BETA_INIT;}
//        this.bestAB = null;
    }

//    public void resetBestValue(){
//        if(isMaxing){this.bestValueDuringIteration = ALPHA_INIT;}
//        else{this.bestValueDuringIteration = BETA_INIT;}
//    }

//    public void updateIfBetterValue(int value){
//        // besserer Wert für Max-Spieler
//        if (this.isMaxing && value > this.bestValueDuringIteration){
//            this.bestValueDuringIteration = value;
//            this.bestSequence = new ArrayList<Move>(moveStack);
//        }
//        // besserer Wert für Min-Spieler
//        if (!this.isMaxing && value < this.bestValueDuringIteration){
//            this.bestValueDuringIteration = value;
//            this.bestSequence = new ArrayList<Move>(moveStack);
//        }
//    }



}
