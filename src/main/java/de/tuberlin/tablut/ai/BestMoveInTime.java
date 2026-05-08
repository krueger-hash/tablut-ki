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
    private final CompletableFuture<Move> future;

    BestMoveInTime(Board state, int depth, int msTime) {
        future = CompletableFuture.supplyAsync( ()-> {

            // * Logik von BestMove
            ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
            int bestValue = 0;
            this.bestMove = moves.getFirst();

            for (Move move : moves){
                ///TO-DO: Refactor auf makeMove
                Board newState = Board.boardAfterMove(state, move);
                int value = AlphaBeta.alphaBetaSearch(newState,depth,-9001,9001); // Aufruf des Alpha-Beta-Fensters ist typischerweise mit +/- unendlich, ~ 9000+ ist äquivalent

                // hier ggf mit unmake Move
                if(state.sideToMove == maxPlayer) {
                    if (value > bestValue) {
                        bestValue = value;
                        this.bestMove = move;
                    }
                }
                else if (state.sideToMove == minPlayer) {
                    if (value < bestValue) {
                        bestValue = value;
                        this.bestMove = move;
                    }
                }
                else {throw new IllegalStateException("Übergebenes Board hat ungültige .sideToMove");}
            }
            return this.bestMove;
        }).orTimeout(msTime,TimeUnit.MILLISECONDS);
    }

    public Move getMove(){
        try {
            return future.join();
        } catch (Exception e) {
            return this.bestMove;
        }
    }

    //Illustration der Anwendung
    static void main() {
        String fen ="3bbb3/4b4/4w4/b3w3b/bbwwKwwbb/b3w3b/4w4/4b4/3bbb3 S 48";
        Board test = Board.fenToBoard(fen);

        Move niceMove = new BestMoveInTime(test,5,1).getMove();

        //ungetestet :/
    }

}
