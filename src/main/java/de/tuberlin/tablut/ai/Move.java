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

    // alternativer Constructor, der schematisch von der konkreten Implementierung von Bitboard unabhängig ist (da er in Abhängigkeit der cols das entsprechende Bit berechnet)
    Move(int fromX, int fromY, int toX, int toY, Piece moved){
        this.from = fromX + Bitboard90.cols * fromY;
        this.to = toX + Bitboard90.cols * toY;
        this. movedPiece = moved;
    }

    Move inputToMove(Board board,int fromX,int fromY, int toX, int toY){

        Piece moved;
        if(Bitboard90.getBitAsMatrix(board.white,fromX,fromY)){
            moved = Piece.WHITE;
        }
        else if(Bitboard90.getBitAsMatrix(board.whiteKing,fromX,fromY)){
            moved = Piece.KING;
        }
        else if(Bitboard90.getBitAsMatrix(board.black,fromX,fromY)) {
            moved = Piece.BLACK;
        }
        else {
            throw new IllegalArgumentException("No Piece at Position: x="+fromX+" y="+fromY);
        }

        return new Move(fromX,fromY,toX,toY,moved); // hier ist der alternative Konstruktor genutzt

    }
}
