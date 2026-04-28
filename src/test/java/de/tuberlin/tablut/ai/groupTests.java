package de.tuberlin.tablut.ai;

import org.junit.Test;
import org.junit.Ignore;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class groupTests {
    private record GroupCase(String name, String board, char sideToMove, int... perft) {
    }

    private static final List<GroupCase> CASES = List.of(
            new GroupCase("Königsflucht", "9/9/9/9/9/9/3r5/1K7/9 w 0 1", 'w', 16),
            new GroupCase("Sandwichfalle", "9/9/9/2r6/2R6/r8/9/9/9 s 0 1", 's', 25),
            new GroupCase("König ist Bewegungseingeschränkt", "X3b3X/7b1/2w2b3/7b1/4X4/3bK1b2/7b1/3w3b1/X7X s 0 1", 's'),
//            new GroupCase("Einsperren des Königs", "...................................B......B.K........B...........................", 's', 34, 3463, 358016, 33728521),
//            new GroupCase("Flucht des Königs", "..............................W....B..W.K.W.....W.........................B......", 'w', 54, 3089, 454549, 53957077),
            new GroupCase("Thronübersprung", "9/9/7K1/9/3r5/9/9/9/9 s 0 1", 's', 15),
            new GroupCase("Schlag über Thron", "9/4r4/K8/4R4/9/9/4r4/9/9 s 0 1", 's', 21),
            new GroupCase("Gewinnspiel weiß", "7K1/6r1r/2R3r2/9/3r5/9/1r1r5/9/9  w 20 10", 'w', 29),
            new GroupCase("Gewinnspiel schwarz", "7K1/6r1r/2R3r2/9/3r5/9/1r1r5/9/9  s 21 10", 's', 57),
            new GroupCase("König neben Thron unter Druck", "9/9/9/9/2rK5/3r5/9/9/9 s 0 1", 's', 21),
//            new GroupCase("Passiver Sandwich-Test", "9/9/3r1r3/4R4/7K1/9/9/9/9 w 0 1", 'w', 31),
//            new GroupCase("mittelspiel-thron-besetzt", "3rrr3/4r4/4R4/r3R4/rrRRKR1r1/r3R3r/4R4/4r4/3rrr3 s 5 10", 's', 73, 3587, 262916, 13462626),
            new GroupCase("Endspiel-könig-auf-flucht", "3r5/9/9/4R4/r4R2r/7K1/9/2r6/5r3 w 3 24", 'w', 43, 2417, 95843, 5343738),
            new GroupCase("Weiß fast gewonnen", "4rr3/4r4/5R3/r4r3/rr1r2Rrr/r3R3r/7R1/4r4/4rK3 s 1 12", 's', 82),
            new GroupCase("Weiß in der Klemme", "4rr3/4rK2r/4r4/5r1R1/r5R2/r6R1/2r5r/6r2/9 w 2 27", 'w', 32),
//            new GroupCase("König im Zentrum unter Druck", "...........B..........W................BK........W..........B....................", 's', 43, 1171, 46572, 1518965),
            new GroupCase("Fluchtmöglichkeiten", "..............B.....W..........B......W.K.B...............W....B.................", 'w', 33, 1645, 62941, 2967825),
            new GroupCase("König kann gerettet werden", "000000000000000000000W000000000B00000B00KB0000000B0000000000000000000000000000000", 'w', 18),
            new GroupCase("König kann geschlagen werden", "0000000000000000000000000000000B00000B00KB0000000B0000000000000000000000000000000", 's', 44),
//            new GroupCase("Middlegame", "3rrr3/9/4R4/r3R3r/rrR1KRR1r/r3R3r/3R5/4r4/3rrr3 s 4 7", 's', 67, 3983, 279567, 16423455),
//            new GroupCase("Endgame", "9/9/4r4/3r1r3/4K4/3R5/9/9/9 w 12 24", 'w', 25, 705, 18205, 582800),
//            new GroupCase("Rettung des Königs 2", "9/2R3r2/9/9/9/r8/Rr1rR4/4rK3/9 w 1 25", 'w', 34, 1714, 61125, 3221238),
//            new GroupCase("Blockade des Königs", "9/9/9/8r/8K/9/9/9/5r3 s 5 50", 's', 24, 294, 7230, 102390),
//            new GroupCase("König als einschließende Figur", "5R3/rr3r3/1KR6/9/9/9/9/9/9 w 0 30", 'w', 24, 802, 25326, 918191),
//            new GroupCase("Last Man Standing", "2r6/rK7/9/9/9/9/9/9/9 w 0 48", 'w', 15, 301, 4325, 87107),
            new GroupCase("Endspielstellung", "....b......b....w.......bk...............w..............b...................b....", 'w', 28),
            new GroupCase("Wettrennen", "1r7/9/rKRr1r3/1RR6/1r7/9/9/1r7/9 w 1 1", 'w', 15),
            new GroupCase("Thronbelagerung", "9/9/4r4/3rRr3/2rrKRr2/3RrR3/4r4/9/9 w 1 1", 'w', 12),
//            new GroupCase("Schraubstock von e5", "9/4Rr3/9/4r4/3rKR2r/4rr3/9/9/9 w 0 42", 'w', 10, 491, 8485, 445439),
//            new GroupCase("Königlicher Abschluss", "7r1/R8/9/9/9/K8/9/1r7/9 s 0 39", 's', 30, 728, 19629, 522017),
//            new GroupCase("Zentrum Verteidigen", "3rr4/4r4/4R4/r2R1R2r/rr2K2rr/2rR1R3/9/4r4/4rr3 w 0 8", 'w', 41, 3105, 132386, 10278359),
//            new GroupCase("Einkesseln des König", "3rr4/4rr3/1r2k1r2/4RRr2/rrRR3r1/1r1RRr3/4R4/9/3rrr3 s 0 19", 's', 77, 2432, 188885, 6659945),
            new GroupCase("Weg für den König frei machen", "2r1rr3/2RRr4/2R6/r3R3r/rr2KRRrr/r3R3r/4R4/3rrr3/4r4 s 0 5", 's', 72),
            new GroupCase("König wird blockiert", "2r1rr3/2RRr4/R8/4R3r/r2rKRRrr/r3R3r/4R4/3rrr3/4r4 w 08", 'w', 57),
//            new GroupCase("Weißer Durchbruch", "1K7/9/9/9/9/9/9/9/4r4 w 0 15", 'w', 16, 0),
//            new GroupCase("Midgame State", "3r5/9/9/4R4/2r1K2R1/4R4/9/9/4r4 s 0 5", 's', 37),
//            new GroupCase("König fast geschlagen", "3r5/9/6R2/r5r2/r2R1R1R1/4rKr2/4rrr2/4R1R2/9 s 0 1", 's', 44),
            new GroupCase("Eck-Sperre", "9/r8/4R4/9/4K4/9/9/8r/9 s 0 1", 's', 28),
            new GroupCase("König hat eine Chance", "6R2/2R4R1/9/6R2/6R2/4rKr2/4rrr2/9/3r2r2 w 0 1", 'w'),
            new GroupCase("Einzug-Schlagtest", "9/9/9/9/9/9/5RRRR/5RRKR/4RrR1r s 0 1", 's', 1, 44, 125, 6453),
//            new GroupCase("König beschattet einen Dorfbewohner", "4K4/4r4/9/9/9/9/9/9/9 s 0 1", 's', 14, 189, 2717, 40689),
            new GroupCase("schwarze Figur wird geschlagen", "2s2s3/2w6/9/w2ww3s/s2sKw1ss/s3w4/9/9/5s3 w 0 1", 'w', 47),
            new GroupCase("Schwarz gewinnt im nächsten Zug", "4s4/9/4w4/8s/s1sK1w1ss/s1wsw4/9/9/9 s 0 1", 's', 44),
            new GroupCase("Zum Sieg geschlagen", "9/9/9/1Rr5R/r1K6/9/1r7/9/9", 'w'),
            new GroupCase("Qual der Wahl", "1w5s1/4k4/s7s/s7s/9/3w5/sws6/9/9 w 0 1", 'w', 48),
//            new GroupCase("Schutzschild", "9/2R2r3/2KRr1r2/2R6/9/9/5R3/9/9 s 2 31", 's', 29, 1116, 37840, 2082159),
//            new GroupCase("Eingekreist von 4 Seiten", "3R5/3r5/1rK2r3/R2r5/9/9/9/9/9 w 6 41", 'w', 24, 995, 31703, 1458235),
//            new GroupCase("König und Sonderfeld", ".K.....................W..................................................S......", 's', 16),
//            new GroupCase("Weiß kann schlagen", ".............................W................................KWS.........S......", 'w', 39),
            new GroupCase("All Roads Lead to Rome", "3rr4/4r4/5R3/r1R1Rr2r/rr1RKR1Rr/4R3r/5R3/4r4/3rr4 w 0 10", 'w', 59, 3553, 206212, 13101485)
//            new GroupCase("White Mate in One", "3rrr3/9/3R5/8r/rr7/r1R1RR3/4R3r/4rKRr1/5rr2 s 0 21", 's', 69, 3493, 248449, 12680595),
//            new GroupCase("Startaufstellung, Schwarz am Zug", "3rrr3/4r4/4R4/r3R3r/rrRRKRRrr/r3R3r/4R4/4r4/3rrr3 s 0 1", 's', 56),
//            new GroupCase("Startaufstellung, Weiß am Zug", "3rrr3/4r4/4R4/r3R3r/rrRRKRRrr/r3R3r/4R4/4r4/3rrr3 w 0 1", 'w', 72),
//            new GroupCase("Midgame State 1", "4rr3/3rr3r/4R4/r3R4/rrRRKRRrr/r3R3r/6R2/4r4/3rrr3 w 0 1", 'w', 57, 4608, 265009, 21615696),
//            new GroupCase("Midgame State 2", "4rrR2/3rr3r/4R4/rr2R4/r1RRKR1rr/r3R3r/2R6/7r1/3rrr3 w 0 1", 'w', 61, 4965, 300250, 24749620),
//            new GroupCase("König in Bedrängnis", "9/r2r5/9/5RR2/9/1r7/1Kr6/5R3/7r1 w 0 1", 'w', 36, 2228, 90203, 5316557),
//            new GroupCase("Kurz vor dem Ziel", "1r7/9/5r3/9/9/9/3R5/2K5r/1r2r4 s 0 1", 's', 61, 1843, 106600, 2949759)
//            new GroupCase("König in Zentrum unter Druck", ".............B.......WBW.....W.B.W.....BKB.....W...W.....WBW.......B............. s 0 1", 's', 12),
//            new GroupCase("Fluchtmöglichkeiten", "...........B...B.....W.W.......K.......W.W.....B...B...............W............. w 0 1", 'w', 44)
    );

    @Test
    public void groupPerftDepthOneCasesMatchExpectedCounts() {
        StringBuilder failures = new StringBuilder();

        for (GroupCase groupCase : CASES) {
            if (groupCase.perft.length == 0) {
                continue;
            }

            Board board = boardFrom(groupCase);
            int actual = PerformanceTest.perft(board, 1, board.sideToMove);
            int expected = groupCase.perft[0];
            if (actual != expected) {
                failures.append(groupCase.name)
                        .append(" perft(1): expected ")
                        .append(expected)
                        .append(" but was ")
                        .append(actual)
                        .append(System.lineSeparator());
            }
        }

        if (!failures.isEmpty()) {
            fail(failures.toString());
        }
    }

//    @Ignore("Slow regression suite; run manually when validating full perft counts.")
    @Test
    public void groupPerftCasesMatchAllExpectedCounts() {
        for (GroupCase groupCase : CASES) {
            Board board = boardFrom(groupCase);

            for (int depth = 1; depth <= groupCase.perft.length; depth++) {
                int expected = groupCase.perft[depth - 1];
                assertEquals(
                        groupCase.name + " perft(" + depth + ")",
                        expected,
                        PerformanceTest.perft(board, depth, board.sideToMove)
                );
            }
        }
    }

    private static Board boardFrom(GroupCase groupCase) {
        String board = groupCase.board.trim();
        if (board.contains("/")) {
            return Board.fenToBoard(normalizeFen(board, groupCase.sideToMove));
        }

        String pointString = board.split("\\s+")[0].replace('0', '.');
        return Board.transformPointString(pointString, playerFrom(groupCase.sideToMove));
    }

    private static String normalizeFen(String fen, char sideToMove) {
        String normalized = fen.trim().replaceAll("\\s+", " ");
        String[] parts = normalized.split(" ");
        String position = parts[0].replace("X", "1");
        String side = parts.length > 1 ? parts[1] : String.valueOf(sideToMove);
        return position + " " + side;
    }

    private static Player playerFrom(char sideToMove) {
        return switch (Character.toLowerCase(sideToMove)) {
            case 's', 'b' -> Player.BLACK;
            case 'w' -> Player.WHITE;
            default -> throw new IllegalArgumentException("Undefined side to move: " + sideToMove);
        };
    }
}
