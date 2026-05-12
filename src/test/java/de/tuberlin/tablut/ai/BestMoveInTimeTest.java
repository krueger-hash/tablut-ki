package de.tuberlin.tablut.ai;

import org.junit.Test;
import org.w3c.dom.ls.LSOutput;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BestMoveInTimeTest {

    @Test
    public void testBestMoveInTimeReturnsLegalMove(){
        Board board = Board.fenToBoard("4K2b1/1b7/9/9/9/9/9/9/9 b 20");
        // TODO - board somehow mutates original board when stopped
        Move move = new BestMoveInTime(Board.deepCopy(board),1000).getMove();
        board.printBoard();
        System.out.println(move);
        assertNotNull(move);
        assertTrue(containsMove(Board.generateLegalMoves(board, board.sideToMove), move));
    }


    @Test
    public void testBestMoveInTimeDoesNotMutateOriginalBoard(){
        Board board = Board.fenToBoard("4K2b1/1b7/9/9/9/9/9/9/9 b 20");
        int whiteBefore = board.white.bitCount();
        int kingBefore = board.whiteKing.bitCount();
        int blackBefore = board.black.bitCount();
        Player sideBefore = board.sideToMove;
        int movesWithoutCaptureBefore = board.movesWithoutCapture;

        new BestMoveInTime(board,1000).getMove();

        assertFalse(board.gameIsEnd());
        assertTrue(board.boardStateChanges.isEmpty());
        assertTrue(whiteBefore == board.white.bitCount());
        assertTrue(kingBefore == board.whiteKing.bitCount());
        assertTrue(blackBefore == board.black.bitCount());
        assertTrue(sideBefore == board.sideToMove);
        assertTrue(movesWithoutCaptureBefore == board.movesWithoutCapture);
    }

    @Test
    public void testBestMoveInTimeReturnsOnlyLegalBlackMove(){
        Board board = Board.fenToBoard("9/9/9/9/9/9/5RRRR/5RRKR/4RrR1r s 0"); // Same board as submitted by our group
        /*
                    0 1 2 3 4 5 6 7 8
                0 | X . . . . . . . X |
                1 | . . . . . . . . . |
                2 | . . . . . . . . . |
                3 | . . . . . . . . . |
                4 | . . . . T . . . . |
                5 | . . . . . . . . . |
                6 | . . . . . W W W W |
                7 | . . . . . W W K W |
                8 | X . . . W B W . B |
         */
        Move expected = new Move(8, 8, 7, 8, Piece.BLACK);
        assertOnlyLegalMove(board, expected);
        assertSameMove(expected, new BestMoveInTime(board,1000).getMove());
    }

    @Test
    public void testBestMoveInTimeReturnsOnlyLegalWhiteMove(){
        Board board = Board.fenToBoard("9/9/9/4b4/3bKb3/4b4/9/8b/6b1w w 0");
        /*
                0 1 2 3 4 5 6 7 8
            0 | X . . . . . . . X |
            1 | . . . . . . . . . |
            2 | . . . . . . . . . |
            3 | . . . . B . . . . |
            4 | . . . B K B . . . |
            5 | . . . . B . . . . |
            6 | . . . . . . . . . |
            7 | . . . . . . . . B |
            8 | X . . . . . B . W |
         */
        Move expected = new Move(8, 8, 7, 8, Piece.WHITE);
        System.out.println("Test Black Best Move");
        board.printBoard();

        assertOnlyLegalMove(board, expected);
        assertSameMove(expected, new BestMoveInTime(board,1000).getMove());
    }

    @Test
    public void testBestMoveInTimeReturnsKingMoveToEscape(){
        Board board = Board.fenToBoard("9/9/9/9/9/8b/9/8K/9 w");
        /*
                0 1 2 3 4 5 6 7 8
            0 | X . . . . . . . X |
            1 | . . . . . . . . . |
            2 | . . . . . . . . . |
            3 | . . . . . . . . . |
            4 | . . . . T . . . . |
            5 | . . . . . . . . . |
            6 | . . . . . . . . . |
            7 | . . . . . . . . K |
            8 | X . . . . . . . X |
         */
        board.printBoard();
        Move expected = new Move(8,7,8,8, Piece.KING);
        Move bestMove = new BestMoveInTime(board,1000).getMove();
        System.out.println(bestMove);
        assertSameMove(expected, bestMove);
    }

    @Test
    public void testBestMoveInTimeCaptureWhitePiece(){
        Board board = Board.fenToBoard("9/9/5k3/9/8b/8w/2b6/9/9 b");
        board.printBoard();
        Move expected = new Move(2,6,8,6, Piece.BLACK);
        Move bestMove = new BestMoveInTime(board,1000).getMove();
        System.out.println(bestMove);
        assertSameMove(expected, bestMove);
    }

    private static void assertOnlyLegalMove(Board board, Move expected){
        ArrayList<Move> legalMoves = Board.generateLegalMoves(board, board.sideToMove);

        assertEquals(1, legalMoves.size());
        assertSameMove(expected, legalMoves.getFirst());
    }

    private static boolean containsMove(ArrayList<Move> moves, Move expected){
        for (Move move : moves){
            if (sameMove(move, expected)){
                return true;
            }
        }
        return false;
    }

    private static void assertSameMove(Move expected, Move actual){
        assertNotNull(actual);
        assertTrue(sameMove(expected, actual));
    }

    private static boolean sameMove(Move first, Move second){
        return first.from == second.from
                && first.to == second.to
                && first.movedPiece == second.movedPiece;
    }
}
