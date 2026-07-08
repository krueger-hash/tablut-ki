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

/**
 * Final Version of Search Algorithm
 * Incorporates transposition tables, PVS, move-ordering (with history heuristic and value sorting)
 */
public class Negamax {
    // Starts a new search by providing initial alpha/beta window and search depth
    public static SearchResult search(Board board, int depth, int alpha, int beta) {
        SearchContext context = new SearchContext();

        try {
            return search(board, depth, alpha, beta, context);
        } catch (SearchStoppedException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Starts a new search by providing initial alpha/beta window and search depth + an existing search context
    public static SearchResult search(Board board, int depth, int alpha, int beta,
                                      SearchContext context) throws SearchStoppedException {

        // Increment counter of visited nodes
        context.incrementPositions();
        // Check if search should be stopped (time limit exceeded)
        if (context.shouldStop()) throw new SearchStoppedException("Zeitlimit erreicht");

        // Save original alpha value for cutoff labeling in tt
        int originalAlpha = alpha;

        // Terminal / Blatt
        if (depth == 0 || board.gameIsEnd()) {
            context.incrementLeafs();
            int eval = BoardEvaluator.evaluate(board);
            if (board.sideToMove == MIN_PLAYER) eval = -eval;
            return new SearchResult(eval, new ArrayList<>());
        }

        // Retrieve transposition table
        TranspositionTable transpositionTable = context.getTranspositionTable();
        // Generate key for current board state
        TranspositionKey key = transpositionTable.key(board);

        // Check if transposition table entry exists - so tt value can be reused
        if (SearchControlParameters.TRANSPOSITION_TABLE_ACTIVE) {
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


        // For pvs - first move is treated differently than the following moves
        boolean firstMove = true;

        int bestScore = alpha;
        // Stored path to best leaf
        List<Move> bestPath = new ArrayList<>();


        // Generate legal moves and order by heuristics (if enabled)
        ArrayList<Move> moves = Board.generateLegalMoves(board, board.sideToMove);
        if (SearchControlParameters.SORT_MOVES_ACTIVE) {
            sortMoves(board, moves, context);
        }

        // Main Search Loop for each node
        for (Move move : moves) {
            // Enter child node
            board.makeMove(move);
            SearchResult child;

            // Result for a child node is trace and value
            // in pvs first move is explore fully - following moves are explored with reduced ab-window
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

            // Exit child node
            board.unmakeMove();

            int score = -child.value;

            if (score > bestScore) {
                bestScore = score;

                // Construct best path of current node
                bestPath = new ArrayList<>();
                bestPath.add(move);
                bestPath.addAll(child.trace);
            }

            if (bestScore > alpha) alpha = bestScore;
            if (alpha >= beta) {
                // Beta-Cutoff: Save in history heuristic and transposition table
                if (SearchControlParameters.HISTORY_HEURISTICS_ACTIVE) {
                    context.incrementHistoryHeuristic(move, depth);
                }
                if (SearchControlParameters.TRANSPOSITION_TABLE_ACTIVE) {
                    transpositionTable.getTranspositionTable().put(key, new TranspositionEntry(depth, beta, Bound.LOWER, bestPath));
                }
                return new SearchResult(bestScore, new ArrayList<>());
            }
        }
        // Save evaluation of the node in transposition table
        if (SearchControlParameters.TRANSPOSITION_TABLE_ACTIVE) {
            if (bestScore <= originalAlpha) {
                transpositionTable.getTranspositionTable().put(key, new TranspositionEntry(depth, bestScore, Bound.UPPER, bestPath));
            } else if (bestScore >= beta) {
                transpositionTable.getTranspositionTable().put(key, new TranspositionEntry(depth, bestScore, Bound.LOWER, bestPath));
            } else {
                transpositionTable.getTranspositionTable().put(key, new TranspositionEntry(depth, bestScore, Bound.EXACT, bestPath));
            }
        }

        return new SearchResult(bestScore, bestPath);
    }

    static int evalMove(Move move, Board state, SearchContext context) {
        int result = 0;
        // Value sorting:
        if (SearchControlParameters.SORT_MOVES_BY_VALUE) {
            state.makeMove(move);
            result += BoardEvaluator.evaluate(state);
            state.unmakeMove();
        }

        // History heuristic
        if (SearchControlParameters.HISTORY_HEURISTICS_ACTIVE) {
            result += context.getHistoryHeuristicScore(move);
        }
        return result;
    }

    static void sortMoves(Board state, ArrayList<Move> moves, SearchContext context) {
        int n = moves.size();
        if (n < 2) return;

        // save sign depending on player
        int sign = (MIN_PLAYER == state.sideToMove) ? -1 : 1;

        Move[] moveArr = moves.toArray(new Move[0]);
        int[] keys = new int[n];
        for (int i = 0; i < n; i++) {
            keys[i] = sign * evalMove(moveArr[i], state, context);
        }

        // inserstion-sort
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
