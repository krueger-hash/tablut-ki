package de.tuberlin.tablut.ai.SearchAlgorithms.MCTS;

import de.tuberlin.tablut.ai.Bitboard90;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.Piece;

/**
 * Mast implementation for mcts node simulation
 */
public class MAST {
    // numbor of total fields including separation cells
    private static final int SIZE = Bitboard90.rows * Bitboard90.cols;

    // Instead of matrix use equivalent array of size [size*size] = [size][size]. One Array for each Type of Piece to differentiate moves
    private final double[] sumReward_B = new double[SIZE * SIZE];
    private final int[] count_B = new int[SIZE * SIZE];
    private final double[] sumReward_W = new double[SIZE * SIZE];
    private final int[] count_W = new int[SIZE * SIZE];
    private final double[] sumReward_K = new double[SIZE * SIZE];
    private final int[] count_K = new int[SIZE * SIZE];

    // Calculate index for size square array
    private static int index(Move move) {
        return move.getFrom() * SIZE + move.getTo();
    }

    // Get average reward for a given move
    public double getScore(Move move) {
        int i = index(move);
        int[] count;
        double[] sumReward;
        if(move.getMovedPiece() == Piece.BLACK){count = count_B; sumReward = sumReward_B;}
        else if (move.getMovedPiece() == Piece.WHITE){count = count_W; sumReward = sumReward_W;}
        else {count = count_K; sumReward = sumReward_K;}
        int c = count[i];
        return c == 0 ? 0.0 : sumReward[i] / c;
    }

    // Update mast entries based on a given move and reward
    public void update(Move move, int reward) {
        int i = index(move);
        int[] count;
        double[] sumReward;
        if(move.getMovedPiece() == Piece.BLACK){count = count_B; sumReward = sumReward_B;}
        else if (move.getMovedPiece() == Piece.WHITE){count = count_W; sumReward = sumReward_W;}
        else {count = count_K; sumReward = sumReward_K;}
        sumReward[i] += reward;
        count[i] += 1;
    }
}
