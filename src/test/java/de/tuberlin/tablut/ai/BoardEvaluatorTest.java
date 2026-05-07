package de.tuberlin.tablut.ai;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BoardEvaluatorTest {

    @Test
    public void testBlackWinIsTerminalScore() {
        Board board = new Board(new Bitboard90(), new Bitboard90(), new Bitboard90());

        assertEquals(100_000, BoardEvaluator.evaluate(board));
    }

    @Test
    public void testWhiteWinIsTerminalScore() {
        Board board = new Board(
                new Bitboard90(),
                new Bitboard90(1L, 0),
                new Bitboard90()
        );

        assertEquals(-100_000, BoardEvaluator.evaluate(board));
    }

    @Test
    public void testFewerWhitePiecesAreBetterForBlack() {
        Board manyWhitePieces = new Board(
                new Bitboard90((1L << 24) | (1L << 34) | (1L << 42), 0),
                new Bitboard90(1L << 44, 0),
                new Bitboard90(1L << 3, 0)
        );
        Board fewerWhitePieces = new Board(
                new Bitboard90(1L << 24, 0),
                new Bitboard90(1L << 44, 0),
                new Bitboard90(1L << 3, 0)
        );

        assertTrue(
                BoardEvaluator.evaluate(fewerWhitePieces)
                        > BoardEvaluator.evaluate(manyWhitePieces)
        );
    }

    @Test
    public void testOpenEscapeLineIsGoodForWhite() {
        Board openEscape = new Board(
                new Bitboard90(),
                new Bitboard90(1L << 4, 0),
                new Bitboard90()
        );
        Board blockedEscape = new Board(
                new Bitboard90(),
                new Bitboard90(1L << 4, 0),
                new Bitboard90((1L << 2) | (1L << 6), 0)
        );

        assertTrue(
                BoardEvaluator.evaluate(openEscape)
                        < BoardEvaluator.evaluate(blockedEscape)
        );
    }

    @Test
    public void testKingFartherFromEscapeIsBetterForBlack() {
        Board kingNearEscape = new Board(
                new Bitboard90(),
                new Bitboard90(1L << 14, 0),
                new Bitboard90(1L << 40, 0)
        );
        Board kingFarFromEscape = new Board(
                new Bitboard90(),
                new Bitboard90(1L << 44, 0),
                new Bitboard90(1L << 40, 0)
        );

        assertTrue(
                BoardEvaluator.evaluate(kingFarFromEscape)
                        > BoardEvaluator.evaluate(kingNearEscape)
        );
    }

    @Test
    public void testBlackPressureAroundKingIsGoodForBlack() {
        Board noPressure = new Board(
                new Bitboard90(),
                new Bitboard90(1L << 44, 0),
                new Bitboard90()
        );
        Board pressure = new Board(
                new Bitboard90(),
                new Bitboard90(1L << 44, 0),
                new Bitboard90((1L << 34) | (1L << 43) | (1L << 45), 0)
        );

        assertTrue(
                BoardEvaluator.evaluate(pressure)
                        > BoardEvaluator.evaluate(noPressure)
        );
    }
}
