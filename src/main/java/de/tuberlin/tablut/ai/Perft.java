package de.tuberlin.tablut.ai;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class Perft {

    /////////////////////////////////////////
    //nur Mock-Up!!, da bei mir lokal die Korrekturen nicht nicht drin sind
    List<Move> moveGenerator(Board position){
        return new ArrayList<Move>();
    }
    Board applyMove_outPlace(Board board, Move move){
        return new Board();
    }
    /////////////////////////////////////////

    int perft(Board state,int depth){
        int leafCount = 0;
        if(depth == 0){
            throw new IllegalArgumentException("keine Moves auf Tiefe 0");
        }
        if(depth == 1){
            List<Move> moves = moveGenerator(state);
//            return moves.size();
            leafCount += moves.size();
        }
        else{
            List<Move> moves = moveGenerator(state);
            for (Move move : moves){
                Board newState = applyMove_outPlace(state,move); // bei dieser Funktion müssen wir auch dran denken, dass der Spieler der am Zug ist getauscht wird!
                perft(state,depth-1);
            }
        }
        return leafCount;
    }

    void perfTest(String fen,int depth, int repetitions){

        Board state = Board.fenToBoard(fen);
        long tstart = System.currentTimeMillis();
        int leafs = perft(state,depth);
        long tend = System.currentTimeMillis();
        long duration = tend - tstart;

        String output = "///////////////////////////////////////////\n"
                + "Results:\n"
                + "inital Board: "+ fen +"\n"
                + "depth: " + depth + "\n"
                + "time elapsed: " + duration + " ms\n"
                + "Leafs found: " + leafs;
        System.out.println(output);
    }

    static void main() {;
    }
}
