package de.tuberlin.tablut.ai;

import java.util.ArrayList;

public class PerformanceTest {
    public static void main(String[] args) {
//        Board base = new Board();
//        System.out.println(perft(base, 1, Player.BLACK));
        boolean includeOld = true;
        benchmark(includeOld);
    }

    public static int perft(Board board, int depth, Player player) {
        if (depth == 0) return 1;

        // generate all possible moves
        ArrayList<Move> moves = Board.generateLegalMoves(board, player);

        int nodes = 0;
        // iterate through all moves
        for (Move move : moves) {
            board.makeMove(move);
            // call perft recursively

            // Stalemate should be ignored in perft tests
            board.movesWithoutCapture = 0;
            nodes += perft(board, depth - 1, board.sideToMove);
            board.unmakeMove();
        }
        return nodes;
    }

    // Without piece hit
    public static int perftWithDeepCopy(Board board, int depth, Player player) {
        if (depth == 0) return 1;

        // generate all possible moves
        ArrayList<Move> moves = Board.generateLegalMoves(board, player);

        int nodes = 0;
        // iterate through all moves
        for (Move move : moves) {
            Board copy = Board.deepCopy(board);
            copy.makeMove(move);
            // call perft recursively
            nodes += perft(copy, depth - 1, copy.sideToMove);
        }
        return nodes;

    }


    static void benchmark(boolean includeOld) {
        String startPos = "3bbb3/4b4/4w4/b3w3b/bbwwKwwbb/b3w3b/4w4/4b4/3bbb3 S";
        String midGame = "3rrr3/9/4R4/r3R3r/rrR1KRR1r/r3R3r/3R5/4r4/3rrr3 s 4 7"; // Gruppe B1_1
        String endGame = "3r5/9/9/4R4/r4R2r/7K1/9/2r6/5r3 w 3 24";// Gruppe AI_2
        int repetitions = 10 * 1000;

        String output = "///////////////////////////////////////////\n"
                + "//Benchmark - " + repetitions + " Wiederholungen\n";
        output += "\nStartposition: " + startPos + "\n";
        output += String.format("%-10s %-15s%-10s", "Peft(n)", "Duration ms", "Leafs") + "\n";
        for (int depth = 1; depth <= 4; depth++) {
            output += perfTest(startPos, depth, repetitions) + "\n";
            if(includeOld) output += perfTest(startPos, depth, repetitions, true) + " # Old Test\n";
        }
        output += "\nMidgame: " + midGame + "\n";
        output += String.format("%-10s %-15s%-10s", "Peft(n)", "Duration ms", "Leafs") + "\n";
        for (int depth = 1; depth <= 4; depth++) {
            output += perfTest(midGame, depth, repetitions) + "\n";
            if(includeOld) output += perfTest(startPos, depth, repetitions, true) + " # Old Test\n";
        }
        output += "\nEndgame: " + endGame + "\n";
        output += String.format("%-10s %-15s%-10s", "Peft(n)", "Duration ms", "Leafs") + "\n";
        for (int depth = 1; depth <= 4; depth++) {
            output += perfTest(endGame, depth, repetitions) + "\n";
            if(includeOld) output += perfTest(startPos, depth, repetitions, true) + " # Old Test\n";
        }
        output += "\n///////////////////////////////////////////";

        System.out.println(output);

    }

    static String perfTest(String fen, int depth, int repetitions){
        return perfTest(fen, depth, repetitions, false);
    }
    static String perfTest(String fen, int depth, int repetitions, boolean testOld) {

        Board state = Board.fenToBoard(fen);
        long tStart = System.currentTimeMillis();
        int leafs;
        if(!testOld){
            leafs = perft(state, depth, state.sideToMove);
        }else{
            leafs = perftWithDeepCopy(state, depth, state.sideToMove);
        }
        long tEnd = System.currentTimeMillis();
        long duration = tEnd - tStart;

//        String output = String.format("perft(%d) - time: %d ms - leafs: %d", depth, duration, leafs);
        String output = String.format("%-10s %-15s%-10s",depth, duration, leafs);
//                "///////////////////////////////////////////\n"
//                + "Results:\n"
//                + "inital Board: "+ fen +"\n"
//                + "depth: " + depth + "\n"
//                + "Leafs found: " + leafs +"\n"
//                + "repetitions: " + repetitions+"\n"
//                + "time elapsed: " + duration + " ms\n"
//                + "///////////////////////////////////////////\n";
        return output;
    }
}
