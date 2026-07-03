package de.tuberlin.tablut.ai.SearchAlgorithms.MCTS;

import de.tuberlin.tablut.ai.Bitboard90;
import de.tuberlin.tablut.ai.Move;

public class MAST {
    // Board bit indices run from 0 to rows*cols-1 (0..89), so a move (from, to) maps to a
    // unique slot from*SIZE + to. Flat arrays give O(1) lookup/update with no hashing and no
    // object allocation per rollout ply - the HashMap<Move, MAST_ENTRY> version was the main
    // per-simulation cost that starved the MAST variants of iterations.
    private static final int SIZE = Bitboard90.rows * Bitboard90.cols;

    private final double[] sumReward = new double[SIZE * SIZE];
    private final int[] count = new int[SIZE * SIZE];

    private static int index(Move move) {
        return move.getFrom() * SIZE + move.getTo();
    }

    public double getScore(Move move) {
        int i = index(move);
        int c = count[i];
        return c == 0 ? 0.0 : sumReward[i] / c;
    }

    public void update(Move move, int reward) {
        int i = index(move);
        sumReward[i] += reward;
        count[i] += 1;
    }
}
