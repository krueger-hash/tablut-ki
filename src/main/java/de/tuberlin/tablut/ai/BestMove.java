package de.tuberlin.tablut.ai;

import java.util.ArrayList;

public class BestMove {

    //erstmal hier eine globale Definition für min bzw. max spieler; vmtl besser in Bewertungsfunktion
    static final Player maxPlayer = Player.WHITE;
    static final Player minPlayer = Player.BLACK;


    // Übergabe von Parametern muss noch angepasst werden auf unterschiedliche Fälle; Ich würde hier in dieser Funktion die Fallunterscheidungen machen wollen. Vllt ein Parameter-Objekt übergeben? <~~ bezüglich anderer Suchstrategien/Modularisierung
    static Move getBestMove(Board state,int depth){
        ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
        int bestValue = 0;
        Move bestMove = moves.getFirst();

        for (Move move : moves){
            Board newState = Board.boardAfterMove(state, move); // Refactor auf makeMove
            // Aufruf des Alpha-Beta-Fensters ist typischerweise mit +/- unendlich.
            int value = AlphaBeta.alphaBetaSearch(newState,depth,-9001,9001);

            // hier ggf mit unmake Move
            if(state.sideToMove == BestMove.maxPlayer) {
                if (value > bestValue) {
                    bestValue = value;
                    bestMove = move;
                }
            }
            else if (state.sideToMove == BestMove.minPlayer) {
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
