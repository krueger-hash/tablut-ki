package de.tuberlin.tablut.ai;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static de.tuberlin.tablut.ai.Board.generateLegalMoves;
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

        return new Board(white, king, black);
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
        // 2026-04-23 changed from 0 to 1, since blockedPieces is now a static field
        assertEquals(Piece.EMPTY, board.getPieceAt(1), "Empty field expected at pos 0");
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
        ArrayList<Hit> hit = new ArrayList<Hit>();


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
        ArrayList<Hit> hit = new ArrayList<Hit>();

        Move move = new Move(30, 40, Piece.BLACK);
        board.applyMove(move);

        assertEquals(Piece.EMPTY, board.getPieceAt(30));
        assertEquals(Piece.BLACK, board.getPieceAt(40));
    }

    @Test
    public void testCheckHitNormalSandwichHorizontal() {
        Board board = createBoard(
                (1L << 10) | (1L << 13), 0,
                0, 0,
                1L << 11, 0,
                0, 1L << 16
        );

        Move move = new Move(22, 12, Piece.WHITE);
        ArrayList<Hit> hits = board.checkHit(move);
        board.hit(hits);

        boolean containsBlack = false;
        for (Hit h : hits) {
            if (h.piece() == Piece.BLACK) containsBlack = true;
        }

        assertTrue(containsBlack);
        assertEquals(Piece.EMPTY, board.getPieceAt(11));
    }

    @Test
    public void testKingCanCaptureBlack() {
        Board board = createBoard(
                1L << 13, 0,
                1L << 10, 0,
                1L << 11, 0,
                0, 1L << 16
        );

        Move move = new Move(22, 12, Piece.WHITE);
        ArrayList<Hit> hits = board.checkHit(move);

        board.hit(hits);

        boolean containsBlack = false;
        for (Hit h : hits) {
            if (h.piece() == Piece.BLACK) containsBlack = true;
        }

        assertTrue(containsBlack);
    }

    @Test
    public void testKingCapturedOnThrone() {
        Board board = createBoard(
                0, 0,
                1L << 44, 0,
                (1L << 34) | (1L << 43) | (1L << 45) | (1L << 54), 0,
                0, 1L << 16
        );

        Move move = new Move(24, 34, Piece.BLACK);
        ArrayList<Hit> hits = board.checkHit(move);
        board.hit(hits);


        boolean containsKing = false;
        for (Hit h : hits) {
            if (h.piece() == Piece.KING) containsKing = true;
        }

        assertTrue(containsKing);
    }

    @Test
    public void testKingCapturedAdjacentToThrone() {
        Board board = createBoard(
                0, 0,
                1L << 34, 0,
                (1L << 33) | (1L << 35) | (1L << 24), 0,
                0, 1L << 16
        );

        Move move = new Move(43, 33, Piece.BLACK);
        ArrayList<Hit> hits = board.checkHit(move);
        board.hit(hits);


        boolean containsKing = false;
        for (Hit h : hits) {
            if (h.piece() == Piece.KING) containsKing = true;
        }

        assertTrue(containsKing);
    }

    @Test
    public void testDoubleCapture() {
        Board board = createBoard(
                (1L << 10) | (1L << 15) | (1L << 14), 0,
                0, 0,
                (1L << 11) | (1L << 13), 0,
                0, 1L << 16
        );

        Move move = new Move(22, 12, Piece.WHITE);
        ArrayList<Hit> hits = board.checkHit(move);
        board.hit(hits);


        assertEquals(2, hits.size());
        assertEquals(Piece.EMPTY, board.getPieceAt(11));
        assertEquals(Piece.EMPTY, board.getPieceAt(13));
    }

    @Test
    public void testCaptureAgainstCorner() {
        Board board = createBoard(
                1L << 3, 0,
                44, 0,
                1L << 1, 0,
                1L << 0, 1L << 16
        );

        Move move = new Move(3, 2, Piece.WHITE);
        ArrayList<Hit> hits = board.checkHit(move);
        board.hit(hits);


        boolean containsBlack = false;
        for (Hit h : hits) {
            if (h.piece() == Piece.BLACK) containsBlack = true;
        }

        assertTrue(containsBlack);
        assertEquals(Piece.EMPTY, board.getPieceAt(1));
    }

    @Test
    public void testCaptureAgainstEmptyThrone() {
        Board board = createBoard(
                1L << 41, 0,
                0, 0,
                1L << 43, 0,
                0, 1L << 16
        );

        Move move = new Move(41, 42, Piece.WHITE);
        ArrayList<Hit> hits = board.checkHit(move);
        board.hit(hits);


        boolean containsBlack = false;
        for (Hit h : hits) {
            if (h.piece() == Piece.BLACK) containsBlack = true;
        }

        assertTrue(containsBlack);
    }

    @Test
    public void testKingCapturedLikeNormalPiece() {
        Board board = createBoard(
                0, 0,
                1L << 21, 0,
                (1L << 20) | (1L << 23), 0,
                0, 1L << 16
        );

        Move move = new Move(23, 22, Piece.BLACK);
        ArrayList<Hit> hits = board.checkHit(move);
        board.hit(hits);


        boolean containsKing = false;
        for (Hit h : hits) {
            if (h.piece() == Piece.KING) containsKing = true;
        }

        assertTrue(containsKing);
    }

    @Test
    public void testVerticalSandwich() {
        Board board = createBoard(
                (1L << 10) | (1L << 40), 0,
                0, 0,
                1L << 20, 0,
                0, 1L << 16
        );

        Move move = new Move(40, 30, Piece.WHITE);
        ArrayList<Hit> hits = board.checkHit(move);
        board.hit(hits);


        boolean containsBlack = false;
        for (Hit h : hits) {
            if (h.piece() == Piece.BLACK) containsBlack = true;
        }

        assertTrue(containsBlack);
        assertEquals(Piece.EMPTY, board.getPieceAt(20));
    }

    @Test
    public void testNoCaptureAtBoardEdge() {
        Board board = createBoard(
                1L << 12, 0,
                0, 0,
                1L << 10, 0,
                0, 1L << 16
        );

        Move move = new Move(12, 11, Piece.WHITE);
        ArrayList<Hit> hits = board.checkHit(move);
        board.hit(hits);


        assertTrue(hits.isEmpty());
        assertEquals(Piece.BLACK, board.getPieceAt(10));
    }

    @Test
    public void testNoCaptureWhenNotSandwiched() {
        Board board = createBoard(
                1L << 13, 0,
                0, 0,
                1L << 11, 0,
                0, 1L << 16
        );

        Move move = new Move(13, 12, Piece.WHITE);
        ArrayList<Hit> hits = board.checkHit(move);
        board.hit(hits);


        assertTrue(hits.isEmpty());
        assertEquals(Piece.BLACK, board.getPieceAt(11));
    }

    @Test
    public void testNoCaptureAgainstOccupiedThroneBlack() {
        Board board = createBoard(
                1L << 47, 0,
                1L << 44, 0,
                1L << 45, 0,
                0, 1L << 16
        );

        Move move = new Move(47, 46, Piece.WHITE);
        ArrayList<Hit> hits = board.checkHit(move);
        board.hit(hits);


        boolean containsBlack = false;
        for (Hit h : hits) {
            if (h.piece() == Piece.BLACK) containsBlack = true;
        }

        assertTrue(containsBlack);
        assertEquals(Piece.EMPTY, board.getPieceAt(45));
    }

    @Test
    public void testNoCaptureAgainstOccupiedThroneWhite() {
        Board board = createBoard(
                1L << 45, 0,
                1L << 44, 0,
                1L << 47, 0,
                0, 1L << 16
        );

        Move move = new Move(47, 46, Piece.BLACK);
        ArrayList<Hit> hits = board.checkHit(move);
        board.hit(hits);


        assertTrue(hits.isEmpty());
        assertEquals(Piece.WHITE, board.getPieceAt(45));
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
    public void testIsLegalMoveTargetViaReflection() throws Exception {
        Board board = new Board();
        Method isLegalMoveTarget = Board.class.getDeclaredMethod("isLegalMoveTarget", int.class, int.class, int.class);
        isLegalMoveTarget.setAccessible(true);

        assertTrue((Boolean) isLegalMoveTarget.invoke(board, 21, 22, 1));
        assertTrue((Boolean) isLegalMoveTarget.invoke(board, 22, 32, 10));
        assertFalse((Boolean) isLegalMoveTarget.invoke(board, 21, 19, -1));  // horizontal wrap into previous row
        assertFalse((Boolean) isLegalMoveTarget.invoke(board, 8, 9, 1));      // separator column
        assertFalse((Boolean) isLegalMoveTarget.invoke(board, 88, 98, 10));   // above range
        assertFalse((Boolean) isLegalMoveTarget.invoke(board, 0, -10, -10));  // below range
    }

    @Test
    public void testGenerateLegalMovesForWhitePiece() {
        Board board = createBoard(
                1L << 22, 0,                  // white at 22
                0, 0,                         // no king
                (1L << 12) | (1L << 24), 0,   // black blocks north and east
                0, 1L << 16
        );

        ArrayList<Move> legalMoves = generateLegalMoves(board, Player.WHITE);

        Set<String> expectedMoves = new HashSet<>(Arrays.asList(
                "22-20", "22-21", "22-23",
                "22-32", "22-42", "22-52", "22-62", "22-72", "22-82"
        ));

        Set<String> actualMoves = new HashSet<>();
        for (Move move : legalMoves) {
            assertEquals(Piece.WHITE, move.movedPiece, "Generated move should belong to a white piece");
            actualMoves.add(move.from + "-" + move.to);
        }

        assertEquals(expectedMoves, actualMoves, "generateLegalMoves should return exactly the reachable moves");
    }

    @Test
    public void testGenerateLegalMovesAllowsKingToMoveOntoBlockedCorner() {
        Board board = createBoard(
                0, 0,
                1L << 3, 0,                    // king at 3
                (1L << 4) | (1L << 13), 0,     // block east and south
                0, 0
        );

        ArrayList<Move> legalMoves = generateLegalMoves(board, Player.WHITE);

        Set<String> actualMoves = new HashSet<>();
        for (Move move : legalMoves) {
            assertEquals(Piece.KING, move.movedPiece, "Generated move should belong to the king");
            actualMoves.add(move.from + "-" + move.to);
        }

        Set<String> expectedMoves = new HashSet<>(Arrays.asList("3-2", "3-1", "3-0"));
        assertEquals(expectedMoves, actualMoves, "King should be allowed to move onto the blocked corner");
    }

    @Test
    public void testGenerateLegalMovesDoesNotAllowWhitePieceOntoBlockedCorner() {
        Board board = createBoard(
                1L << 3, 0,                    // white at 3
                0, 0,
                (1L << 4) | (1L << 13), 0,     // block east and south
                0, 0
        );

        ArrayList<Move> legalMoves = generateLegalMoves(board, Player.WHITE);

        Set<String> actualMoves = new HashSet<>();
        for (Move move : legalMoves) {
            assertEquals(Piece.WHITE, move.movedPiece, "Generated move should belong to a white piece");
            actualMoves.add(move.from + "-" + move.to);
        }

        Set<String> expectedMoves = new HashSet<>(Arrays.asList("3-2", "3-1"));
        assertEquals(expectedMoves, actualMoves, "Normal white pieces must not be allowed to move onto blocked corners");
    }

    @Test
    public void testGenerateLegalMovesDoesNotAllowWhitePieceOntoEmptyThrone() {
        Board board = createBoard(
                1L << 41, 0,                   // white left of throne
                0, 0,
                0, 0,
                0, 0
        );

        ArrayList<Move> legalMoves = generateLegalMoves(board, Player.WHITE);

        Set<String> actualMoves = new HashSet<>();
        for (Move move : legalMoves) {
            actualMoves.add(move.from + "-" + move.to);
        }

        assertFalse(actualMoves.contains("41-44"), "Normal white pieces must not be allowed to enter the empty throne");
        assertTrue("Move before the throne should stay legal", actualMoves.contains("41-42"));
        assertTrue("Move before the throne should stay legal", actualMoves.contains("41-43"));
        assertTrue("Empty throne may be passed over", actualMoves.contains("41-45"));
        assertTrue("Empty throne may be passed over", actualMoves.contains("41-46"));
        assertTrue("Empty throne may be passed over", actualMoves.contains("41-47"));
        assertTrue("Empty throne may be passed over", actualMoves.contains("41-48"));
    }

    @Test
    public void testGenerateLegalMovesDoesNotAllowKingBackOntoEmptyThrone() {
        Board board = createBoard(
                0, 0,
                1L << 41, 0,                   // king left of throne
                0, 0,
                0, 0
        );

        ArrayList<Move> legalMoves = generateLegalMoves(board, Player.WHITE);

        Set<String> actualMoves = new HashSet<>();
        for (Move move : legalMoves) {
            assertEquals(Piece.KING, move.movedPiece, "Generated move should belong to the king");
            actualMoves.add(move.from + "-" + move.to);
        }

        assertFalse(actualMoves.contains("41-44"), "King must not be allowed to re-enter the empty throne");
        assertTrue("King may pass over the empty throne", actualMoves.contains("41-45"));
        assertTrue("King may pass over the empty throne", actualMoves.contains("41-46"));
        assertTrue("King may pass over the empty throne", actualMoves.contains("41-47"));
        assertTrue("King may pass over the empty throne", actualMoves.contains("41-48"));
    }

    @Test
    public void testGenerateLegalMovesDoesNotJumpOverOccupiedThrone() {
        Board board = createBoard(
                1L << 41, 0,                   // white left of throne
                1L << 44, 0,                   // king on throne
                0, 0,
                0, 0
        );

        ArrayList<Move> legalMoves = generateLegalMoves(board, Player.WHITE);

        Set<String> actualMoves = new HashSet<>();
        for (Move move : legalMoves) {
            actualMoves.add(move.from + "-" + move.to);
        }

        assertFalse(actualMoves.contains("41-44"), "Occupied throne must not be a legal destination");
        assertFalse(actualMoves.contains("41-45"), "Occupied throne must not be jumpable");
        assertFalse(actualMoves.contains("41-46"), "Occupied throne must not be jumpable");
        assertFalse(actualMoves.contains("41-47"), "Occupied throne must not be jumpable");
        assertFalse(actualMoves.contains("41-48"), "Occupied throne must not be jumpable");
        assertTrue("Moves before the occupied throne should stay legal", actualMoves.contains("41-42"));
        assertTrue("Moves before the occupied throne should stay legal", actualMoves.contains("41-43"));
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
