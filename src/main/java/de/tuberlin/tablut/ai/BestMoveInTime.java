package de.tuberlin.tablut.ai;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BestMoveInTime{
//public class BestMoveInTime implements Runnable{

    private static final Player maxPlayer = BoardEvaluator.MAX_PLAYER;
    private static final Player minPlayer = BoardEvaluator.MIN_PLAYER;


    private volatile Move bestMove; //volatile Variable, damit Future sie überschreiben kann und ein Ergebnis auch bei Timeout geliefert wird
    private volatile int bestValue;
    private final CompletableFuture<Move> future;

    BestMoveInTime(Board state, int msTime) {
        this.future = CompletableFuture.supplyAsync( ()-> {

            long tStart = System.currentTimeMillis();

            // * Logik von BestMove
            ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
            this.bestValue = 0;
            this.bestMove = moves.getFirst(); // der erste Move erstmal als Ausgangspunkt

            //iterative Tiefensuche
            for (int depth = 0; ; depth++) {
                long iterationStart = System.currentTimeMillis();
                this.bestMoveAtDepth(state, moves, depth);
                long iterationEnd = System.currentTimeMillis();

                //Abbruchbedingung sinnvoll?
                long remainingTime = msTime - (iterationEnd - tStart);
                long iterationTime = iterationEnd - iterationStart;
                if(iterationTime > remainingTime){break;}

            }

            return bestMove;
        }).orTimeout(msTime,TimeUnit.MILLISECONDS);
    }

    public Move getMove(){
        try {
            return future.join();
        } catch (Exception e) {
            return bestMove;
        }
    }

    //Illustration der Anwendung
    static void main() {
        String fen ="3bbb3/4b4/4w4/b3w3b/bbwwKwwbb/b3w3b/4w4/4b4/3bbb3 S 48";
        Board test = Board.fenToBoard(fen);

        Move niceMove = new BestMoveInTime(test,1).getMove();

        //ungetestet :/
    }
    void bestMoveAtDepth(Board state, ArrayList<Move> moves, int depth){
        for (Move move : moves){
            state.makeMove(move);
            int value = AlphaBeta.alphaBetaSearch(state,depth,-9001,9001); // Aufruf des Alpha-Beta-Fensters ist typischerweise mit +/- unendlich, ~ 9000+ ist äquivalent
            state.unmakeMove();

            if(state.sideToMove == maxPlayer) {
                if (value > bestValue) {
                    this.bestValue = value;
                    this.bestMove = move;
                }
            }
            else if (state.sideToMove == minPlayer) {
                if (value < bestValue) {
                    this.bestValue = value;
                    this.bestMove = move;
                }
            }
            else {throw new IllegalStateException("Übergebenes Board hat ungültige .sideToMove");}
        }

    }


}
