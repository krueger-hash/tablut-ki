package de.tuberlin.tablut.ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class AlphaBeta {

    private static final Player maxPlayer = BoardEvaluator.MAX_PLAYER;
    private static final Player minPlayer = BoardEvaluator.MIN_PLAYER;

    //nur Mock-Up, damit ich nicht die ganze Zeit rote Linien sehen muss
//    static int evaluateState(Board state){
//        return 0;
//    }

    ///////////////////////////////////////////////////////////////////////
    /// reine Alpha-Beta-Suche
    ///////////////////
    //Wrapper für den Aufruf der Alpha-Beta-Suche. Alternativ hätte man auch alles in eine Funktion mit Fallunterscheidung reinpacken können. Ich fand aber die Zweiteilung in Min-Max aber besser, da analog zur GKI-VL
    static int alphaBetaSearch(Board state, int depth, int alpha, int beta){
        if (state.sideToMove == maxPlayer){
            return alphaBetaMax(state,depth,alpha,beta);
        }
        if (state.sideToMove == minPlayer){
            return alphaBetaMin(state,depth,alpha,beta);
        }
        else {
            throw new IllegalStateException("Übergebenes Board hat ungültige .sideToMove");
        }
    }

    //Alpha-Beta-Max basierend auf GKI-VL WS2526 05A01 Folie 14
    static int alphaBetaMax(Board state, int depth, int alpha, int beta){
        if(depth == 0 || state.gameIsEnd()){
            return BoardEvaluator.evaluate(state);
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

    //Alpha-Beta-Min basierend auf GKI-VL WS2526 05A01 Folie 14
    static int alphaBetaMin(Board state, int depth, int alpha, int beta){
        if(depth == 0 || state.gameIsEnd()){
            return BoardEvaluator.evaluate(state);
        }
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

    ///////////////////////////////////////////////////////////////////////
    /// Alpha-Beta mit Zugsortierung
    ///////////////////
    static int sortedAlphaBetaSearch(Board state, int depth, int alpha, int beta){
        if(depth == 0 || state.gameIsEnd()){
            return BoardEvaluator.evaluate(state);
        }
        ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
        //Zugsortierung
        sortMoves(state, moves);

        // * Max-Player
        if (state.sideToMove == maxPlayer) {
        // Rekursiver Aufruf
            for (Move move : moves) {
                Board newState = Board.boardAfterMove(state, move);
                int score = sortedAlphaBetaSearch(newState, depth - 1, alpha, beta);
                if (score >= beta) {
                    return beta; //Cutoff
                }
                if (score > alpha) {
                    alpha = score;
                }
            }
            return alpha;
        }

        // * Min-Player
        else if (state.sideToMove == minPlayer){
            // Rekursiver Aufruf
            for (Move move : moves){
                Board newState = Board.boardAfterMove(state, move);
                int score = sortedAlphaBetaSearch(newState,depth-1,alpha,beta);
                if (score <= alpha){
                    return alpha; //Cutoff
                }
                if (score < beta){
                    beta = score;
                }
            }
            return beta;
        }
        else {
            throw new IllegalStateException("Übergebenes Board hat ungültige .sideToMove");
        }
    }

    static int evalMove(Move move, Board state){
        ArrayList<Hit> hits = state.makeMove(move);
        int result = BoardEvaluator.evaluate(state);
        //state.unmakeMove(move, hits);
        state.unmakeMove();
//        System.out.println("Move:"+move+" - Result:" +result);
//        System.out.println("Moves without Capture: " + state.movesWithoutCapture);
        return result;
    }

    static void sortMoves(Board state,ArrayList<Move> moves){
        Player sideToMove = state.sideToMove;

        Map<Move, Integer> scores = new HashMap<>();
        for (Move move : moves) {
            scores.put(move, evalMove(move, state));
        }

        //Zugsortierung absteigend
        if (sideToMove == maxPlayer) {
            moves.sort(
                    Comparator.comparingInt(
                            ((Move move) -> scores.get(move))
                    ).reversed()
            );
        }
        //Zugsortierung aufsteigend
        else {
            moves.sort(
                    Comparator.comparingInt(
                            ((Move move) -> scores.get(move))
                    )
            );
        }
    }
}
