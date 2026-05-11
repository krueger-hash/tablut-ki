package de.tuberlin.tablut.ai;

import java.util.ArrayList;

@Deprecated
public class BestMove {

    private static final Player maxPlayer = BoardEvaluator.MAX_PLAYER;
    private static final Player minPlayer = BoardEvaluator.MIN_PLAYER;

    static Move getBestMove(Board state,int depth){
        ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
        int bestValue = 0;
        Move bestMove = moves.getFirst();

        for (Move move : moves){

            state.makeMove(move);
            // Aufruf des Alpha-Beta-Fensters ist typischerweise mit +/- unendlich.
            int value = AlphaBeta.alphaBetaSearch(state,depth,-9001,9001);
            state.unmakeMove();

            if(state.sideToMove == maxPlayer) {
                if (value > bestValue) {
                    bestValue = value;
                    bestMove = move;
                }
            }
            else if (state.sideToMove == minPlayer) {
                if (value < bestValue) {
                    bestValue = value;
                    bestMove = move;
                }
            }
            else {throw new IllegalStateException("Übergebenes Board hat ungültige .sideToMove");}
        }
        return bestMove;
    }
}
