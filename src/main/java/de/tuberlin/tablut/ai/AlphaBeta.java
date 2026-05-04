package de.tuberlin.tablut.ai;

import java.util.ArrayList;

public class AlphaBeta {

    static int evaluateState(Board state){
        return 0;
    }

    static int alphaBetaSearch(Board state, int depth, int alpha, int beta){
        if (state.sideToMove == Player.WHITE){
            return alphaBetaMax(state,depth,alpha,beta);
        }
        if (state.sideToMove == Player.BLACK){
            return alphaBetaMin(state,depth,alpha,beta);
        }
        else {
            throw new IllegalArgumentException("Kein Zugspieler definiert!");
        }
    }

    static int alphaBetaMax(Board state, int depth, int alpha, int beta){
        if(depth == 0 || state.gameIsEnd()){
            return evaluateState(state);
        }
        int value = -9001;
        ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
        for (Move move : moves){
            Board newState = Board.boardAfterMove(state, move);
            value = Math.max(value,alphaBetaMin(newState,depth-1,alpha,beta)); //korrekt? hier max von value und AB?
            alpha = Math.max(alpha, value); // geht das auch kürzer?
            if (alpha >= beta){ // Bedingung korrekt?
                break; //Cutoff
            }
        }
        return value; // return korrekt?
    }

    static int alphaBetaMin(Board state, int depth, int alpha, int beta){
        if(depth == 0 || state.gameIsEnd()){
            return evaluateState(state);
        }
        int value = 9001;
        ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
        for (Move move : moves){
            Board newState = Board.boardAfterMove(state, move);
            value = Math.min(value,alphaBetaMax(newState,depth-1,alpha,beta)); //korrekt? hier max von value und AB?
            alpha = Math.min(alpha, value); // geht das auch kürzer?
            if (beta <= alpha){ // Bedingung korrekt?
                break; //Cutoff
            }
        }
        return value; // return korrekt?
    }
}
