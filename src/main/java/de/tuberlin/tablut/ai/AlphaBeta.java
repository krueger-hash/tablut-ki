package de.tuberlin.tablut.ai;

import java.util.ArrayList;

public class AlphaBeta {

    //nur Mock-Up, damit ich nicht die ganze Zeit rote Linien sehen muss
    static int evaluateState(Board state){
        return 0;
    }

    //Wrapper für den Aufruf der Alpha-Beta-Suche. Alternativ hätte man auch alles in eine Funktion mit Fallunterscheidung reinpacken können. Ich fand aber die Zweiteilung in Min-Max aber besser, da analog zur GKI-VL
    static int alphaBetaSearch(Board state, int depth, int alpha, int beta){
        if (state.sideToMove == BestMove.maxPlayer){
            return alphaBetaMax(state,depth,alpha,beta);
        }
        if (state.sideToMove == BestMove.minPlayer){
            return alphaBetaMin(state,depth,alpha,beta);
        }
        else {
            throw new IllegalStateException("Übergebenes Board hat ungültige .sideToMove");
        }
    }

    //Alpha-Beta-Min basierend auf GKI-VL WS2526 05A01 Folie 14
    static int alphaBetaMax(Board state, int depth, int alpha, int beta){
        if(depth == 0 || state.gameIsEnd()){
            return evaluateState(state);
        }
        ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
        for (Move move : moves){
            Board newState = Board.boardAfterMove(state, move);
            int score = alphaBetaMin(newState,depth-1,alpha,beta); // Aufruf von ABMin
            if (score >= beta){
                return beta; //Cutoff
            }
            if (score > alpha){
                alpha = score;
            }
        }
        return alpha;
    }

    //Alpha-Beta-Max basierend auf GKI-VL WS2526 05A01 Folie 14
    static int alphaBetaMin(Board state, int depth, int alpha, int beta){
        if(depth == 0 || state.gameIsEnd()){
            return evaluateState(state);
        }
        int value = 9001;
        ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
        for (Move move : moves){
            Board newState = Board.boardAfterMove(state, move);
            int score = alphaBetaMax(newState,depth-1,alpha,beta); //Aufruf von ABMax
            if (score <= alpha){
                return alpha; //Cutoff
            }
            if (score < beta){
                beta = score;
            }
        }
        return beta;
    }
}
