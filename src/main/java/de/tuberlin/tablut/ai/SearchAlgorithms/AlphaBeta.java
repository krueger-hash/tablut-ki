package de.tuberlin.tablut.ai.SearchAlgorithms;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.BoardEvaluator;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.Player;

import java.util.*;

/**
 * @deprecated Now contained in negamax search and further developed
 */
@Deprecated
public class AlphaBeta {

    private static final Player maxPlayer = BoardEvaluator.MAX_PLAYER;
    private static final Player minPlayer = BoardEvaluator.MIN_PLAYER;

    /// ////////////////////////////////////////////////////////////////////
    /// Alpha-Beta mit Zugsortierung
    /// ////////////////
    public static SearchResult sortedAlphaBetaSearch(Board state, int depth, int alpha, int beta) {
        SearchContext context = new SearchContext();
        try {
            return sortedAlphaBetaSearch(state, depth, alpha, beta, context);
        } catch (SearchStoppedException sse) {
            sse.printStackTrace();
            return null;
        }
    }

    // bei Erreichen des Zeitlimits wird unmakeMove nicht! aufgerufen. Wir könnten das noch sauber beheben mit catch-Blöcken, aber es ist wohl besser mit Deep-Copy zu begin von BestMove
    public static SearchResult sortedAlphaBetaSearch(Board state, int depth, int alpha, int beta, SearchContext context) throws SearchStoppedException {
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
            return new SearchResult(value,new ArrayList<Move>());
        }

        //*Erzeuge alle möglichen Züge
        ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
        //Zugsortierung
//        sortMoves(state, moves);

        // * Schleife über Kinder
        for (Move move : moves) {
            if (context.shouldStop()) {
                throw new SearchStoppedException("Zeitlimit erreicht");
            }

            state.makeMove(move);
            SearchResult child = sortedAlphaBetaSearch(state, depth - 1, alpha, beta, context);
            state.unmakeMove();

            int score = child.value;

            if (isMaxing) {
                if (score >= beta ) {
                    return new SearchResult(beta,null); //Cutoff
                }
                if (score > alpha) {
                    alpha = score;
                    bestPath = new ArrayList<Move>(child.trace);
                    bestPath.addFirst(move);
                }
            } else {
                if (score <= alpha) {
                    return new SearchResult(alpha,null); //Cutoff
                }
                if (score < beta) {
                    beta = score;
                    bestPath = new ArrayList<Move>(child.trace);
                    bestPath.addFirst(move);
                }
            }
        }
        if (isMaxing) {
            return new SearchResult(alpha,bestPath);
        } else {
            return new SearchResult(beta,bestPath);
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
