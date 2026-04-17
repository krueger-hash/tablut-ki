package de.tuberlin.tablut.ai;

public class Move {

    //Das spielfeld wird einfach durchnummeriert, entsprechend den Bits, also von 0 bis 89 von oben links nach unten rechts

    int from;
    int to;
    Piece movedPiece;

    public Move(int from, int to, Piece movedPiece) {
        this.from = from;
        this.to = to;
        this.movedPiece = movedPiece;
    }
}
