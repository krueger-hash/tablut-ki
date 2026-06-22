package de.tuberlin.tablut.ai.SearchAlgorithms.MCTS;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.Move;

import java.util.List;

public class SimulateNode {
    public static int randomMoves(Board board) {
        while (true) {
            // Terminal Check
            if (board.hasWhiteWon()) {
                return -1;
            }else if(board.hasBlackWon()){
                return 1;
            }else if(board.isStalemate()){
                return 0;
            }

            // generiere zug für derzeitigen spieler
            List<Move> moves = Board.generateLegalMoves(board, board.sideToMove);
            Move move = moves.get((int) (Math.random() * moves.size()));
            board.makeMove(move);
        }
    }
}
