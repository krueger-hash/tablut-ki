package de.tuberlin.tablut.ai.PerformanceTest;

import de.tuberlin.tablut.ai.ABResult;
import de.tuberlin.tablut.ai.AlphaBeta;
import de.tuberlin.tablut.ai.BestMoveInTime;
import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.Minimax;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.SearchContext;
import de.tuberlin.tablut.ai.SearchReport;
import de.tuberlin.tablut.ai.SearchStoppedException;

import java.util.List;
import java.util.Locale;

public class PerformanceTestSearch {
    private static final int ALPHA_INIT = -1_000_000;
    private static final int BETA_INIT = 1_000_000;
    private static final int ONE_SECOND_MS = 1_000;
    private static final int TWO_MINUTES_MS = 120_000;
    private static final int DEPTH_FOUR = 4;
    private static final int PRACTICAL_MAX_DEPTH = 1_000;

    public static void main(String[] args) {
        List<String> defaultPositions = List.of(
                "3rrr3/4r4/4R4/r3R3r/rrRRKRRrr/r3R3r/4R4/4r4/3rrr3 s 0 1",
                "2b6/9/b1K1b4/9/9/2b6/9/9/9 b 20",
                "3K2b2/2b6/9/9/9/9/9/9/6b2 b 45"
        );

        for (String fen : defaultPositions) {
            printExperiment1(fen);
            printExperiment2(fen);
            printExperiment3(fen);
        }
    }

    // Experiment 1:
    // Minimax + AlphaBeta, limit 1s, return leafs, max depth and best move
    static void printExperiment1(String fen) {
        Board board = Board.fenToBoard(fen);
        System.out.println();
        System.out.println("Experiment 1 - 1s search");
        System.out.println("Position: " + fen);
        printTimedResult("Minimax", BestMoveInTime.searchInTime(board, ONE_SECOND_MS, PRACTICAL_MAX_DEPTH, Minimax::minimaxSearch));
        printTimedResult("Alpha-Beta", BestMoveInTime.searchInTime(board, ONE_SECOND_MS, PRACTICAL_MAX_DEPTH, PerformanceTestSearch::alphaBetaSearch));
    }

    // Experiment 2:
    // Minimax + AlphaBeta, limit depth 4, limit 2min, return leafs, time and best move
    static void printExperiment2(String fen) {
        Board board = Board.fenToBoard(fen);
        System.out.println();
        System.out.println("Experiment 2 - depth 4, max 2min");
        System.out.println("Position: " + fen);
        printDepthResult("Minimax", BestMoveInTime.searchAtDepth(board, DEPTH_FOUR, TWO_MINUTES_MS, Minimax::minimaxSearch));
        printDepthResult("Alpha-Beta", BestMoveInTime.searchAtDepth(board, DEPTH_FOUR, TWO_MINUTES_MS, PerformanceTestSearch::alphaBetaSearch));
    }

    // Experiment 3:
    // Minimax + AlphaBeta, max limit 2min, return leafs, max depth, time
    static void printExperiment3(String fen) {
        Board board = Board.fenToBoard(fen);
        System.out.println();
        System.out.println("Experiment 3 - max 2min search");
        System.out.println("Position: " + fen);
        printTimedResult("Minimax", BestMoveInTime.searchInTime(board, TWO_MINUTES_MS, PRACTICAL_MAX_DEPTH, Minimax::minimaxSearch));
        printTimedResult("Alpha-Beta", BestMoveInTime.searchInTime(board, TWO_MINUTES_MS, PRACTICAL_MAX_DEPTH, PerformanceTestSearch::alphaBetaSearch));
    }

    private static void printTimedResult(String algorithm, SearchReport report) {
        System.out.printf(Locale.ROOT,
                "%s: leafs=%d, positions=%d, depth=%d, time=%.3fs, bestMove=%s%n",
                algorithm,
                report.leafs(),
                report.positions(),
                report.depth(),
                report.seconds(),
                formatMove(report.bestMove())
        );
    }

    private static void printDepthResult(String algorithm, SearchReport report) {
        System.out.printf(Locale.ROOT,
                "%s: completed=%s, leafs=%d, positions=%d, time=%.3fs, bestMove=%s%n",
                algorithm,
                report.completed(),
                report.leafs(),
                report.positions(),
                report.seconds(),
                formatMove(report.bestMove())
        );
    }

    private static String formatMove(Move move) {
        return move == null ? "-" : move.toString();
    }

    private static ABResult alphaBetaSearch(Board board, int depth, SearchContext context) throws SearchStoppedException {
        return AlphaBeta.sortedAlphaBetaSearch(board, depth, ALPHA_INIT, BETA_INIT, context);
    }
}
