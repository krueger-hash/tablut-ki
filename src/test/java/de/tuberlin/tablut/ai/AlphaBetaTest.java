package de.tuberlin.tablut.ai;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AlphaBetaTest {


    @Test
    public void testSortMoves_sortWhite(){
        String fen ="4K2b1/1b7/9/9/9/9/9/9/9 w 48"; //bester Zug für W: König auf obere linke Ecke
        Board test = Board.fenToBoard(fen);
        Move tMove1 = new Move(4,0,0,0,Piece.KING); // besserer Move
        Move tMove2 = new Move(4,0,4,8,Piece.KING);
        Move tMove3 = new Move(4,0,4,7,Piece.KING);
        ArrayList<Move> testList = new ArrayList<>();
        testList.add(tMove2);
        testList.add(tMove2);
        testList.add(tMove3);
        testList.add(tMove3);
        testList.add(tMove1);
//        for (Move move : testList){
//            System.out.print(move +": ");
//            System.out.println(
//                    BoardEvaluator.evaluate(Board.boardAfterMove(test,move))
//            );
//        }
        AlphaBeta.sortMoves(test,testList);
        assertEquals(tMove1,testList.getFirst());
    }

    @Test
    public void testSortMoves_sortBlack(){
        String fen ="4K2b1/1b7/9/9/9/9/9/9/9 b 20"; //bester Zug für W: König auf obere linke Ecke
        Board test = Board.fenToBoard(fen);
        Move tMove1 = new Move(4,0,0,0,Piece.KING); // besserer Move
        Move tMove2 = new Move(4,0,4,8,Piece.KING);
        Move tMove3 = new Move(4,0,4,7,Piece.KING);
        ArrayList<Move> testList = new ArrayList<>();
        testList.add(tMove2);
        testList.add(tMove2);
        testList.add(tMove3);
        testList.add(tMove3);
        testList.add(tMove1);
//        for (Move move : testList){
//            System.out.print(move +": ");
//            System.out.println(
//                    BoardEvaluator.evaluate(Board.boardAfterMove(test,move))
//            );
//        }
        AlphaBeta.sortMoves(test,testList);
        assertEquals(tMove3,testList.getFirst());
    }

    @Test
    public void testSortMoves_MovesWithoutCaptureReset(){
        String fen ="4K2b1/1b7/9/9/9/9/9/9/9 w 48"; //bester Zug für W: König auf obere linke Ecke
        Board test = Board.fenToBoard(fen);
        Move tMove2 = new Move(4,0,4,8,Piece.KING);
        ArrayList<Move> testList = new ArrayList<>();
        testList.add(tMove2);
        testList.add(tMove2);
        testList.add(tMove2);
        testList.add(tMove2);
//        for (Move move : testList){
//            System.out.print(move +": ");
//            System.out.println(
//                    BoardEvaluator.evaluate(Board.boardAfterMove(test,move))
//            );
//        }
        AlphaBeta.sortMoves(test,testList);
        assertEquals(48,test.movesWithoutCapture);
        assertEquals(
                -4620,
                BoardEvaluator.evaluate(Board.boardAfterMove(test,testList.getLast()))
        );
    }
}
