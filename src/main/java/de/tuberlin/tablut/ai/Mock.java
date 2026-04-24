package de.tuberlin.tablut.ai;

public class Mock {

    static void main() {
        String fen ="3bbb3/4b4/4w4/b3w3b/bbwwKwwbb/b3w3b/4w4/4b4/3bbb3 S";

        Board test = Board.fenToBoard(fen);

        test.printBoard();


    }
}
