package de.tuberlin.tablut.ai.SearchAlgorithms;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.BoardEvaluator;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.Player;

import java.util.*;

public class AlphaBetaTransposition extends AlphaBeta{

    private static final Player maxPlayer = BoardEvaluator.MAX_PLAYER;
    private static final Player minPlayer = BoardEvaluator.MIN_PLAYER;
    private static final int ALPHA_INIT = -1_000_000;
    private static final int BETA_INIT = 1_000_000;

    private enum Bound {
        EXACT,
        LOWER,
        UPPER
    }

    private record TranspositionKey(long hash, int movesWithoutCapture) {}

    private static class TranspositionEntry {
        final int depth;
        final int value;
        final Bound bound;
        final ArrayList<Move> trace;

        TranspositionEntry(int depth, int value, Bound bound, List<Move> trace) {
            this.depth = depth;
            this.value = value;
            this.bound = bound;
            this.trace = trace == null ? null : new ArrayList<>(trace);
        }

        ABResult toResult() {
            return new ABResult(value, trace == null ? null : new ArrayList<>(trace));
        }
    }

    private final Map<TranspositionKey, TranspositionEntry> transpositionTable = new HashMap<>();

    /// ////////////////////////////////////////////////////////////////////
    /// Alpha-Beta mit Zugsortierung
    /// ////////////////

    // Implements SearchFunction — use as instance::search for iterative deepening
    public ABResult search(Board state, int depth, SearchContext context) throws SearchStoppedException {
        return sortedAlphaBetaSearch(state, depth, ALPHA_INIT, BETA_INIT, context, transpositionTable);
    }

    private ABResult sortedAlphaBetaSearch(
            Board state,
            int depth,
            int alpha,
            int beta,
            SearchContext context,
            Map<TranspositionKey, TranspositionEntry> transpositionTable
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
            return new ABResult(value,new ArrayList<Move>());
        }

        TranspositionKey key = new TranspositionKey(state.getCurrentHash(), state.movesWithoutCapture);
        TranspositionEntry cached = transpositionTable.get(key);
        // depth >= depth is critical - cache result from depth-2 search is not trustworthy when doing a depth-5 search, didn't look far enough ahead
        if (cached != null && cached.depth >= depth) {
            // previous search fully explored this node within the window and found the true value
            if (cached.bound == Bound.EXACT) {
                return cached.toResult();
            }
            // previous search cut off because a child was too good for the maximizer - true value is at least this
            if (cached.bound == Bound.LOWER) {
                alpha = Math.max(alpha, cached.value);
            }
            // previous search cut off because a child was too bad for the maximizer - true value is at most this
            else if (cached.bound == Bound.UPPER) {
                beta = Math.min(beta, cached.value);
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
            ABResult child = sortedAlphaBetaSearch(state, depth - 1, alpha, beta, context, transpositionTable);
            state.unmakeMove();

            int score = child.value;

            if (isMaxing) {
                // good move - minimizing player above would never allow this position - they already have a better option
                // store a lower bound true value >= beta
                if (score >= beta ) {
                    transpositionTable.put(key, new TranspositionEntry(depth, beta, Bound.LOWER, null));
                    return new ABResult(beta, new ArrayList<>()); //Cutoff
                }
                // update our best-known result wihtin the window
                if (score > alpha) {
                    alpha = score;
                    bestPath = new ArrayList<Move>(child.trace != null ? child.trace : List.of());
                    bestPath.add(move);
                }
            } else {
                // move too bad for maximizing player that he'd never allow it. Store upper bound (true value <= alpha
                if (score <= alpha) {
                    transpositionTable.put(key, new TranspositionEntry(depth, alpha, Bound.UPPER, null));
                    return new ABResult(alpha, new ArrayList<>()); //Cutoff
                }
                // update our best-known result wihtin the window
                if (score < beta) {
                    beta = score;
                    bestPath = new ArrayList<Move>(child.trace != null ? child.trace : List.of());
                    bestPath.add(move);
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
        transpositionTable.put(key, new TranspositionEntry(depth, value, bound, bestPath));
        if (isMaxing) {
            return new ABResult(alpha,bestPath);
        } else {
            return new ABResult(beta,bestPath);
        }
    }
}
