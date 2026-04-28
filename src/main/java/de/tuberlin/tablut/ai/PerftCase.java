package de.tuberlin.tablut.ai;

@Deprecated
public class PerftCase {
    String fen;
    int perft1;
    int perft2;
    int perft3;
    int perft4;

    PerftCase(String fen) {
        this(fen, -1, -1, -1, -1);
    }

    PerftCase(String fen, int perft1) {
        this(fen, perft1, -1, -1, -1);
    }

    PerftCase(String fen, int perft1, int perft2) {
        this(fen, perft1, perft2, -1, -1);
    }

    PerftCase(String fen, int perft1, int perft2, int perft3) {
        this(fen, perft1, perft2, perft3, -1);
    }

    PerftCase(String fen, int perft1, int perft2, int perft3, int perft4) {
        this.fen = fen;
        this.perft1 = perft1;
        this.perft2 = perft2;
        this.perft3 = perft3;
        this.perft4 = perft4;
    }

    static PerftCase capture1 = new PerftCase("3wb4/2b6/9/9/9/9/9/9/9 s");
    static PerftCase capture2 = new PerftCase("3Kb4/2b6/9/9/9/9/9/9/9 s");
    static PerftCase capture3 = new PerftCase("3bw4/2K6/9/9/9/9/9/9/9 w");
    static PerftCase capture4 = new PerftCase("3bK4/2w6/9/9/9/9/9/9/9 w");
    static PerftCase capture5 = new PerftCase("3bw4/2w6/9/9/9/9/9/9/9 w");

    static PerftCase cornerNW = new PerftCase("1K7/2b6/9/9/9/9/9/9/9 s");
    static PerftCase cornerNE = new PerftCase("7w1/6b2/9/9/9/9/9/9/9 s");
    static PerftCase cornerSE = new PerftCase("9/9/9/9/9/9/9/6K2/7b1 w");
    static PerftCase cornerSW = new PerftCase("9/9/9/9/9/9/9/2w6/1b7 w");

    static PerftCase aa1 = new PerftCase("4b4/7b1/2w2b3/7b1/9/3bK1s2/7b1/3w3b1/9 s");
    static PerftCase aa2 = new PerftCase("4b4/7b1/2w6/7b1/9/3bbK1s2/4b4/3w3b1/9 w");
    static PerftCase ai1 = new PerftCase("3rrr3/4r4/4R4/r3R4/rrRRKR1r1/r3R3r/4R4/4r4/3rrr3 s", 73, 3587, 262916, 13462626);
    static PerftCase ai2 = new PerftCase("3r5/9/9/4R4/r4R2r/7K1/9/2r6/5r3 w", 43, 2417, 95843, 5343738);
    static PerftCase al1 = new PerftCase("4rr3/4r4/5R3/r4r3/rr1r2Rrr/r3R3r/7R1/4r4/4rK3 s", 82);
    static PerftCase al2 = new PerftCase("4rr3/4rK2r/4r4/5r1R1/r5R2/r6R1/2r5r/6r2/9 w", 32);

    static PerftCase b1_1 = new PerftCase("3rrr3/9/4R4/r3R3r/rrR1KRR1r/r3R3r/3R5/4r4/3rrr3 s", 67, 3983, 279567, 16423455);
    static PerftCase b1_2 = new PerftCase("9/9/4r4/3r1r3/4K4/3R5/9/9/9 w", 25, 705, 18205, 582800);

    static PerftCase j1 = new PerftCase("1r7/9/rKRr1r3/1RR6/1r7/9/9/1r7/9 w");
    static PerftCase j2 = new PerftCase("9/9/4r4/3rRr3/2rrKRr2/3RrR3/4r4/9/9 w");
}
