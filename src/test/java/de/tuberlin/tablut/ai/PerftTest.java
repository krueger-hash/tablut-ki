package de.tuberlin.tablut.ai;

import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PerftTest {
    private String transformFen(String fen){
        return fen.replace("r","s").replace("R","w");
    }

    @Test
    public void testIsPerftCorrectForStartPositionBlack() {
        String transformedFen = transformFen("3rrr3/4r4/4R4/r3R3r/rrRRKRRrr/r3R3r/4R4/4r4/3rrr3 s 0 1");
        Board board = Board.fenToBoard(transformedFen);
        assertEquals(72, PerformanceTest.perft(board, 1, board.sideToMove), "Perft(1) should be 72 for base position black");
    }

    @Test
    public void testIsPerftCorrectForStartPositionWhite() {
        String transformedFen = "3rrr3/4r4/4R4/r3R3r/rrRRKRRrr/r3R3r/4R4/4r4/3rrr3 w 0 1".replace("r","s").replace("R","w");
        Board board = Board.fenToBoard(transformedFen);
        assertEquals(56, PerformanceTest.perft(board, 1, Player.WHITE), "Perft(1) should be 56 for base position white");
    }

    @Test
    public void testOurGroup1(){
        String transformedFen = "9/9/9/9/9/9/5RRRR/5RRKR/4RrR1r s";
        Board board = Board.fenToBoard(transformedFen);
        assertEquals(1, PerformanceTest.perft(board, 1, board.sideToMove), "Perft(1) should be 1 for base position black");
        assertEquals(44, PerformanceTest.perft(board, 2, board.sideToMove), "Perft(1) should be 1 for base position black");
        assertEquals(125, PerformanceTest.perft(board, 3, board.sideToMove), "Perft(1) should be 1 for base position black");
        assertEquals(6453, PerformanceTest.perft(board, 4, board.sideToMove), "Perft(1) should be 1 for base position black");
    }
}
