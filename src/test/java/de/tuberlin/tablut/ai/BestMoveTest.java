package de.tuberlin.tablut.ai;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

public class BestMoveTest {

    @Test
    public void testBestMoveAtDepth_allMovesUnmade(){
        String fen = "2b6/9/b1K1b4/9/9/2b6/9/9/9 b 20";
        Board t = Board.fenToBoard(fen);
//        testBoard.printBoard();
        ArrayList<Move> moves = Board.generateLegalMoves(t, t.sideToMove);

        BestMove testBM = new BestMove();
        testBM.bestMoveAtDepth(t,moves,2,new SearchContext());
        Board og = Board.fenToBoard(fen);

        assertEquals(t.movesWithoutCapture,og.movesWithoutCapture);
        assertEquals(t.isStalemateTrackingInitialized(),og.isStalemateTrackingInitialized());
        assertEquals(t.white,og.white);
        assertEquals(t.whiteKing,og.whiteKing);
        assertEquals(t.black,og.black);
        assertEquals(t.sideToMove,og.sideToMove);
        assertEquals(t.boardStateChanges,og.boardStateChanges);
//        assertEquals(t.getPositionCounts(),og.getPositionCounts());
    }

    @Test
    public void testBestMoveAtDepth_1B() {
        String fen = "2b6/9/b1K1b4/9/9/2b6/9/9/9 b 20"; // schwarz kann in 3 halbzügen Sieg erzwingen, indem es figur auf [5,2] nach oben bewegt, ansonsten nicht; Tiefe 3 nötig!
        Board testBoard = Board.fenToBoard(fen);
//        testBoard.printBoard();
        ArrayList<Move> moves = Board.generateLegalMoves(testBoard, testBoard.sideToMove);

        BestMove testBM = new BestMove();
        testBM.bestMoveAtDepth(testBoard,moves,3,new SearchContext());

//        System.out.println(test.getBestMoveDuringIteration()); // warum nicht auf [3,2]?
        assertEquals(100_000,testBM.getBestValueDuringIteration());
    }

    @Test
    public void testBestMoveAtDepth_1W() {
        String fen = "2b6/9/b1K1b4/9/2b6/9/9/9/9 w 20"; // weiß hat in 2 halbzügen verloren
        Board testBoard = Board.fenToBoard(fen);
        testBoard.printBoard();
        ArrayList<Move> moves = Board.generateLegalMoves(testBoard, testBoard.sideToMove);

        BestMove testBM = new BestMove();
        testBM.bestMoveAtDepth(testBoard,moves,2, new SearchContext());

        System.out.println(testBM.getBestMoveDuringIteration()); // warum nicht auf [3,2]?
        assertEquals(100_000,testBM.getBestValueDuringIteration());
    }

    @Test
    public void testBestMoveAtDepth_2b() {
        String fen = "9/9/9/9/9/9/9/9/4K2b1 b 20"; // weiß hat in 2 halbzügen gewonnen
        Board testBoard = Board.fenToBoard(fen);
        testBoard.printBoard();
        ArrayList<Move> moves = Board.generateLegalMoves(testBoard, testBoard.sideToMove);

        BestMove testBM = new BestMove();
        testBM.bestMoveAtDepth(testBoard,moves,2, new SearchContext());

        System.out.println(testBM.getBestMoveDuringIteration());
        System.out.println(testBM.getBestValueDuringIteration());
        assertEquals(-100_000,testBM.getBestValueDuringIteration());
    }

    @Test
    public void testBestMoveAtDepth_2w() {
        String fen = "9/9/9/9/9/9/9/9/4K2b1 w 20"; // weiß hat in 1 halbzügen gewonnen
        Board testBoard = Board.fenToBoard(fen);
        testBoard.printBoard();
        ArrayList<Move> moves = Board.generateLegalMoves(testBoard, testBoard.sideToMove);

        BestMove testBM = new BestMove();
        testBM.bestMoveAtDepth(testBoard,moves,1,new SearchContext());

        System.out.println(testBM.getBestMoveDuringIteration());
        System.out.println(testBM.getBestValueDuringIteration());
        assertEquals(-100_000,testBM.getBestValueDuringIteration());
    }

    @Test
    public void testBestMoveAtDepth_3w() {
        String fen = "9/9/9/9/9/9/9/4K5/2b6 w 20"; // weiß hat in 3 halbzügen gewonnen
        Board testBoard = Board.fenToBoard(fen);
        testBoard.printBoard();
        ArrayList<Move> moves = Board.generateLegalMoves(testBoard, testBoard.sideToMove);

        BestMove testBM = new BestMove();
        testBM.bestMoveAtDepth(testBoard,moves,3,new SearchContext());

        System.out.println(testBM.getBestMoveDuringIteration());
        System.out.println(testBM.getBestValueDuringIteration());
        assertEquals(-100_000,testBM.getBestValueDuringIteration());
    }

    @Test
    public void testBestMoveAtDepth_3b() {
        String fen = "9/9/9/9/9/9/9/4K5/2b6 b 20"; // weiß hat in 4 halbzügen gewonnen
        Board testBoard = Board.fenToBoard(fen);
        testBoard.printBoard();
        ArrayList<Move> moves = Board.generateLegalMoves(testBoard, testBoard.sideToMove);

        BestMove testBM = new BestMove();
        testBM.bestMoveAtDepth(testBoard,moves,4,new SearchContext());

        System.out.println(testBM.getBestMoveDuringIteration());
        System.out.println(testBM.getBestValueDuringIteration());
        assertEquals(-100_000,testBM.getBestValueDuringIteration());
    }

}