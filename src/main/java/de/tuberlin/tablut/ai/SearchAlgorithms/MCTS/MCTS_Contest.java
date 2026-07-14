package de.tuberlin.tablut.ai.SearchAlgorithms.MCTS;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.Move;

import java.util.Arrays;

public class MCTS_Contest {


    static void main() {
        int timePerTurn_ms = 10_000;
        boolean biasB = true;
        boolean mastB = true;

        boolean biasW = false;
        boolean mastW = false;

        int rounds = 3;
        int[] results = new int[3];

        long tStart = System.currentTimeMillis();
        for (int i = 0; i < rounds; i++) {
            MCTS_Contest contest = new MCTS_Contest();
            results[i] = contest.contest_loop(biasB,mastB,biasW,mastW,timePerTurn_ms);
            System.out.println("Current Runtime in ms: " + (System.currentTimeMillis()-tStart));
        }
        System.out.println("##############\nResults: "+ Arrays.toString(results)+"  for " +
                "\n BLACK - Prog Bias = "+ biasB+" ,Mast = "+mastB +
                "\n WHITE - Prog Bias = "+ biasW+" ,Mast = "+mastW +
                "\n "
        );


    }

//    static String printresults(int[] res){
//        StringBuilder str = new StringBuilder();
//        str.append("[");
//        for (int i = 0; i < res.length; i++) {
//            str.append(res[i]);
//            str.append(",");
//        }
//        str.append("]");
//    }




    static void setControlParameters(boolean bias, boolean mast){
        MCTS_Control_Parameters.PROGRESSIVE_BIAS_ACTIVE=bias;
        MCTS_Control_Parameters.MAST_ACTIVE=mast;
    }

    static void printGameEnd(Board board,int turn){
        if(board.hasBlackWon()){
            System.out.println("######\nBlack has won!");
        }
        else if(board.hasWhiteWon()){
            System.out.println("######\nWhite has won!");
        }
        else if(board.isStalemate()){
            System.out.println("######\nStalemate!");
        }
        board.printBoard();
        System.out.println("######\nHalbzüge: "+turn);
    }

    int contest_loop(boolean biasB, boolean mastB, boolean biasW, boolean mastW, int timeLimit_ms){

        //Startaufstellung
        Board board = Board.fenToBoard();
        int turn = 0;

        //Initialisiere MCTS-Baum für Schwarz
        setControlParameters(biasB,mastB);
        MCTS_search mcts_BLACK = new MCTS_search(board);
        Move bMove = mcts_BLACK.search(timeLimit_ms);

        //Update Board
        turn++;
        board.makeMove(bMove);
        System.out.println(bMove);
        board.printBoard();
        if(board.gameIsEnd()){
            printGameEnd(board,turn);
        }

        // Initialisiere MCTS-Baum für WEISS
        setControlParameters(biasW,mastW);
        MCTS_search mcts_WHITE = new MCTS_search(board);
        Move wMove = mcts_WHITE.search(timeLimit_ms);

        //Update Board
        turn++;
        board.makeMove(wMove);
        System.out.println(wMove);
        board.printBoard();
        if(board.gameIsEnd()){
            printGameEnd(board,turn);
        }


        //Loop für das Spiel
        while(true){

            //Updaten der Wurzel für Schwarz
            mcts_BLACK.updateRoot(bMove,wMove);

            //MCTS für Schwarz
            setControlParameters(biasB,mastB);
            bMove = mcts_BLACK.search(timeLimit_ms);

            //Update Board
            turn++;
            board.makeMove(bMove);
            System.out.println(bMove);
            board.printBoard();
            if(board.gameIsEnd()){
                printGameEnd(board,turn);
                break;
            }

            //Updaten der Wurzel für WEISS
            mcts_WHITE.updateRoot(wMove,bMove);

            // Initialisiere MCTS-Baum für WEISS
            setControlParameters(biasW,mastW);
            wMove = mcts_WHITE.search(timeLimit_ms);

            //Update Board
            turn++;
            board.makeMove(wMove);
            System.out.println(wMove);
            board.printBoard();
            if(board.gameIsEnd()){
                printGameEnd(board,turn);
                break;
            }
            System.out.println("Full Turn complete. Halfturns: "+turn);
        }
        if(board.hasBlackWon()){
            return 1;
        }
        else if(board.hasWhiteWon()){
            return -1;
        }
        else if(board.isStalemate()){
            return 0;
        }
        else {return 99;} //invalid value
    }
}
