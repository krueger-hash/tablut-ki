package de.tuberlin.tablut.ai.ZobristHasher;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.Hit;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.Piece;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class TablutZobristHasherTest {

    @Test
    public void hashIsDeterministicForSamePosition() {
        Board board = Board.fenToBoard("3bbb3/4b4/4w4/b3w3b/bbwwKwwbb/b3w3b/4w4/4b4/3bbb3 S 48");
        TablutZobristHasher hasher = new TablutZobristHasher(1234L);

        assertEquals(hasher.hashPosition(board), hasher.hashPosition(Board.deepCopy(board)));
    }

    @Test
    public void hashDifferentiatesSideToMove() {
        TablutZobristHasher hasher = new TablutZobristHasher(1234L);
        Board blackToMove = Board.fenToBoard("9/9/9/9/w8/9/9/9/9 b");
        Board whiteToMove = Board.fenToBoard("9/9/9/9/w8/9/9/9/9 w");

        assertNotEquals(hasher.hashPosition(blackToMove), hasher.hashPosition(whiteToMove));
    }

    @Test
    public void hashHandlesHighestEncodedPlayableSquare() {
        TablutZobristHasher hasher = new TablutZobristHasher(1234L);
        Board board = Board.fenToBoard("9/9/9/9/9/9/9/9/8K w");

        assertEquals(hasher.hashPosition(board), hasher.hashPosition(Board.deepCopy(board)));
    }

    @Test
    public void hashIsRestoredAfterMakeAndUnmakeMove() {
        TablutZobristHasher hasher = new TablutZobristHasher(1234L);
        Board board = Board.fenToBoard("9/9/9/9/w8/9/9/9/9 w");
        long before = hasher.hashPosition(board);

        board.makeMove(new Move(40, 41, Piece.WHITE));
        assertNotEquals(before, hasher.hashPosition(board));

        board.unmakeMove();
        assertEquals(before, hasher.hashPosition(board));
    }

    @Test
    public void updateHashPositionMatchesFullHashAfterQuietMove() {
        TablutZobristHasher hasher = new TablutZobristHasher(1234L);
        Board board = Board.fenToBoard("9/9/9/9/w8/9/9/9/9 w");
        Move move = new Move(40, 41, Piece.WHITE);
        List<Hit> hits = board.checkHit(move);

        long updatedHash = hasher.updateHashPosition(hasher.hashPosition(board), move, hits);
        board.makeMove(move);

        assertEquals(hasher.hashPosition(board), updatedHash);
    }

    @Test
    public void updateHashPositionMatchesFullHashAfterCapture() {
        TablutZobristHasher hasher = new TablutZobristHasher(1234L);
        Board board = Board.fenToBoard("9/9/9/9/w8/9/b8/w8/9 w");
        Move move = new Move(40, 50, Piece.WHITE);
        List<Hit> hits = board.checkHit(move);

        long updatedHash = hasher.updateHashPosition(hasher.hashPosition(board), move, hits);
        board.makeMove(move);

        assertEquals(hasher.hashPosition(board), updatedHash);
    }
}
