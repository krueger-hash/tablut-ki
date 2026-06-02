package de.tuberlin.tablut.ai.SearchAlgorithms;

import de.tuberlin.tablut.ai.BestMoveInTime;
import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.BoardEvaluator;
import de.tuberlin.tablut.ai.Move;
import org.junit.Test;

import static de.tuberlin.tablut.ai.Board.deepCopy;
import static de.tuberlin.tablut.ai.BoardEvaluator.MIN_PLAYER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PVSTest {
    private static final int INF = 99999999;


    // Hilfsmethode für PVS-Aufruf
    private SearchResult runPVS(Board board, int depth) throws SearchStoppedException {
        PrincipalVariation pvs = new PrincipalVariation(depth);
        return pvs.pvSearch(board, depth, -INF, INF);
    }

    // -------------------------------------------------------------
    // 1) Test: PVS liefert gleiche Bewertung wie AlphaBeta
    // -------------------------------------------------------------
    @Test
    public void testPVSMatchesAlphaBetaValue() throws SearchStoppedException {
        String fen = "2b1Kb3/b4b3/b4b3/b4b3/b4b3/b4b3/b4b3/b4b3/5b3 w 0";
        Board board = Board.fenToBoard(fen); // Beispielstellung

        SearchResult ab = AlphaBeta.sortedAlphaBetaSearch(board, 3, -INF, INF);
        SearchResult pvs = runPVS(board, 3);

        int expected = ab.value;
        if (board.sideToMove == MIN_PLAYER) {
            expected = -expected;
        }

        assertEquals(expected, pvs.value);

    }
    @Test
    public void testPVSMatchesAlphaBetaValue2() throws SearchStoppedException {
        String fen = "3K2b2/2b6/9/9/9/9/9/9/6b2 b 45";
        Board board = Board.fenToBoard(fen); // Beispielstellung

        SearchReport ab = BestMoveInTime.searchAtDepth(board, 3, Integer.MAX_VALUE, BestMoveInTime::alphaBetaSearch, new SearchContext());

        //ABResult ab = AlphaBeta.sortedAlphaBetaSearch(board, 3, -INF, INF);
        SearchResult pvs = runPVS(board, 3);

        //int expected = ab.value;
        //if (board.sideToMove == MIN_PLAYER) {
        //    expected = -expected;
        //}

        assertEquals(ab.value(), pvs.value);

    }
    @Test
    public void testPVSMatchesAlphaBetaValueStalemate() throws SearchStoppedException {
        String fen = "2b1Kb3/b4b3/b4b3/b4b3/b4b3/b4b3/b4b3/b4b3/5b3 w 0";
        Board board = Board.fenToBoard(fen); // Beispielstellung

        SearchResult ab = AlphaBeta.sortedAlphaBetaSearch(board, 5, -INF, INF);
        SearchResult pvs = runPVS(board, 5);

        assertEquals(ab.value, pvs.value,
                "PVS und AlphaBeta müssen denselben Wert liefern");
    }
    // -------------------------------------------------------------
    // 2) Test: PVS liefert gleiche PV wie AlphaBeta
    // -------------------------------------------------------------
   /* @Test
    public void testPVSMatchesAlphaBetaPV() throws SearchStoppedException {
        String fen = "2b1Kb3/b4b3/b4b3/b4b3/b4b3/b4b3/b4b3/b4b3/5b3 w 0";
        Board board = Board.fenToBoard(fen); // Beispielstellung

        ABResult ab = AlphaBeta.sortedAlphaBetaSearch(board, 5, -INF, INF);
        ABResult pvs = runPVS(board, 5);

        System.out.println("AB:"+ab.trace);
        System.out.println("PVS:"+pvs.trace);
        assertEquals(ab.trace, pvs.trace,
                "AB:"+ab.trace+"PVS"+pvs.trace+"PVS und AlphaBeta müssen dieselbe Principal Variation liefern");
    }*/



    // -------------------------------------------------------------
    // 4) Test: PVS bricht korrekt bei depth=0 ab
    // -------------------------------------------------------------
    @Test
    public void testPVSDepthZero() throws SearchStoppedException {
        String fen = "4K2b1/1b7/9/9/9/9/9/9/9 b 20";
        Board board = Board.fenToBoard(fen); // Beispielstellung
        SearchResult pvs = runPVS(board, 0);

        int eval = BoardEvaluator.evaluate(board);

        assertEquals(eval, pvs.value,
                "Bei depth=0 muss PVS die statische Bewertung liefern");
        assertTrue(pvs.trace.isEmpty(),
                "Bei depth=0 darf die PV leer sein");
    }


    // -------------------------------------------------------------
    // 5) Test: PVS erzeugt eine PV, die konsistent ist
    // -------------------------------------------------------------
    @Test
    public void testPVIsConsistent() throws SearchStoppedException {
        String fen = "4K2b1/1b7/9/9/9/9/9/9/9 b 20";
        Board board = Board.fenToBoard(fen); // Beispielstellung
        SearchResult pvs = runPVS(board, 4);

        Board clone = deepCopy(board);

        for (Move m : pvs.trace) {
            assertTrue(Board.generateLegalMoves(clone, clone.sideToMove).contains(m),
                    "Jeder Zug in der PV muss legal sein");
            clone.makeMove(m);
        }
    }
}
