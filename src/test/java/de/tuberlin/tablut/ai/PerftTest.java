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
    public void testGroupAL1(){
        String transformedFen = transformFen("4rr3/4r4/5R3/r4r3/rr1r2Rrr/r3R3r/7R1/4r4/4rK3 s 1 12");
        Board board = Board.fenToBoard(transformedFen);
        assertEquals(82, PerformanceTest.perft(board, 1, board.sideToMove), "Perft(1) should be 82 for position Group AL1");
    }

    @Test
    public void testGroupAL2(){
        String transformedFen = transformFen("4rr3/4rK2r/4r4/5r1R1/r5R2/r6R1/2r5r/6r2/9 w 2 27");
        Board board = Board.fenToBoard(transformedFen);
        assertEquals(32, PerformanceTest.perft(board, 1, Player.WHITE), "Perft(1) should be 32 for position Group AL2");
    }

    @Test
    public void testGroupAB1(){
        String pointString = "...................................B......B.K........B...........................";
        Board board = Board.transformPointString(pointString, Player.BLACK);

        assertEquals(34, PerformanceTest.perft(board, 1, Player.BLACK), "Perft(1) should be 34 for position Group AB1");
        assertEquals(3463, PerformanceTest.perft(board, 2, Player.BLACK), "Perft(2) should be 3463 for position Group AB1");

    }
}
