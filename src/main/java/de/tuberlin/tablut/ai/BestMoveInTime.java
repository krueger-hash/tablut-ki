package de.tuberlin.tablut.ai;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BestMoveInTime{
//public class BestMoveInTime implements Runnable{

    private static final Player maxPlayer = BoardEvaluator.MAX_PLAYER;
    private static final Player minPlayer = BoardEvaluator.MIN_PLAYER;

    private static final int ALPHA_INIT = -1_000_000;
    private static final int BETA_INIT = 1_000_000;


    private volatile Move bestMove; //volatile Variable, damit Future sie überschreiben kann und ein Ergebnis auch bei Timeout geliefert wird
    private volatile int bestValue;

    private volatile Move bestMoveDuringIteration;
    private volatile int bestValueDuringIteration;

    private final CompletableFuture<Move> future;

    BestMoveInTime(Board originalState, int msTime) {
        Board state = Board.deepCopy(originalState);
        this.future = CompletableFuture.supplyAsync( ()-> {

            long tStart = System.currentTimeMillis();

            // * Logik von BestMove
            ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
            this.bestMove = moves.getFirst();
            this.bestValue = 0;// der erste Move erstmal als Ausgangspunkt

            //iterative Tiefensuche
            for (int depth = 1; ; depth++) {

                long iterationStart = System.currentTimeMillis();
                this.bestMoveAtDepth(state, moves, depth);
                long iterationEnd = System.currentTimeMillis();

                this.bestMove = this.bestMoveDuringIteration;
                this.bestValue = this.bestValueDuringIteration;

                //Abbruchbedingung sinnvoll?
                long remainingTime = msTime - (iterationEnd - tStart);
                long iterationTime = iterationEnd - iterationStart;
                if(iterationTime > remainingTime){break;}

            }

            return this.bestMove;
        }).orTimeout(msTime,TimeUnit.MILLISECONDS);
    }

    public Move getMove(){
        try {
            return future.join();
        } catch (Exception e) {
            return bestMove;
        }
    }

    public int getBestValueDuringIteration() {
        return bestValueDuringIteration;
    }
    public Move getBestMoveDuringIteration() {
        return bestMoveDuringIteration;
    }

    //Illustration der Anwendung
    static void main() {
        String fen ="3bbb3/4b4/4w4/b3w3b/bbwwKwwbb/b3w3b/4w4/4b4/3bbb3 S 48";
        Board test = Board.fenToBoard(fen);

        Move niceMove = new BestMoveInTime(test,1).getMove();

        //ungetestet :/
    }

    // während einer Suchtiefe wird der beste Move auf der Iterationsvariable gespeichert, damit bestMove nur basierend auf einer vollständig durchsuchten Ebene zurückgegeben wird;
    //bestValue benötigt keine intermediate Variable
    void bestMoveAtDepth(Board state, ArrayList<Move> moves, int depth){

        if (state.sideToMove != maxPlayer && state.sideToMove != minPlayer){
            throw new IllegalStateException("Übergebenes Board hat ungültige .sideToMove");
        }

        this.bestMoveDuringIteration = this.bestMove;
        boolean isMaxing = (state.sideToMove == maxPlayer);
        // bestValue muss auf jeder Suchtiefe neu initialisiert werden, da ggf. bei größerer Tiefe identische Züge schlechter bewertet werden können, als mit geringerer Tiefe
        if(isMaxing){this.bestValueDuringIteration = ALPHA_INIT;}
        else {this.bestValueDuringIteration = BETA_INIT;}

        //Tiefe 0 ist der Wurzelknoten. dort gibt es noch keine Moves
        if(depth == 0){
            this.bestValueDuringIteration = BoardEvaluator.evaluate(state);
            this.bestMove = null;
            return;
        }

        for (Move move : moves){
            state.makeMove(move);
            //Aufruf mit depth-1, da die erste Ebene (die moves) bereits generiert wurde; d.h. wird mit depth = 1 aufgerufen, wird der Wert des ersten Halbzugs ausgewertet
            int value = AlphaBeta.sortedAlphaBetaSearch(state,depth-1,ALPHA_INIT,BETA_INIT);
            state.unmakeMove();

            if(isMaxing) {
                if (value > this.bestValueDuringIteration) {
                    this.bestValueDuringIteration = value;
                    this.bestMoveDuringIteration = move;
                }
            }
            else{
                if (value < this.bestValueDuringIteration) {
                    this.bestValueDuringIteration = value;
                    this.bestMoveDuringIteration = move;
                }
            }
        }
        return;
    }


}
