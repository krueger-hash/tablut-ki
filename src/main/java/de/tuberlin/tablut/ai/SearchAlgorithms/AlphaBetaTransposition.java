package de.tuberlin.tablut.ai.SearchAlgorithms;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.BoardEvaluator;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.Player;
import de.tuberlin.tablut.ai.SearchAlgorithms.TranspositionTable.Bound;
import de.tuberlin.tablut.ai.SearchAlgorithms.TranspositionTable.TranspositionEntry;
import de.tuberlin.tablut.ai.SearchAlgorithms.TranspositionTable.TranspositionKey;
import de.tuberlin.tablut.ai.SearchAlgorithms.TranspositionTable.TranspositionTable;

import java.util.*;

public class AlphaBetaTransposition extends AlphaBeta{

    private static final Player maxPlayer = BoardEvaluator.MAX_PLAYER;
    private static final Player minPlayer = BoardEvaluator.MIN_PLAYER;
    private static final int ALPHA_INIT = -1_000_000;
    private static final int BETA_INIT = 1_000_000;

    /// ////////////////////////////////////////////////////////////////////
    /// Alpha-Beta mit Zugsortierung
    /// ////////////////

    // Implements SearchFunction — use as instance::search for iterative deepening
    public static SearchResult search(Board state, int depth, SearchContext context) throws SearchStoppedException {
        return sortedAlphaBetaSearch(state, depth, ALPHA_INIT, BETA_INIT, context);
    }

    public static SearchResult sortedAlphaBetaSearch(
            Board state,
            int depth,
            int alpha,
            int beta,
            SearchContext context
    ) throws SearchStoppedException {
        context.incrementPositions();
        if (context.shouldStop()) {throw new SearchStoppedException("Zeitlimit erreicht");}

        //*Initialisierung lokaler Variablen für Knoten
        boolean isMaxing;
        ArrayList<Move> bestPath = new ArrayList<Move>();
        int originalAlpha = alpha;
        int originalBeta = beta;
        if (state.sideToMove == BoardEvaluator.MAX_PLAYER){isMaxing = true;}
        else{isMaxing = false;}

        //*Rekursionsende in Terminal- oder Blattknoten
        if (depth == 0 || state.gameIsEnd()) {
            context.incrementLeafs();
            int value = BoardEvaluator.evaluate(state);
            return new SearchResult(value,new ArrayList<Move>());
        }

        TranspositionTable transpositionTable = context.getTranspositionTable();

        TranspositionKey key = transpositionTable.key(state);
        TranspositionEntry cached = transpositionTable.get(state);
        // depth >= depth is critical - cache result from depth-2 search is not trustworthy when doing a depth-5 search, didn't look far enough ahead
        if (cached != null && cached.getDepth() >= depth) {
            // previous search fully explored this node within the window and found the true value
            if (cached.getBound() == Bound.EXACT) {
                return cached.toResult();
            }
            // previous search cut off because a child was too good for the maximizer - true value is at least this
            else if (cached.getBound() == Bound.LOWER) {
                alpha = Math.max(alpha, cached.getValue());
            }
            // previous search cut off because a child was too bad for the maximizer - true value is at most this
            else if (cached.getBound() == Bound.UPPER) {
                beta = Math.min(beta, cached.getValue());
            }
            // if alpha >= beta, window has closed - node is irrelevant to the parent
            if (alpha >= beta) {
                return cached.toResult();
            }
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
            SearchResult child = sortedAlphaBetaSearch(state, depth - 1, alpha, beta, context);
            state.unmakeMove();

            int score = child.value;

            if (isMaxing) {
                // good move - minimizing player above would never allow this position - they already have a better option
                // store a lower bound true value >= beta
                if (score >= beta ) {
                    transpositionTable.getTranspositionTable().put(key, new TranspositionEntry(depth, beta, Bound.LOWER, null));
                    return new SearchResult(beta, new ArrayList<>()); //Cutoff
                }
                // update our best-known result wihtin the window
                if (score > alpha) {
                    alpha = score;
                    bestPath = new ArrayList<Move>(child.trace != null ? child.trace : List.of());
                    bestPath.addFirst(move);
                }
            } else {
                // move too bad for maximizing player that he'd never allow it. Store upper bound (true value <= alpha
                if (score <= alpha) {
                    transpositionTable.getTranspositionTable().put(key, new TranspositionEntry(depth, alpha, Bound.UPPER, null));
                    return new SearchResult(alpha, new ArrayList<>()); //Cutoff
                }
                // update our best-known result wihtin the window
                if (score < beta) {
                    beta = score;
                    bestPath = new ArrayList<Move>(child.trace != null ? child.trace : List.of());
                    bestPath.addFirst(move);
                }
            }
        }
        int value = isMaxing ? alpha : beta;
        Bound bound;
        // alpha never improved - every child was worse than what maximizer already had elsewhere, don't know true value, only <= originalAlpha -> Upper bound
        if (value <= originalAlpha) {
            bound = Bound.UPPER;
        }
        // only possible for a min node where beta never moved. True value >= originalBeta -> Lower bound
        else if (value >= originalBeta) {
            bound = Bound.LOWER;
        }
        // Value was found within the window, it's the true minimax value -> Exact
        else {
            bound = Bound.EXACT;
        }
        transpositionTable.getTranspositionTable().put(key, new TranspositionEntry(depth, value, bound, bestPath));
        if (isMaxing) {
            return new SearchResult(alpha,bestPath);
        } else {
            return new SearchResult(beta,bestPath);
        }
    }
}
