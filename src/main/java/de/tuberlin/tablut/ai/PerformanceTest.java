package de.tuberlin.tablut.ai;

import java.util.ArrayList;

public class PerformanceTest {
    public static void main(String[] args){
        Board base = new Board();
        System.out.println(perft(base, 1, Player.BLACK));
    }
    // Without piece hit
    public static int perft(Board board, int depth, Player player){
        if(depth == 0) return 1;

        // generate all possible moves
        ArrayList<Move> moves = Board.generateLegalMoves(board, player);

        int nodes = 0;
        // iterate through all moves
        for(Move move : moves){
            // make a deep copy of the board
            Board copy = Board.deepCopy(board);

            // apply the moves
            copy.applyMove(move);

            // call perft recursively
            nodes += perft(copy, depth-1, Board.oppositeSide(player));
        }
        return nodes;

    }
}
