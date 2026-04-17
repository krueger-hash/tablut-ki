package de.tuberlin.tablut.ai;

public class Board {

    static long whiteLow = (1L << 24) | (1L << 34) | (1L << 42) | (1L << 43) | (1L << 45) | (1L << 46) | (1L << 54);
    static long whiteHigh = (1L);

    static long whiteKingLow = 1L << 44;
    static long whiteKingHigh = 0L;

    static long blackLow = (1L << 3) | (1L << 4)| (1L << 5)| (1L << 14)| (1L << 30)| (1L << 38)| (1L << 40)|
            (1L << 41)| (1L << 47)| (1L << 48)| (1L << 50)| (1L << 58);
    static long blackHigh = (1L << 10) | (1L << 19) | (1L << 20) | (1L << 21);

    static long blockedLow = (1L << 0) | (1L << 8);
    static long blockedHigh = (1L << 16) | (1L << 24);

    static Bitboard90 white = new Bitboard90(whiteLow, whiteHigh);
    static Bitboard90 whiteKing = new Bitboard90(whiteKingLow, whiteKingHigh);
    static Bitboard90 black = new Bitboard90(blackLow, blackHigh);
    static Bitboard90 blockedPieces = new Bitboard90(blockedLow, blockedHigh);
    static Bitboard90 throne = new Bitboard90((1L << 44), 0L);


    static void main() {
        Bitboard90.printBBToConsole(white);
        System.out.println();
        Bitboard90.printBBToConsole(whiteKing);
        System.out.println();
        Bitboard90.printBBToConsole(black);
        System.out.println();
        Bitboard90.printBBToConsole(blockedPieces);
        System.out.println();
        Bitboard90.printBBToConsole(throne);


    }



    //bestimmt der Art des Steins an einer gegebenen Position
    //basierend auf dem Enum Pieces mit den Möglichkeiten KING, WHITE, BLACK, BLOCKED
    static Piece getPieceAt(int pos){
        if (Bitboard90.getBit(whiteKing, pos)) return Piece.KING;
        if (Bitboard90.getBit(white, pos)) return Piece.WHITE;
        if (Bitboard90.getBit(black, pos)) return Piece.BLACK;
        if (Bitboard90.getBit(blockedPieces, pos)) return Piece.BLOCKED;
        return Piece.EMPTY;
    }


    //die Züge ausführen, also den alten Stein löschen und einen neuen an der neuen Position einfügen
    public void applyMove (Move move){
        if (move.movedPiece == Piece.KING){
            Bitboard90.removeBit(whiteKing, move.from);
            Bitboard90.setBit(whiteKing, move.to);

            return;
        }
        if (move.movedPiece == Piece.WHITE){
            Bitboard90.removeBit(white, move.from);
            Bitboard90.setBit(white, move.to);
            return;
        }
        if (move.movedPiece == Piece.BLACK){
            Bitboard90.removeBit(black, move.from);
            Bitboard90.setBit(black, move.to);
            return;
        }
    }

    public boolean checkHit (Move move){
        int pos = move.to;

        //überprüfung der Standardsituation zum schlagen
        //entweder rechts und linkt oder oben und unten vom Gegner
        if (move.movedPiece == Piece.WHITE) {
            if ((getPieceAt(pos - 1) == Piece.BLACK) && (getPieceAt(pos - 2) == Piece.WHITE)) {
                Bitboard90.removeBit(black, pos - 1);
                return true;
            }

            if ((getPieceAt(pos + 1) == Piece.BLACK) && (getPieceAt(pos + 2) == Piece.WHITE)) {
                Bitboard90.removeBit(black, pos + 1);
                return true;
            }
            if ((getPieceAt(pos - 10) == Piece.BLACK) && (getPieceAt(pos - 20) == Piece.WHITE)) {
                Bitboard90.removeBit(black, pos - 10);
                return true;
            }
            if ((getPieceAt(pos + 10) == Piece.BLACK) && (getPieceAt(pos + 10) == Piece.WHITE)) {
                Bitboard90.removeBit(black, pos + 10);
                return true;
            }
        } else if (move.movedPiece == Piece.BLACK) {
            if ((getPieceAt(pos - 1) == Piece.WHITE) && (getPieceAt(pos - 2) == Piece.BLACK)) {
                Bitboard90.removeBit(white, pos - 1);
                return true;
            }
            if ((getPieceAt(pos + 1) == Piece.WHITE) && (getPieceAt(pos + 2) == Piece.BLACK)) {
                Bitboard90.removeBit(white, pos + 1);
                return true;
            }
            if ((getPieceAt(pos - 10) == Piece.WHITE) && (getPieceAt(pos - 20) == Piece.BLACK)) {
                Bitboard90.removeBit(white, pos - 10);
                return true;
            }
            if ((getPieceAt(pos + 10) == Piece.WHITE) && (getPieceAt(pos + 10) == Piece.BLACK)) {
                Bitboard90.removeBit(white, pos + 10);
                return true;
            }
        }


        // hier muss später noch die Logik zum Überprüfen, ob ein Stein am rand geschlagen wird eingefügt werden
        // Logik zum Überprüfen, ob der König geschlagen wird

        return false;
    }


    //TODO:
    // Methode zum Überprüfen des Spielendes
    // Methode die möglichen Züge zu generieren

}
