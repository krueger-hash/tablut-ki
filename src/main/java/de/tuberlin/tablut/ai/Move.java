package de.tuberlin.tablut.ai;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Objects;

@Getter
@Setter
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
    Move(int fromCol, int fromRow, int toCol, int toRow, Piece moved){
        this.from = fromCol + Bitboard90.cols * fromRow;
        this.to = toCol + Bitboard90.cols * toRow;
        this. movedPiece = moved;
    }

    static Move inputToMove(Board board,int fromCol,int fromRow, int toCol, int toRow){

        Piece moved;
        if(Bitboard90.getBitAsMatrix(board.white,fromRow,fromCol)){
            moved = Piece.WHITE;
        }
        else if(Bitboard90.getBitAsMatrix(board.whiteKing,fromRow, fromCol)){
            moved = Piece.KING;
        }
        else if(Bitboard90.getBitAsMatrix(board.black,fromRow, fromCol)) {
            moved = Piece.BLACK;
        }
        else {
            throw new IllegalArgumentException("No Piece at Position: col="+fromCol+" row="+fromRow);
        }
        return new Move(fromCol,fromRow,toCol,toRow,moved); // hier ist der alternative Konstruktor genutzt
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

        return "Move<row,col>{"
                + moved +" "
                + origin
                +"->"
                + target
                +"}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return from == move.from && to == move.to && movedPiece == move.movedPiece;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, movedPiece);
    }
}
