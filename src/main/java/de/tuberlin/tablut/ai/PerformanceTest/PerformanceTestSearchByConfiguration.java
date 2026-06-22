package de.tuberlin.tablut.ai.PerformanceTest;

import de.tuberlin.tablut.ai.BestMoveInTime;
import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.SearchAlgorithms.*;

import java.util.List;
import java.util.Locale;

public class PerformanceTestSearchByConfiguration {
    static List<String> positions = List.of(
            "3rrr3/4r4/4R4/r3R3r/rrRRKRRrr/r3R3r/4R4/4r4/3rrr3 s 0"
//            , "2b6/9/b1K1b4/9/9/2b6/9/9/9 b 20"
//            , "3K2b2/2b6/9/9/9/9/9/9/6b2 b 45"
    );
    private static final int ONE_SECOND_MS = 1_000;
    private static final int TEN_SECOND_MS = 10 * ONE_SECOND_MS;
    private static final int TWO_MINUTES_MS = 2 * 60 * ONE_SECOND_MS;
    private static final int DEPTH_FOUR = 4;

    public static void main(String[] args) {
//        1) AB pur // sortMoves rauskommentiert ..., sonst ValueSort
//        System.out.println("\nAB pur");
//        performanceTestByConfiguration(BestMoveInTime::alphaBetaSearch, false, false, false, false, false);

//        2) Negamax pur
        System.out.println("\n2) Negamax pur");
        performanceTestByConfiguration(false, false, false, false, false);
//        3) Negamax mit PVS
        System.out.println("\n3) Negamax mit PVS");
        performanceTestByConfiguration(false, false, false, false, true);
//        4) Negamax mit TT
        System.out.println("\n4) Negamax mit TT");
        performanceTestByConfiguration(false, false, false, true, false);
//        5) Negamax mit ValueSort
        System.out.println("\n5) Negamax mit ValueSort");
        performanceTestByConfiguration(true, false, true, false, false);
//        6) Negamax mit History-Heuristic
        System.out.println("\n6) Negamax mit History-Heuristic");
        performanceTestByConfiguration(true, true, false, false, false);
//        7) Negamax mit PVS + TT
        System.out.println("\n7) Negamax mit PVS + TT");
        performanceTestByConfiguration(false, false, false, true, true);
//        8) Negamax mit PVS + VS
        System.out.println("\n8) Negamax mit PVS + VS");
        performanceTestByConfiguration(true, false, true, false, true);
//        9) Negamax mit PVS + HH
        System.out.println("\n9) Negamax mit PVS + HH");
        performanceTestByConfiguration(true, true, false, false, true);
//        10) Negamax mit TT + VS
        System.out.println("\n10) Negamax mit TT + VS");
        performanceTestByConfiguration(true, false, true, true, false);
//        11) Negamax mit TT + HH
        System.out.println("\n11) Negamax mit TT + HH");
        performanceTestByConfiguration(true, true, false, true, false);
//        12) Negamax mit VS + HH
        System.out.println("\n12) Negamax mit VS + HH");
        performanceTestByConfiguration(true, true, true, false, false);
//        13) Negamax mit PVS + TT + VS
        System.out.println("\n13) Negamax mit PVS + TT + VS");
        performanceTestByConfiguration(true, false, true, true, true);
//        14) Negamax mit PVS + TT + HH
        System.out.println("\n14) Negamax mit PVS + TT + HH");
        performanceTestByConfiguration(true, true, false, true, true);
//        15) Negamax mit PVS + VS + HH
        System.out.println("\n15) Negamax mit PVS + VS + HH");
        performanceTestByConfiguration(true, true, true, false, true);
//        16) Negamax mit TT + VS + HH
        System.out.println("\n16) Negamax mit TT + VS + HH");
        performanceTestByConfiguration(true, true, true, true, false);
//        17) Negamax mit PVS + TT + VS + HH
        System.out.println("\n17) Negamax mit PVS + TT + VS + HH");
        performanceTestByConfiguration(true, true, true, true, true);


    }


    private static void performanceTestByConfiguration(boolean sortMovesActive, boolean historyHeuristicsActive, boolean sortMovesByValue, boolean transpositionTableActive, boolean pvsActive) {
        performanceTestByConfiguration(BestMoveInTime::negamaxSearch, sortMovesActive, historyHeuristicsActive, sortMovesByValue, transpositionTableActive, pvsActive);
    }
    private static void performanceTestByConfiguration(SearchFunction search, boolean sortMovesActive, boolean historyHeuristicsActive, boolean sortMovesByValue, boolean transpositionTableActive, boolean pvsActive) {
        SearchControlParameters.updateSearchControlParameters(sortMovesActive, historyHeuristicsActive, sortMovesByValue, transpositionTableActive, pvsActive);

        for (String position : positions) {
            Board board = Board.fenToBoard(position);
            // Experiment 1: 1 second limit
//            System.out.println("Experiment 1: 1 second limit");
//            printResult(position, BestMoveInTime.searchInTime(board, ONE_SECOND_MS, search));
//
//            // Experiment 2:, depth 4, 2 minutes limit
//            System.out.println("Experiment 2: depth 4, 2 minutes limit");
//            printResult(position, BestMoveInTime.searchInTime(board, DEPTH_FOUR, TWO_MINUTES_MS, search));

            // Experiment 3: 2 minutes limit
//            System.out.println("Experiment 3: 2 minutes limit");
//            printResult(position, BestMoveInTime.searchInTime(board, TWO_MINUTES_MS, search));

            // Experiment 4: 10 second limit
            System.out.println("Experiment 4: 10 second limit");
            printResult(position, BestMoveInTime.searchInTime(board, TEN_SECOND_MS, search));
        }
    }

    private static void printResult(String algorithm, SearchReport report) {
        System.out.printf(Locale.ROOT,
                "%s: completex=%s, leafs=%d, positions=%d, depth=%d, time=%.3fs, bestMove=%s%n",
                algorithm,
                report.completed(),
                report.leafs(),
                report.positions(),
                report.depth(),
                report.seconds(),
                formatMove(report.bestMove())
        );
    }

    private static String formatMove(Move move) {
        return move == null ? "-" : move.toString();
    }
}
