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

import static de.tuberlin.tablut.ai.BoardEvaluator.MAX_PLAYER;
import static de.tuberlin.tablut.ai.BoardEvaluator.MIN_PLAYER;

public class Negamax {
    private static final int INF = BoardEvaluator.BETA_INIT;

    public static SearchResult search(Board board, int depth, int alpha, int beta) {
        SearchContext context = new SearchContext();

        try {
            return search(board, depth, alpha, beta, context);
        } catch (SearchStoppedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static SearchResult search(Board board, int depth, int alpha, int beta,
                                      SearchContext context) throws SearchStoppedException {

        context.incrementPositions();
        if (context.shouldStop()) throw new SearchStoppedException("Zeitlimit erreicht");

        int originalAlpha = alpha;

        // Terminal / Blatt
        if (depth == 0 || board.gameIsEnd()) {
            context.incrementLeafs();
            int eval = BoardEvaluator.evaluate(board);
            if (board.sideToMove == MIN_PLAYER) eval = -eval;
//            System.out.println("Eval: " + eval);
            return new SearchResult(eval, new ArrayList<>());
        }


        TranspositionTable transpositionTable = context.getTranspositionTable();
        TranspositionKey key = transpositionTable.key(board);
        if (SearchControlParameters.TRANSPOSITION_TABLE_ACTIVE) {
//            TranspositionKey key = transpositionTable.key(board);
            TranspositionEntry cached = transpositionTable.get(board);
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
        }


        boolean firstMove = true;
        int bestScore = alpha;

        List<Move> bestPath = new ArrayList<>();


        ArrayList<Move> moves = Board.generateLegalMoves(board, board.sideToMove);
        if (SearchControlParameters.SORT_MOVES_ACTIVE) {
            sortMoves(board, moves, context);
        }

        for (Move move : moves) {

            board.makeMove(move);
            SearchResult child;

            if (SearchControlParameters.PVS_ACTIVE) {
                if (firstMove) {
                    child = search(board, depth - 1, -beta, -alpha, context);
                    firstMove = false;
                } else {
                    child = search(board, depth - 1, -alpha - 1, -alpha, context);

                    if (-child.value > alpha && -child.value < beta) {
                        child = search(board, depth - 1, -beta, -alpha, context);
                    }
                }
            } else {
                child = search(board, depth - 1, -beta, -alpha, context);
            }

            board.unmakeMove();

            int score = -child.value;

            if (score > bestScore) {
                bestScore = score;

                bestPath = new ArrayList<>();
                bestPath.add(move);
                bestPath.addAll(child.trace);
            }

            if (bestScore > alpha) alpha = bestScore;
            if (alpha >= beta) {
                // Beta-Cutoff
                if (SearchControlParameters.HISTORY_HEURISTICS_ACTIVE) {
                    context.incrementHistoryHeuristic(move, depth);
                }
                if (SearchControlParameters.TRANSPOSITION_TABLE_ACTIVE) {
                    transpositionTable.getTranspositionTable().put(key, new TranspositionEntry(depth, beta, Bound.LOWER, bestPath));
                }
//                System.out.println("Beta-Cutoff");
                return new SearchResult(bestScore, new ArrayList<>());
            }
        }
        if (SearchControlParameters.TRANSPOSITION_TABLE_ACTIVE) {
            if (bestScore <= originalAlpha) {
                transpositionTable.getTranspositionTable().put(key, new TranspositionEntry(depth, bestScore, Bound.UPPER, bestPath));
            } else if (bestScore >= beta) {
                transpositionTable.getTranspositionTable().put(key, new TranspositionEntry(depth, bestScore, Bound.LOWER, bestPath));
            } else {
                transpositionTable.getTranspositionTable().put(key, new TranspositionEntry(depth, bestScore, Bound.EXACT, bestPath));
            }
        }

//        System.out.println("Exact");
        return new SearchResult(bestScore, bestPath);
    }

    static int evalMove(Move move, Board state, SearchContext context) {
        int result = 0;
        if (SearchControlParameters.SORT_MOVES_BY_VALUE) {
            state.makeMove(move);
            result = BoardEvaluator.evaluate(state);
            state.unmakeMove();
        }

        //HistoryHeuristik liefert Bonus-Score für Zugsortierung
        if (SearchControlParameters.HISTORY_HEURISTICS_ACTIVE) {
            result += context.getHistoryHeuristicScore(move);
        }
//        TranspositionEntry cached = context.getTranspositionTable().get(state);
//        if (cached != null) {
//            if(!cached.getTrace().isEmpty()){
//                result += cached.getTrace().getFirst() == move ? 30_000 : 0;
//            }
//        }
//        System.out.println("Move:"+move+" - Result:" +result);
//        System.out.println("Moves without Capture: " + state.movesWithoutCapture);
        return result;
    }

    static void sortMoves(Board state, ArrayList<Move> moves, SearchContext context) {
        int n = moves.size();
        if (n < 2) return;

        // Höherer Score zuerst (absteigend). Vorzeichen für sideToMove einmalig einfalten,
        // damit der Vergleich ein reiner int-Vergleich ohne HashMap-Lookup/Boxing ist.
        int sign = (MIN_PLAYER == state.sideToMove) ? -1 : 1;

        Move[] moveArr = moves.toArray(new Move[0]);
        int[] keys = new int[n];
        for (int i = 0; i < n; i++) {
            keys[i] = sign * evalMove(moveArr[i], state, context);
        }

        // Insertion-Sort absteigend nach key: stabil, allokationsfrei und schnell für
        // die kleinen Zuglisten (Move Ordering in Schach-Engines macht das genauso).
        for (int i = 1; i < n; i++) {
            Move m = moveArr[i];
            int k = keys[i];
            int j = i - 1;
            while (j >= 0 && keys[j] < k) {
                moveArr[j + 1] = moveArr[j];
                keys[j + 1] = keys[j];
                j--;
            }
            moveArr[j + 1] = m;
            keys[j + 1] = k;
        }

        for (int i = 0; i < n; i++) {
            moves.set(i, moveArr[i]);
        }
    }
}
