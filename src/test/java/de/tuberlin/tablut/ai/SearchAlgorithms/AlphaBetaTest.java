package de.tuberlin.tablut.ai.SearchAlgorithms;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.BoardEvaluator;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.Piece;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AlphaBetaTest {

    @Test
    public void testAlphaBetaDepthZeroReturnsEvaluation(){
        Board board = Board.fenToBoard("3bbb3/4b4/4w4/b3w3b/bbwwKwwbb/b3w3b/4w4/4b4/3bbb3 S 48");

        Assert.assertEquals(
                BoardEvaluator.evaluate(board),
                AlphaBeta.sortedAlphaBetaSearch(board,0,-200000,200000).getValue()
        );
    }

    @Test
    public void testAlphaBetaDepthOneMaximizesForBlack(){
        Board board = Board.fenToBoard("4K2b1/1b7/9/9/9/9/9/9/9 b 20");
        ArrayList<Move> moves = Board.generateLegalMoves(board, board.sideToMove);
        int expected = Integer.MIN_VALUE;

        for (Move move : moves){
            expected = Math.max(
                    expected,
                    BoardEvaluator.evaluate(Board.boardAfterMove(board, move))
            );
        }

        assertEquals(expected, AlphaBeta.sortedAlphaBetaSearch(board,1,-200000,200000).getValue());
    }

    @Test
    public void testAlphaBetaDepthOneMinimizesForWhite(){
        Board board = Board.fenToBoard("4K2b1/1b7/9/9/9/9/9/9/9 w 48");
        ArrayList<Move> moves = Board.generateLegalMoves(board, board.sideToMove);
        int expected = Integer.MAX_VALUE;

        for (Move move : moves){
            expected = Math.min(
                    expected,
                    BoardEvaluator.evaluate(Board.boardAfterMove(board, move))
            );
        }

        assertEquals(expected, AlphaBeta.sortedAlphaBetaSearch(board,1,-200000,200000).getValue());
    }

//    @Test
//    public void testSortedAlphaBetaMatchesUnsortedSearch(){
//        Board board = Board.fenToBoard("4K2b1/1b7/9/9/9/9/9/9/9 b 20");
//
//        assertEquals(
//                AlphaBeta.alphaBetaSearch(board,2,-200000,200000),
//                AlphaBeta.sortedAlphaBetaSearch(board,2,-200000,200000).value
//        );
//    }

    @Test
    public void testSortMoves_sortWhite(){
        String fen ="4K2b1/1b7/9/9/9/9/9/9/9 w 48"; //bester Zug für W: König auf obere linke Ecke
        Board test = Board.fenToBoard(fen);
        Move tMove1 = new Move(4,0,0,0, Piece.KING); // besserer Move
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
