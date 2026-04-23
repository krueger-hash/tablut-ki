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

        Move move = new Move(22, 12, Piece.WHITE);
        ArrayList<Piece> hits = board.checkHit(move);

        assertTrue("Black piece should be captured", hits.contains(Piece.BLACK));
        assertEquals(Piece.EMPTY, board.getPieceAt(11), "Captured piece must be removed");
    }

    @Test
    public void testKingCanCaptureBlack() {
        // King at 10, White at 12, Black at 11
        Board board = createBoard(
                1L << 13, 0,          // white
                1L << 10, 0,          // king
                1L << 11, 0,           // black
                0, 1L << 16
        );

        Move move = new Move(22, 12, Piece.WHITE);
        ArrayList<Piece> hits = board.checkHit(move);

        assertTrue("King must be allowed to help capture", hits.contains(Piece.BLACK));
    }

    @Test
    public void testKingCapturedOnThrone() {
        // King at 44 (Thron), black at all 4 neighbors
        Board board = createBoard(
                0, 0,
                1L << 44, 0,   // king
                (1L << 34) | (1L << 43) | (1L << 45) | (1L << 54), 0,
                0, 1L << 16
        );

        Move move = new Move(24, 34, Piece.BLACK); // dummy move
        ArrayList<Piece> hits = board.checkHit(move);

        assertTrue("King must be captured on throne when surrounded by 4 black pieces", hits.contains(Piece.KING));
    }

    @Test
    public void testKingCapturedAdjacentToThrone() {
        // King at 34, black at 33, 35, 24
        Board board = createBoard(
                0, 0,
                1L << 34, 0,   // king
                (1L << 33) | (1L << 35) | (1L << 24), 0,
                0, 1L << 16
        );

        Move move = new Move(43, 33, Piece.BLACK); // dummy move
        ArrayList<Piece> hits = board.checkHit(move);

        assertTrue("King must be captured when adjacent to throne and surrounded on 3 sides", hits.contains(Piece.KING));
    }

    @Test
    public void testDoubleCapture() {
        // White at 10, 12, 14
        // Black at 11 and 13
        Board board = createBoard(
                (1L << 10) | (1L << 15) | (1L << 14), 0,
                0, 0,
                (1L << 11) | (1L << 13), 0,
                0, 1L << 16
        );

        Move move = new Move(22, 12, Piece.WHITE);
        ArrayList<Piece> hits = board.checkHit(move);

        assertEquals(2, hits.size(), "Two black pieces should be captured");
        assertEquals(Piece.EMPTY, board.getPieceAt(11));
        assertEquals(Piece.EMPTY, board.getPieceAt(13));
    }

    @Test
    public void testCaptureAgainstCorner() {
        Board board = createBoard(
                (1L << 3) , 0,   // white
                44, 0,                       // king
                1L << 1, 0,                 // black at corner-adjacent
                1L << 0, 1L << 16
        );

        Move move = new Move(3, 2, Piece.WHITE);
        ArrayList<Piece> hits = board.checkHit(move);

        assertTrue("Black should be captured against corner", hits.contains(Piece.BLACK));
        assertEquals(Piece.EMPTY, board.getPieceAt(1));
    }

    @Test
    public void testCaptureAgainstEmptyThrone() {
        // White at 45, Black at 44 (throne), White at 43
        Board board = createBoard(
                (1L << 41), 0,   // white
                0, 0,
                1L << 43, 0,                  // black on throne
                0, 1L << 16
        );

        Move move = new Move(41, 42, Piece.WHITE);
        ArrayList<Piece> hits = board.checkHit(move);

        assertTrue("Black must be captured when sandwiched against empty throne", hits.contains(Piece.BLACK));
    }

    @Test
    public void testKingCapturedLikeNormalPiece() {
        // Black at 20 and 22, King at 21
        Board board = createBoard(
                0, 0,
                1L << 21, 0,                 // king
                (1L << 20) | (1L << 23), 0,  // black
                0, 1L << 16
        );

        Move move = new Move(23, 22, Piece.BLACK);
        ArrayList<Piece> hits = board.checkHit(move);

        assertTrue("King should be captured like a normal piece outside throne area", hits.contains(Piece.KING));
    }

    @Test
    public void testVerticalSandwich() {
        // White at 10 and 30, Black at 20
        Board board = createBoard(
                (1L << 10) | (1L << 40), 0,
                0, 0,
                1L << 20, 0,
                0, 1L << 16
        );

        Move move = new Move(40, 30, Piece.WHITE);
        ArrayList<Piece> hits = board.checkHit(move);

        assertTrue("Black should be captured vertically", hits.contains(Piece.BLACK));
        assertEquals(Piece.EMPTY, board.getPieceAt(20));
    }

    @Test
    public void testNoCaptureAtBoardEdge() {
        // White at 1, Black at 0 (edge), no white on the other side
        Board board = createBoard(
                1L << 12, 0,
                0, 0,
                1L << 10, 0,
                0, 1L << 16
        );

        Move move = new Move(12, 11, Piece.WHITE);
        ArrayList<Piece> hits = board.checkHit(move);

        assertTrue("No capture should occur at board edge", hits.isEmpty());
        assertEquals(Piece.BLACK, board.getPieceAt(10));
    }

    @Test
    public void testNoCaptureWhenNotSandwiched() {
        // White at 10, Black at 11, empty at 12
        Board board = createBoard(
                1L << 13, 0,
                0, 0,
                1L << 11, 0,
                0, 1L << 16
        );

        Move move = new Move(13, 12, Piece.WHITE);
        ArrayList<Piece> hits = board.checkHit(move);

        assertTrue("No capture should occur without sandwich", hits.isEmpty());
        assertEquals(Piece.BLACK, board.getPieceAt(11));
    }

    @Test
    public void testNoCaptureAgainstOccupiedThroneBlack() {
        // Setup:
        // White at 47
        // Black at 45
        // King occupies throne at 44 → throne is NOT empty
        Board board = createBoard(
                1L << 47, 0,            // white at 47
                1L << 44, 0,            // king on throne (occupied)
                1L << 45, 0,            // black at 45
                0, 1L << 16
        );

        // White moves from 47 to 46 (onto throne)
        Move move = new Move(47, 46, Piece.WHITE);
        ArrayList<Piece> hits = board.checkHit(move);

        // EXPECTATION:
        // → NO capture, because throne is occupied
        assertTrue("No capture must occur when throne is occupied", hits.contains(Piece.BLACK));
        assertEquals(Piece.EMPTY, board.getPieceAt(45), "Black piece must remain because throne is occupied");
        assertEquals(1, hits.size());
    }

    @Test
    public void testNoCaptureAgainstOccupiedThroneWhite() {
        // Setup:
        // White at 45
        // Black at 47
        // King occupies throne at 44 → throne is NOT empty
        Board board = createBoard(
                1L << 45, 0,            // white at 45
                1L << 44, 0,            // king on throne (occupied)
                1L << 47, 0,            // black at 47
                0, 1L << 16
        );

        // White moves from 47 to 46 (onto throne)
        Move move = new Move(47, 46, Piece.BLACK);
        ArrayList<Piece> hits = board.checkHit(move);

        // EXPECTATION:
        // → NO capture, because throne is occupied
        assertTrue("No capture must occur when throne is occupied"+board.getPieceAt(45), hits.isEmpty());
        assertEquals(Piece.WHITE, board.getPieceAt(45), "White piece must remain because throne is occupied");
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

        Move[] moves = board.generateLegalMoves(board.whiteKing, Player.WHITE);
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
