package de.tuberlin.tablut.ai;

import java.util.ArrayList;

public class BestMove {


    //Achtung: gerade nur für einen Spieler gültig, da value nur maximiert wird!!

    static Move getBestMove(Board state,int depth){ // Übergabe von Parametern muss noch angepasst werden auf unterschiedliche Fälle; Ich würde hier in dieser Funktion die Fallunterscheidungen machen wollen. Vllt ein Parameter-Objekt übergeben?
        int bestValue = 0;
        Move bestMove = null;

        ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
        for (Move move : moves){
            Board newState = Board.boardAfterMove(state, move);
            int value = AlphaBeta.alphaBetaSearch(newState,depth,-9001,9001);
            if (value > bestValue){
                bestValue = value;
                bestMove = move;
            }
        }
        return bestMove;
    }
}
