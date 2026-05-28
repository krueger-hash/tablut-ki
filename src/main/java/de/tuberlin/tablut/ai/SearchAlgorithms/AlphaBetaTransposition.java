package de.tuberlin.tablut.ai.SearchAlgorithms;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.BoardEvaluator;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.Player;

import java.util.*;

public class AlphaBetaTransposition {

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
        if (cached != null && cached.depth >= depth) {
            if (cached.bound == Bound.EXACT) {
                return cached.toResult();
            }
            if (cached.bound == Bound.LOWER) {
                alpha = Math.max(alpha, cached.value);
            } else if (cached.bound == Bound.UPPER) {
                beta = Math.min(beta, cached.value);
            }
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
                if (score >= beta ) {
                    transpositionTable.put(key, new TranspositionEntry(depth, beta, Bound.LOWER, null));
                    return new ABResult(beta,null); //Cutoff
                }
                if (score > alpha) {
                    alpha = score;
                    bestPath = new ArrayList<Move>(child.trace);
                    bestPath.add(move);
                }
            } else {
                if (score <= alpha) {
                    transpositionTable.put(key, new TranspositionEntry(depth, alpha, Bound.UPPER, null));
                    return new ABResult(alpha,null); //Cutoff
                }
                if (score < beta) {
                    beta = score;
                    bestPath = new ArrayList<Move>(child.trace);
                    bestPath.add(move);
                }
            }
        }
        int value = isMaxing ? alpha : beta;
        Bound bound;
        if (value <= originalAlpha) {
            bound = Bound.UPPER;
        } else if (value >= originalBeta) {
            bound = Bound.LOWER;
        } else {
            bound = Bound.EXACT;
        }
        transpositionTable.put(key, new TranspositionEntry(depth, value, bound, bestPath));
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
