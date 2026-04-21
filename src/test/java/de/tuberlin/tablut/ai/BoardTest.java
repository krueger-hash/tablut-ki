package de.tuberlin.tablut.ai;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class BoardTest {

    private Board createBoard(long wLow, long wHigh,
                              long kLow, long kHigh,
                              long bLow, long bHigh,
                              long bloLow, long bloHigh) {

        Bitboard90 white = new Bitboard90(wLow, wHigh);
        Bitboard90 king = new Bitboard90(kLow, kHigh);
        Bitboard90 black = new Bitboard90(bLow, bHigh);
        Bitboard90 blocked = new Bitboard90(bloLow, bloHigh);
        Bitboard90 throne = new Bitboard90(1L << 44, 0);

        return new Board(white, king, black, blocked, throne);
    }


    @Test
    public void testGetPieceAt() {
        // Setup:
        // Weiß auf 10
        // Schwarz auf 65
        // König auf 30
        // Blockiertes Feld auf 40
        Board board = createBoard(
                1L << 10, 0,      // white
                1L << 30, 0,      // king
                0, 1L << 1,      // black
                0, 1L << 16            // blocked
        );

        // Tests:
        assertEquals(Piece.WHITE, board.getPieceAt(10), "White stone expected at pos 10");
        assertEquals(Piece.BLACK, board.getPieceAt(65), "Black stone expected at pos 20");
        assertEquals(Piece.KING, board.getPieceAt(30), "King expected at pos 30");
        assertEquals(Piece.BLOCKED, board.getPieceAt(80), "Blocked field expected at pos 40");
        assertEquals(Piece.EMPTY, board.getPieceAt(0), "Empty field expected at pos 0");
        assertEquals(Piece.THRONE, board.getPieceAt(44), "Throne field expected at pos 0");
    }

    @Test
    public void testApplyMoveWhitePiece() {
        // White at pos 10
        Board board = createBoard(
                1L << 10, 0,   // white
                0, 0,          // king
                0, 0,           // black
                0, 1L << 16            // blocked
        );

        Move move = new Move(10, 20, Piece.WHITE);
        board.applyMove(move);

        // Prüfen: alter Platz leer
        assertEquals(Piece.EMPTY, board.getPieceAt(10), "Old position should be empty");

        // Prüfen: neuer Platz weiß
        assertEquals(Piece.WHITE, board.getPieceAt(20), "White piece should be at new position");

        // Prüfen: keine Duplikation
        assertNotEquals(Piece.WHITE, board.getPieceAt(0), "No other white pieces should appear");
    }

    @Test
    public void testApplyMoveKing() {
        // King at pos 44 (Thron)
        Board board = createBoard(
                0, 0,           // white
                1L << 44, 0,    // king
                0, 0,            // black
                0, 1L << 16     //blocked
        );

        Move move = new Move(44, 54, Piece.KING);
        board.applyMove(move);

        // Prüfen: alter Platz leer
        assertEquals(Piece.THRONE, board.getPieceAt(44), "Throne should be empty after king moves");

        // Prüfen: neuer Platz König
        assertEquals(Piece.KING, board.getPieceAt(54), "King should be at new position");

        // Prüfen: keine anderen Figuren verändert
        assertEquals(Piece.EMPTY, board.getPieceAt(10), "No other pieces should be affected");
    }

    @Test
    public void testApplyMoveBlackPiece() {
        Board board = createBoard(
                0, 0,
                0, 0,
                1L << 30, 0,    // black at 30
                0, 1L << 16     //blocked
        );

        Move move = new Move(30, 40, Piece.BLACK);
        board.applyMove(move);

        assertEquals(Piece.EMPTY, board.getPieceAt(30));
        assertEquals(Piece.BLACK, board.getPieceAt(40));
    }

    //normales Einschließen links/rechts
    @Test
    public void testCheckHitNormalSandwichHorizontal() {
        // White at 10 and 12, Black at 11
        Board board = createBoard(
                (1L << 10) | (1L << 13), 0,   // white
                0, 0,                        // king
                1L << 11, 0,                  // black
                0, 1L << 16
        );

        Move move = new Move(13, 12, Piece.WHITE);
        ArrayList<Piece> hits = board.checkHit(move);

        assertTrue("Black piece should be captured", hits.contains(Piece.BLACK));
        assertEquals(Piece.EMPTY, board.getPieceAt(11), "Captured piece must be removed");
    }



    @Test
    public void testPrintBoardShowsPiecesAndThrone() {
        Board board = createBoard(
                1L << 10, 0,      // white at 10
                1L << 44, 0,      // king at throne
                1L << 30, 0,      // black at 30
                (1L << 0) | (1L << 8), (1L << 16) | (1L << 24) // blocked corners
        );

        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        try {
            board.printBoard();
        } finally {
            System.setOut(originalOut);
        }

        String output = outputStream.toString();
        assertTrue(output.contains("0 | X . . . . . . . X |"));
        assertTrue(output.contains("1 | W . . . . . . . . |"));
        assertTrue(output.contains("3 | B . . . . . . . . |"));
        assertTrue(output.contains("4 | . . . . K . . . . |"));
    }

    @Test
    public void testGenerateLegalMovesKingFromCenter() {
        Board board = createBoard(
                0, 0,                     // white
                1L << 44, 0,              // king at center
                0, 0,                     // black
                (1L << 0) | (1L << 8), (1L << 16) | (1L << 24) // blocked corners
        );

        Move[] moves = board.generateLegalMoves(board.whiteKing, Piece.KING);
        assertEquals(16, moves.length, "King at center should have 16 rook-like moves");
        assertTrue(Arrays.stream(moves).allMatch(m -> m.movedPiece == Piece.KING));
        assertTrue(Arrays.stream(moves).anyMatch(m -> m.from == 44 && m.to == 4));   // north edge
        assertTrue(Arrays.stream(moves).anyMatch(m -> m.from == 44 && m.to == 84));  // south edge
        assertTrue(Arrays.stream(moves).anyMatch(m -> m.from == 44 && m.to == 40));  // west edge
        assertTrue(Arrays.stream(moves).anyMatch(m -> m.from == 44 && m.to == 48));  // east edge
    }

    @Test
    public void testIsPlayableSquareViaReflection() throws Exception {
        Board board = new Board();
        Method isPlayableSquare = Board.class.getDeclaredMethod("isPlayableSquare", int.class);
        isPlayableSquare.setAccessible(true);

        assertTrue((Boolean) isPlayableSquare.invoke(board, 0));
        assertTrue((Boolean) isPlayableSquare.invoke(board, 88));
        assertFalse((Boolean) isPlayableSquare.invoke(board, 9));   // separator column
        assertFalse((Boolean) isPlayableSquare.invoke(board, 89));  // separator column
        assertFalse((Boolean) isPlayableSquare.invoke(board, -1));  // below range
        assertFalse((Boolean) isPlayableSquare.invoke(board, 90));  // above range
    }

    @Test
    public void testIsStalemateWhenSideToMoveHasNoMoves() {
        Board board = createBoard(
                0, 0,                     // white
                1L << 44, 0,              // king present, so black has not won
                0, 0,                     // black side has no pieces, no legal move
                (1L << 0) | (1L << 8), (1L << 16) | (1L << 24)
        );

        assertTrue("If side to move has no legal move, stalemate should be true", board.isStalemate());
    }

    @Test
    public void testIsStalemateFalseInInitialPosition() {
        Board board = new Board();
        assertFalse(board.isStalemate(), "Initial position should not be stalemate");
    }

}
