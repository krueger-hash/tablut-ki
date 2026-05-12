package de.tuberlin.tablut.ai;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BestMoveInTimeTest {

    @Test
    void testBestMoveAtDepth_1() {
//        String fen = "9/1b7/9/9/9/9/9/9/4K2b1 b 48"; //bester Zug für W: König auf obere linke Ecke
        String fen = "2b6/9/b1K1b4/9/2b6/9/9/9/9 b 20";
        Board testBoard = Board.fenToBoard(fen);
        ArrayList<Move> moves = Board.generateLegalMoves(testBoard, testBoard.sideToMove);

        BestMoveInTime test = new BestMoveInTime(Board.deepCopy(testBoard),0);
        test.bestMoveAtDepth(testBoard,moves,2);

        System.out.println(test.getBestMoveDuringIteration());
        System.out.println(test.getBestValueDuringIteration());

    }

    @Test
    void testBestMoveAtDepth_2() {
//        String fen = "9/1b7/9/9/9/9/9/9/4K2b1 b 48";
        String fen = "9/9/9/9/9/9/9/9/4K2b1 b 48";
        Board testBoard = Board.fenToBoard(fen);
        ArrayList<Move> moves = Board.generateLegalMoves(testBoard, testBoard.sideToMove);

        BestMoveInTime test = new BestMoveInTime(Board.deepCopy(testBoard),0);
        test.bestMoveAtDepth(testBoard,moves,2);

        System.out.println(test.getBestMoveDuringIteration());
        System.out.println(test.getBestValueDuringIteration());
    }
}