package de.tuberlin.tablut.ai.SearchAlgorithms;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.BoardEvaluator;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.Player;

import java.util.*;

public class AlphaBeta {

    private static final Player maxPlayer = BoardEvaluator.MAX_PLAYER;
    private static final Player minPlayer = BoardEvaluator.MIN_PLAYER;


    //nur Mock-Up, damit ich nicht die ganze Zeit rote Linien sehen muss
//    static int evaluateState(Board state){
//        return 0;
//    }

    /// ////////////////////////////////////////////////////////////////////
    /// reine Alpha-Beta-Suche
    /// ////////////////
    //Wrapper für den Aufruf der Alpha-Beta-Suche. Alternativ hätte man auch alles in eine Funktion mit Fallunterscheidung reinpacken können. Ich fand aber die Zweiteilung in Min-Max aber besser, da analog zur GKI-VL
//    static int alphaBetaSearch(Board state, int depth, int alpha, int beta) {
//        if (state.sideToMove == maxPlayer) {
//            return alphaBetaMax(state, depth, alpha, beta);
//        }
//        if (state.sideToMove == minPlayer) {
//            return alphaBetaMin(state, depth, alpha, beta);
//        } else {
//            throw new IllegalStateException("Übergebenes Board hat ungültige .sideToMove");
//        }
//    }
//
//    //Alpha-Beta-Max basierend auf GKI-VL WS2526 05A01 Folie 14
//    static int alphaBetaMax(Board state, int depth, int alpha, int beta) {
//        if (depth == 0 || state.gameIsEnd()) {
//            return BoardEvaluator.evaluate(state);
//        }
//        ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
//        for (Move move : moves) {
//
//            state.makeMove(move);
//            int score = alphaBetaMin(state, depth - 1, alpha, beta); // Aufruf von ABMin
//            state.unmakeMove(); //unmakeMove vor den returns!
//
//            if (score >= beta) {
//                return beta; //Cutoff
//            }
//            if (score > alpha) {
//                alpha = score;
//            }
//        }
//        return alpha;
//    }
//
//    //Alpha-Beta-Min basierend auf GKI-VL WS2526 05A01 Folie 14
//    static int alphaBetaMin(Board state, int depth, int alpha, int beta) {
//        if (depth == 0 || state.gameIsEnd()) {
//            return BoardEvaluator.evaluate(state);
//        }
//        ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
//        for (Move move : moves) {
//
//            state.makeMove(move);
//            int score = alphaBetaMax(state, depth - 1, alpha, beta); //Aufruf von ABMax
//            state.unmakeMove();
//
//            if (score <= alpha) {
//                return alpha; //Cutoff
//            }
//            if (score < beta) {
//                beta = score;
//            }
//        }
//        return beta;
//    }

    /// ////////////////////////////////////////////////////////////////////
    /// Alpha-Beta mit Zugsortierung
    /// ////////////////
    public static ABResult sortedAlphaBetaSearch(Board state, int depth, int alpha, int beta) {
        SearchContext context = new SearchContext();
        try {
            return sortedAlphaBetaSearch(state, depth, alpha, beta, context);
        } catch (SearchStoppedException sse) {
            sse.printStackTrace();
            return null;
        }
    }

    // bei Erreichen des Zeitlimits wird unmakeMove nicht! aufgerufen. Wir könnten das noch sauber beheben mit catch-Blöcken, aber es ist wohl besser mit Deep-Copy zu begin von BestMove
    public static ABResult sortedAlphaBetaSearch(Board state, int depth, int alpha, int beta, SearchContext context) throws SearchStoppedException {
        context.incrementPositions();
        if (context.shouldStop()) {throw new SearchStoppedException("Zeitlimit erreicht");}

        //*Initialisierung lokaler Variablen für Knoten
        boolean isMaxing;
        ArrayList<Move> bestPath = new ArrayList<Move>();
        if (state.sideToMove == BoardEvaluator.MAX_PLAYER){isMaxing = true;}
        else{isMaxing = false;}

        //*Rekursionsende in Terminal- oder Blattknoten
        if (depth == 0 || state.gameIsEnd()) {
            context.incrementLeafs();
            int value = BoardEvaluator.evaluate(state);
            return new ABResult(value,new ArrayList<Move>());
        }

        //*Erzeuge alle möglichen Züge
        ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
        //Zugsortierung
        sortMoves(state, moves);

        // * Schleife über Kinder
        for (Move move : moves) {
            if (context.shouldStop()) {
                throw new SearchStoppedException("Zeitlimit erreicht");
            }

            state.makeMove(move);
            ABResult child = sortedAlphaBetaSearch(state, depth - 1, alpha, beta, context);
            state.unmakeMove();

            int score = child.value;

            if (isMaxing) {
                if (score >= beta ) {
                    return new ABResult(beta,null); //Cutoff
                }
                if (score > alpha) {
                    alpha = score;
                    bestPath = new ArrayList<Move>(child.trace);
                    bestPath.add(move);
                }
            } else {
                if (score <= alpha) {
                    return new ABResult(alpha,null); //Cutoff
                }
                if (score < beta) {
                    beta = score;
                    bestPath = new ArrayList<Move>(child.trace);
                    bestPath.add(move);
                }
            }
        }
        if (isMaxing) {
            return new ABResult(alpha,bestPath);
        } else {
            return new ABResult(beta,bestPath);
        }
    }

    static int evalMove(Move move, Board state){
        state.makeMove(move);
        int result = BoardEvaluator.evaluate(state);
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
