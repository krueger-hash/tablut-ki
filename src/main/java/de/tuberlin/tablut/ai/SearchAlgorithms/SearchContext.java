package de.tuberlin.tablut.ai.SearchAlgorithms;

import de.tuberlin.tablut.ai.BoardEvaluator;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.Piece;
import de.tuberlin.tablut.ai.SearchAlgorithms.TranspositionTable.TranspositionTable;
import lombok.Getter;

/**
 * Context for the search algorithm - contains context information over the whole iterative tree search
 * endTime - time limit
 * stopped - search has been stopped
 * leafs - number of leaf nodes
 * positions - number of positions evaluated
 */
public class SearchContext {
    private boolean stopped;

    @Getter
    private final long endTime;
    @Getter
    private long leafs;
    @Getter
    private long positions;

    //History Heuristik Matrizen
    private final int[][] historyHeuristicW = new int[90][90];
    private final int[][] historyHeuristicB = new int[90][90];
    private final int[][] historyHeuristicK = new int[90][90];

    // Transposition Table
    private final TranspositionTable transpositionTable = new TranspositionTable();

    TranspositionTable getTranspositionTable(){
        return transpositionTable;
    }

    // If time limit is exceeded
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
    // Default time limit of 1h for legacy test compatibility
    public SearchContext(){
        System.err.println("WARNING: Default time in SearchContext!");
        this(3600_000);
    }

    public SearchContext(int msTime){
        this.stopped = false;
        this.endTime = System.currentTimeMillis() + msTime;
        this.leafs = 0;
        this.positions = 0;

        //Initialisiere HistoryHeuristic Matrizen mit Nullwerten
        for(int from = 0; from < 90; from++){
            for(int to = 0; to <90; to++){
                this.historyHeuristicB[from][to]=0;
                this.historyHeuristicW[from][to]=0;
                this.historyHeuristicK[from][to]=0;
            }
        }
    }

    public void incrementLeafs() {
        this.leafs++;
    }

    public void incrementPositions() {
        this.positions++;
    }

    // History Heuristic - saves the depth of the move in the history heuristic matrix as square - whenever a cutoff occurs
    public void incrementHistoryHeuristic(Move move, int depth){
        int to = move.getTo();
        int from = move.getFrom();
        Piece piece =move.getMovedPiece();
        if(piece == Piece.BLACK){
            this.historyHeuristicB[from][to] += depth*depth;
        }
        else if(piece == Piece.WHITE){
            this.historyHeuristicW[from][to] -= depth*depth;
        }
        else if(piece == Piece.KING){
            this.historyHeuristicK[from][to] -= depth*depth;
        }
        else throw new RuntimeException("Undefined Piece for setting HistoryScore");
    }


    // Returns heuristics score for given move
    public int getHistoryHeuristicScore(Move move) {
        int to = move.getTo();
        int from = move.getFrom();
        int score;
        Piece piece =move.getMovedPiece();if(piece == Piece.BLACK){
            score = this.historyHeuristicB[from][to];
        }
        else if(piece == Piece.WHITE){
            score = this.historyHeuristicW[from][to];
        }
        else if(piece == Piece.KING){
            score = this.historyHeuristicK[from][to];
        }
        else throw new RuntimeException("Undefined Piece for getting HistoryScore");

        int CAPPED_SCORE = 60_000;
        if(Piece.BLACK == piece){
            return Math.min(score * BoardEvaluator.HISTORY_HEURISTIC_WEIGHT,CAPPED_SCORE);
        }else{
            return Math.max(score * BoardEvaluator.HISTORY_HEURISTIC_WEIGHT,-CAPPED_SCORE);
        }
    }
}
