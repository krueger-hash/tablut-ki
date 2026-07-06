package de.tuberlin.tablut.ai.SearchAlgorithms.MCTS;

import de.tuberlin.tablut.ai.Bitboard90;
import de.tuberlin.tablut.ai.Move;

/**
 * Mast implementation for mcts node simulation
 */
public class MAST {
    // numbor of total fields including separation cells
    private static final int SIZE = Bitboard90.rows * Bitboard90.cols;

    // Instead of matrix use equivalent array of size [size*size] = [size][size]
    private final double[] sumReward = new double[SIZE * SIZE];
    private final int[] count = new int[SIZE * SIZE];

    // Calculate index for size square array
    private static int index(Move move) {
        return move.getFrom() * SIZE + move.getTo();
    }

    // Get average reward for a given move
    public double getScore(Move move) {
        int i = index(move);
        int c = count[i];
        return c == 0 ? 0.0 : sumReward[i] / c;
    }

    // Update mast entries based on a given move and reward
    public void update(Move move, int reward) {
        int i = index(move);
        sumReward[i] += reward;
        count[i] += 1;
    }
}
