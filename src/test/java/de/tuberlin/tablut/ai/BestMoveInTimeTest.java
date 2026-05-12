package de.tuberlin.tablut.ai;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BestMoveInTimeTest {

    @Test
    public void testBestMoveInTimeReturnsLegalMove(){
        Board board = Board.fenToBoard("4K2b1/1b7/9/9/9/9/9/9/9 b 20");
        Move move = new BestMoveInTime(board,1,1000).getMove();

        assertNotNull(move);
        assertTrue(containsMove(Board.generateLegalMoves(board, board.sideToMove), move));
    }

    @Test
    public void testBestMoveInTimeMatchesBestMoveWithEnoughTimeForBlack(){
        Board board = Board.fenToBoard("4K2b1/1b7/9/9/9/9/9/9/9 b 20");
        Move expected = BestMove.getBestMove(board,1);
        Move actual = new BestMoveInTime(board,1,1000).getMove();

        assertSameMove(expected, actual);
    }

    @Test
    public void testBestMoveInTimeMatchesBestMoveWithEnoughTimeForWhite(){
        Board board = Board.fenToBoard("4K2b1/1b7/9/9/9/9/9/9/9 w 48");
        Move expected = BestMove.getBestMove(board,1);
        Move actual = new BestMoveInTime(board,1,1000).getMove();

        assertSameMove(expected, actual);
    }

    @Test
    public void testBestMoveInTimeDoesNotMutateOriginalBoard(){
        Board board = Board.fenToBoard("4K2b1/1b7/9/9/9/9/9/9/9 b 20");
        int whiteBefore = board.white.bitCount();
        int kingBefore = board.whiteKing.bitCount();
        int blackBefore = board.black.bitCount();
        Player sideBefore = board.sideToMove;
        int movesWithoutCaptureBefore = board.movesWithoutCapture;

        new BestMoveInTime(board,1,1000).getMove();

        assertFalse(board.gameIsEnd());
        assertTrue(board.boardStateChanges.isEmpty());
        assertTrue(whiteBefore == board.white.bitCount());
        assertTrue(kingBefore == board.whiteKing.bitCount());
        assertTrue(blackBefore == board.black.bitCount());
        assertTrue(sideBefore == board.sideToMove);
        assertTrue(movesWithoutCaptureBefore == board.movesWithoutCapture);
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
