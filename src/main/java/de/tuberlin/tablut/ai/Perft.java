package de.tuberlin.tablut.ai;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class Perft {

    static int perft(Board state,int depth){
        int leafCount = 0;
        if(depth == 0){
            throw new IllegalArgumentException("keine Moves auf Tiefe 0");
        }
        if(depth == 1){
            // Für Kompatibilität mit state.sideToMove, aber eigentlich ist das unnötig als Parameter für generateLegalMoves
            List<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
            leafCount += moves.size();
        }
        else{
            List<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
            for (Move move : moves){
                Board newState = Board.boardAfterMove(state,move); // Funktion updatet Boardzustand und Zugspieler;
                perft(state,depth-1);
            }
        }
        return leafCount;
    }

    static void perfTest(String fen,int depth, int repetitions){

        Board state = Board.fenToBoard(fen);
        long tstart = System.currentTimeMillis();
        int leafs = perft(state,depth);
        long tend = System.currentTimeMillis();
        long duration = tend - tstart;

        String output = "///////////////////////////////////////////\n"
                + "Results:\n"
                + "inital Board: "+ fen +"\n"
                + "depth: " + depth + "\n"
                + "Leafs found: " + leafs +"\n"
                + "repetitions: " + repetitions+"\n"
                + "time elapsed: " + duration + " ms\n"
                + "///////////////////////////////////////////\n";
        System.out.println(output);
    }

    static void main() {
        String fen1 = "6bK1/7b1/9/9/9/9/9/9/9 w";
        perfTest(fen1,1,1);

        Board state = Board.fenToBoard(fen1);
        System.out.println(state.sideToMove);
        System.out.println(Board.generateLegalMoves(state, state.sideToMove));
    }
}
