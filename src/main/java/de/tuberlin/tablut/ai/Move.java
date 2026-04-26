package de.tuberlin.tablut.ai;

import java.util.Arrays;

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

    static Move inputToMove(Board board,int fromX,int fromY, int toX, int toY){

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

    // gibt Ursprung und Ziel des Moves als einen Int Array zur
    static int[] moveToIndizes(Move move){
        int[] fromCoords = Bitboard90.bitToMatrix(move.from);
        int[] toCoords = Bitboard90.bitToMatrix(move.to);
        int fromRow = fromCoords[0];
        int fromCol = fromCoords[1];
        int toRow = toCoords[0];
        int toCol = toCoords[1];
        return new int[]{fromRow,fromCol,toRow,toCol};
    }

    @Override
    public String toString() {
        String moved;
        if (this.movedPiece == Piece.BLACK){moved ="b";}
        else if (this.movedPiece == Piece.WHITE){moved="w";}
        else if (this.movedPiece == Piece.KING){moved="K";}
        else {throw new IllegalArgumentException("Something wrong with movedPiece - called with:"+this.movedPiece);}

        String origin = Arrays.toString(Bitboard90.bitToMatrix(this.from));
        String target = Arrays.toString(Bitboard90.bitToMatrix(this.to));

        return "Move{"
                + moved +" "
                + origin
                +"->"
                + target
                +"}";
    }
}
