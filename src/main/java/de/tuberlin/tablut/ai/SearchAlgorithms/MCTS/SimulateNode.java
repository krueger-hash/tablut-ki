package de.tuberlin.tablut.ai.SearchAlgorithms.MCTS;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.Player;

import java.util.List;

public class SimulateNode {
    public static int randomMoves(Board board) {
        while (true) {
            // Terminal Check
            if (board.hasWhiteWon()) {
                return -1;
            } else if (board.hasBlackWon()) {
                return 1;
            } else if (board.isStalemate()) {
                return 0;
            }

            // generiere zug für derzeitigen spieler
            List<Move> moves = Board.generateLegalMoves(board, board.sideToMove);
            Move move = moves.get((int) (Math.random() * moves.size()));
            board.makeMove(move);
        }
    }

    public static int simulateMAST(Board board, MAST mast) {
        while (true) {
            // Terminal Check
            if (board.hasWhiteWon()) {
                return -1;
            } else if (board.hasBlackWon()) {
                return 1;
            } else if (board.isStalemate()) {
                return 0;
            }

            // generiere zug für derzeitigen spieler
            List<Move> moves = Board.generateLegalMoves(board, board.sideToMove);

//            double randomNum = Math.random();
//            double[] distribution = gibbsDistribution(moves, mast, board.sideToMove);
//
//            Move currentMove = moves.getFirst();
//
//            for (int i = 0; i < moves.size(); i++) {
//                if (randomNum <= distribution[i]) {
//                    currentMove = moves.get(i);
//                    break;
//                }
//            }
//
//            board.makeMove(currentMove);
            Move currentMove = sampleMove(moves, mast, board.sideToMove);
            board.makeMove(currentMove);
        }
    }

    private static final double TAU = 0.5;
    private static Move sampleMove(List<Move> moves, MAST mast, Player player){
        int n = moves.size();
        double sign = (player == Player.WHITE) ? -1 : 1;
        double total = 0.0;
        double[] weights = new double[n];
        // pass 1: exp-weight each move once, accumulate the partition sum
        for(int i = 0; i < n; i++){
            double w = Math.exp(sign * mast.getScore(moves.get(i))/TAU);
            weights[i] = w;
            total += w;
        }
        // pass 2: weighted sample on the unnormalized weights (no cumulative array)
        double r = Math.random() * total;
        for (int i = 0; i < n; i++) {
            r -= weights[i];
            if (r <= 0) {
                return moves.get(i);
            }
        }
        return moves.get(n - 1); // guard against float rounding
    }

    // [50, 20, 10, 5, 1]
    private static double[] gibbsDistribution(List<Move> moves, MAST mast, Player player) {
        double tau = 0.5;
        double[] distribution = new double[moves.size()];
        double[] commulativeSum = new double[moves.size()];
        double zustandsSumme = 0;
        for (Move move : moves) {
            // min player
            if (player == Player.WHITE) {
                zustandsSumme += Math.exp(-mast.getScore(move) / tau);
            } else {
                zustandsSumme += Math.exp(mast.getScore(move) / tau);
            }
        }

        for (int i = 0; i < moves.size(); i++) {
            if (player == Player.WHITE) {
                distribution[i] = Math.exp(-mast.getScore(moves.get(i)) / tau) / zustandsSumme;
            } else {
                distribution[i] = Math.exp(mast.getScore(moves.get(i)) / tau) / zustandsSumme;
            }

            if (i == 0) {
                commulativeSum[i] = distribution[i];
            } else {
                commulativeSum[i] = distribution[i] + commulativeSum[i - 1];
            }
        }
        return commulativeSum;
    }
}
