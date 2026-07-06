package de.tuberlin.tablut.ai.PerformanceTest;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.BoardEvaluator;

import java.util.ArrayList;
import java.util.List;

/**
 * Meassure time in seconds for 10_000 executions of evaluation function
 * Milestone 2
 */
public class PerformanceTestBoardEvaluator {
    public static void main(String[] args) {

        String[] positions = {
                "3rrr3/4r4/4R4/r3R3r/rrRRKRRrr/r3R3r/4R4/4r4/3rrr3 s 0 1",
                "2b6/9/b1K1b4/9/9/2b6/9/9/9 b 20",
                "3K2b2/2b6/9/9/9/9/9/9/6b2 b 45",
        };


        System.out.printf("%-60s %-10s%n","Position","Time in seconds");
        for (String position : positions) {
            Board board = Board.fenToBoard(position);
            System.out.printf("%-60s %-10s%n",position,measureTime(board));
        }

    }

    // Measures time in seconds for 10_000 executions of evaluation function
    private static double measureTime(Board board){
        return measureTime(board,10_000);
    }

    // Measures time in seconds for <iterations> executions of evaluation function
    private static double measureTime(Board board, int iterations){
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            BoardEvaluator.evaluate(board);
        }
        long end = System.nanoTime();
        return (end - start) / 1_000_000_000.0;
    }
}
