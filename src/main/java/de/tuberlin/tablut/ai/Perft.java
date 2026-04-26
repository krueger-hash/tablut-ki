package de.tuberlin.tablut.ai;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class Perft {

    static int perft(Board state,int depth){
        int leafCount = 0;
        if (depth == 0){
            return 1;
        }
//        if(depth == 0){
//            throw new IllegalArgumentException("keine Moves auf Tiefe 0");
//        }
//        if(depth == 1){
//            // Für Kompatibilität mit state.sideToMove, aber eigentlich sollte das unnötig sein als Parameter für generateLegalMoves
//            List<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
//            return moves.size();
//        }
        else{
            List<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
            for (int i = 0; i < moves.size();i++){
                Move move = moves.get(i);
//            for (Move move : moves){
                Board newState = Board.boardAfterMove(state,move); // Funktion updatet Boardzustand und Zugspieler;
                if(depth ==1) {
                    System.out.println("Depth: " + depth + "| move " + (i + 1) + "/" + moves.size());
                    newState.printBoard();
                }
                leafCount += perft(newState,depth-1);
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

    static void printPerft(Board state, int depth){
        System.out.println(perft(state,depth));
    }

    static void compareWithCase(PerftCase perftCase,String name){
        Board testBoard = Board.fenToBoard(perftCase.fen);
        String output = ""
                + "case: "+ name +"\n"
                + "fen: "+perftCase.fen +"\n"
                + "\t\tperft1\tperft2\tperft3\tperft4\n"
                + String.format("ourResults:\t\t%d\t%d\t%d\t%d\n", perft(testBoard,1),perft(testBoard,2),perft(testBoard,3),perft(testBoard,4))
                + String.format("caseResults:\t%d\t%d\t%d\t%d\n", perftCase.perft1,perftCase.perft2,perftCase.perft3,perftCase.perft4)
                +"\n";
        System.out.println(output);
    }

    static void main() {
        Board state;

        state = Board.fenToBoard("5K3/5b3/9/9/9/9/9/9/9 s");
        state.printBoard();
//        Perft.printPerft(state,1);
        Perft.printPerft(state,2);
//        Perft.printPerft(state,3);
//        Perft.printPerft(state,4);



//        //Test mit Startaufstellung
//        String start = "3bbb3/4b4/4w4/b3w3b/bbwwKwwbb/b3w3b/4w4/4b4/3bbb3 w";
//        state = Board.fenToBoard(start);
//        System.out.println(Board.generateLegalMoves(state, state.sideToMove));
//        System.out.println(Board.generateLegalMoves(state, state.sideToMove).size());
//        System.out.println("Perft");
//        System.out.println(perft(state,2));

//        String fen1 = "6bK1/7b1/9/9/9/9/9/9/9 w";
////        perfTest(fen1,1,1);
//        state = Board.fenToBoard(fen1);
//        System.out.println(Board.generateLegalMoves(state, state.sideToMove));
//
//        String fen2 = "9/9/9/3b5/2bK5/3b5/9/9/9 w";
////        perfTest(fen2,1,1);
//        state = Board.fenToBoard(fen2);
//        System.out.println(Board.generateLegalMoves(state, state.sideToMove));

//        compareWithCase(PerftCase.b1_2,"b1_2");
    }


}
