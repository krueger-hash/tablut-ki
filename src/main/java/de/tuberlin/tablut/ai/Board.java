package de.tuberlin.tablut.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {

    public long whiteLow = (1L << 24) | (1L << 34) | (1L << 42) | (1L << 43) | (1L << 45) | (1L << 46) | (1L << 54);
    public long whiteHigh = (1L);

    public long whiteKingLow = 1L << 44;
    public long whiteKingHigh = 0L;

    public long blackLow = (1L << 3) | (1L << 4) | (1L << 5) | (1L << 14) | (1L << 30) | (1L << 38) | (1L << 40) |
            (1L << 41) | (1L << 47) | (1L << 48) | (1L << 50) | (1L << 58);
    public long blackHigh = (1L << 10) | (1L << 19) | (1L << 20) | (1L << 21);

    public long blockedLow = (1L << 0) | (1L << 8);
    public long blockedHigh = (1L << 16) | (1L << 24);

    public Bitboard90 white;
    public Bitboard90 whiteKing;
    public Bitboard90 black;
    public Bitboard90 blockedPieces;
    public Bitboard90 throne;

    private static final int STALEMATE_NO_CAPTURE_LIMIT = 50;
    private static final int STALEMATE_REPETITION_LIMIT = 3;

    // In Tablut black (attackers) starts.
    private Player sideToMove = Player.BLACK;
    private int movesWithoutCapture = 0;
    private boolean stalemateTrackingInitialized = false;
    private final Map<PositionKey, Integer> positionCounts = new HashMap<>();

    private record PositionKey(
            long whiteLow,
            long whiteHigh,
            long whiteKingLow,
            long whiteKingHigh,
            long blackLow,
            long blackHigh,
            long blockedLow,
            long blockedHigh,
            long throneLow,
            long throneHigh,
            Player sideToMove
    ) {
    }

    //Konstruktoren:
    //Startaufstellung:
    public Board() {
        this.white = new Bitboard90(whiteLow, whiteHigh);
        this.whiteKing = new Bitboard90(whiteKingLow, whiteKingHigh);
        this.black = new Bitboard90(blackLow, blackHigh);
        this.blockedPieces = new Bitboard90(blockedLow, blockedHigh);
        this.throne = new Bitboard90(1L << 44, 0L);
        resetStalemateTracking();
    }

    public Board(Bitboard90 white,
                 Bitboard90 whiteKing,
                 Bitboard90 black,
                 Bitboard90 blockedPieces,
                 Bitboard90 throne) {
        // Set the side to move to black
        this(white, whiteKing, black, blockedPieces, throne, Player.BLACK);
    }

    //Beliebige Aufstellungen:
    public Board(Bitboard90 white,
                 Bitboard90 whiteKing,
                 Bitboard90 black,
                 Bitboard90 blockedPieces,
                 Bitboard90 throne,
                 Player sideToMove) {
        this.sideToMove = sideToMove;
        this.white = new Bitboard90(white.low, white.high);
        this.whiteKing = new Bitboard90(whiteKing.low, whiteKing.high);
        this.black = new Bitboard90(black.low, black.high);
        this.blockedPieces = new Bitboard90(blockedPieces.low, blockedPieces.high);
        this.throne = new Bitboard90(throne.low, throne.high);
        resetStalemateTracking();
    }

    void main() {
        Bitboard90.printBBToConsole(white);
        System.out.println();
        Bitboard90.printBBToConsole(whiteKing);
        System.out.println();
        Bitboard90.printBBToConsole(black);
        System.out.println();
        Bitboard90.printBBToConsole(blockedPieces);
        System.out.println();
        Bitboard90.printBBToConsole(throne);
        printBoard();
    }

    // This method creates a new deep copy of a given board
    public static Board deepCopy(Board board){
        Board copy = new Board(
                new Bitboard90(board.white.low, board.white.high),
                new Bitboard90(board.whiteKing.low, board.whiteKing.high),
                new Bitboard90(board.black.low, board.black.high),
                new Bitboard90(board.blockedPieces.low, board.blockedPieces.high),
                new Bitboard90(board.throne.low, board.throne.high),
                board.sideToMove
        );
        copy.movesWithoutCapture = board.movesWithoutCapture;
        copy.stalemateTrackingInitialized = board.stalemateTrackingInitialized;
        copy.positionCounts.putAll(board.positionCounts);
        return copy;
    }


    //bestimmt der Art des Steins an einer gegebenen Position
    //basierend auf dem Enum Pieces mit den Möglichkeiten KING, WHITE, BLACK, BLOCKED
    public Piece getPieceAt(int pos) {
        if (Bitboard90.getBit(whiteKing, pos)) return Piece.KING;
        if (Bitboard90.getBit(white, pos)) return Piece.WHITE;
        if (Bitboard90.getBit(black, pos)) return Piece.BLACK;
        if (Bitboard90.getBit(blockedPieces, pos)) return Piece.BLOCKED;
        if (Bitboard90.getBit(throne, pos)) return Piece.THRONE;

        return Piece.EMPTY;
    }


    //die Züge ausführen, also den alten Stein löschen und einen neuen an der neuen Position einfügen
    public void applyMove(Move move) {
        if (move.movedPiece == Piece.KING) {
            if (getPieceAt(move.from) == Piece.KING && getPieceAt(move.to) == Piece.EMPTY) {
                Bitboard90.removeBit(whiteKing, move.from);
                Bitboard90.setBit(whiteKing, move.to);
            }

            return;
        }
        if (move.movedPiece == Piece.WHITE && getPieceAt(move.from) == Piece.WHITE && getPieceAt(move.to) == Piece.EMPTY) {
            Bitboard90.removeBit(white, move.from);
            Bitboard90.setBit(white, move.to);
            return;
        }
        if (move.movedPiece == Piece.BLACK && getPieceAt(move.from) == Piece.BLACK && getPieceAt(move.to) == Piece.EMPTY) {
            Bitboard90.removeBit(black, move.from);
            Bitboard90.setBit(black, move.to);
            return;
        }
    }

    //gibt den geschlagenen Piece zurück
    //gibt EMPTY zurück, wenn keine Figur geschlagen werden kann
    public ArrayList<Piece> checkHit(Move move) {
        int pos = move.to;
        ArrayList<Piece> hitPiece = new ArrayList<Piece>();

        boolean kingOnThrone = getPieceAt(44) == Piece.KING;
        boolean throneEmpty = !kingOnThrone;

        //überprüfung der Standardsituation zum schlagen
        //entweder rechts und linkt oder oben und unten vom Gegner
        if (move.movedPiece == Piece.WHITE || move.movedPiece == Piece.KING) {

            if (getPieceAt(pos - 1) == Piece.BLACK &&
                    (getPieceAt(pos - 2) == Piece.WHITE || getPieceAt(pos - 2) == Piece.KING
                            || Bitboard90.getBit(blockedPieces, pos - 2)
                            || (Bitboard90.getBit(throne, pos - 2) && throneEmpty))) {
                Bitboard90.removeBit(black, pos - 1);
                hitPiece.add(Piece.BLACK);
            }

            if (getPieceAt(pos + 1) == Piece.BLACK &&
                    (getPieceAt(pos + 2) == Piece.WHITE || getPieceAt(pos + 2) == Piece.KING
                            || Bitboard90.getBit(blockedPieces, pos + 2)
                            || (Bitboard90.getBit(throne, pos + 2) && throneEmpty))) {
                Bitboard90.removeBit(black, pos + 1);
                hitPiece.add(Piece.BLACK);
            }

            if (getPieceAt(pos - 10) == Piece.BLACK &&
                    (getPieceAt(pos - 20) == Piece.WHITE || getPieceAt(pos - 20) == Piece.KING
                            || Bitboard90.getBit(blockedPieces, pos - 20)
                            || (Bitboard90.getBit(throne, pos - 20) && throneEmpty))) {
                Bitboard90.removeBit(black, pos - 10);
                hitPiece.add(Piece.BLACK);
            }

            if (getPieceAt(pos + 10) == Piece.BLACK &&
                    (getPieceAt(pos + 20) == Piece.WHITE || getPieceAt(pos + 20) == Piece.KING
                            || Bitboard90.getBit(blockedPieces, pos + 20)
                            || (Bitboard90.getBit(throne, pos + 20) && throneEmpty))) {
                Bitboard90.removeBit(black, pos + 10);
                hitPiece.add(Piece.BLACK);
            }

        } else if (move.movedPiece == Piece.BLACK) {

            //normale weiße Steine
            if (getPieceAt(pos - 1) == Piece.WHITE &&
                    (getPieceAt(pos - 2) == Piece.BLACK
                            || Bitboard90.getBit(blockedPieces, pos - 2)
                            || (Bitboard90.getBit(throne, pos - 2) && throneEmpty))) {
                Bitboard90.removeBit(white, pos - 1);
                hitPiece.add(Piece.WHITE);
            }

            if (getPieceAt(pos + 1) == Piece.WHITE &&
                    (getPieceAt(pos + 2) == Piece.BLACK
                            || Bitboard90.getBit(blockedPieces, pos + 2)
                            || (Bitboard90.getBit(throne, pos + 2) && throneEmpty))) {
                Bitboard90.removeBit(white, pos + 1);
                hitPiece.add(Piece.WHITE);
            }

            if (getPieceAt(pos - 10) == Piece.WHITE &&
                    (getPieceAt(pos - 20) == Piece.BLACK
                            || Bitboard90.getBit(blockedPieces, pos - 20)
                            || (Bitboard90.getBit(throne, pos - 20) && throneEmpty))) {
                Bitboard90.removeBit(white, pos - 10);
                hitPiece.add(Piece.WHITE);
            }

            if (getPieceAt(pos + 10) == Piece.WHITE &&
                    (getPieceAt(pos + 20) == Piece.BLACK
                            || Bitboard90.getBit(blockedPieces, pos + 20)
                            || (Bitboard90.getBit(throne, pos + 20) && throneEmpty))) {
                Bitboard90.removeBit(white, pos + 10);
                hitPiece.add(Piece.WHITE);
            }

            //König auf dem Thron geschlagen?
            if (getPieceAt(44) == Piece.KING
                    && getPieceAt(34) == Piece.BLACK
                    && getPieceAt(43) == Piece.BLACK
                    && getPieceAt(45) == Piece.BLACK
                    && getPieceAt(54) == Piece.BLACK) {
                Bitboard90.removeBit(whiteKing, 44);
                hitPiece.add(Piece.KING);
            }

            //König angrenzend zum Thron geschlagen?
            if ((getPieceAt(34) == Piece.KING && getPieceAt(33) == Piece.BLACK && getPieceAt(35) == Piece.BLACK && getPieceAt(24) == Piece.BLACK)
                    || (getPieceAt(43) == Piece.KING && getPieceAt(33) == Piece.BLACK && getPieceAt(42) == Piece.BLACK && getPieceAt(53) == Piece.BLACK)
                    || (getPieceAt(45) == Piece.KING && getPieceAt(35) == Piece.BLACK && getPieceAt(46) == Piece.BLACK && getPieceAt(55) == Piece.BLACK)
                    || (getPieceAt(54) == Piece.KING && getPieceAt(43) == Piece.BLACK && getPieceAt(53) == Piece.BLACK && getPieceAt(64) == Piece.BLACK)) {
                Bitboard90.removeBit(whiteKing, pos);
                hitPiece.add(Piece.KING);
            }

            //klassisches Schlagen des Königs zwischen 2 Steinen
            if (getPieceAt(pos - 1) == Piece.KING
                    && (getPieceAt(pos - 2) == Piece.BLACK
                    || Bitboard90.getBit(blockedPieces, pos - 2)
                    || (Bitboard90.getBit(throne, pos - 2) && throneEmpty))
                    && (pos - 1 != 34 && pos - 1 != 43 && pos - 1 != 44 && pos - 1 != 45 && pos - 1 != 54)) {
                Bitboard90.removeBit(whiteKing, pos - 1);
                hitPiece.add(Piece.KING);
            }

            if (getPieceAt(pos + 1) == Piece.KING
                    && (getPieceAt(pos + 2) == Piece.BLACK
                    || Bitboard90.getBit(blockedPieces, pos + 2)
                    || (Bitboard90.getBit(throne, pos + 2) && throneEmpty))
                    && (pos + 1 != 34 && pos + 1 != 43 && pos + 1 != 44 && pos + 1 != 45 && pos + 1 != 54)) {
                Bitboard90.removeBit(whiteKing, pos + 1);
                hitPiece.add(Piece.KING);
            }

            if (getPieceAt(pos - 10) == Piece.KING
                    && (getPieceAt(pos - 20) == Piece.BLACK
                    || Bitboard90.getBit(blockedPieces, pos - 20)
                    || (Bitboard90.getBit(throne, pos - 20) && throneEmpty))
                    && (pos - 10 != 34 && pos - 10 != 43 && pos - 10 != 44 && pos - 10 != 45 && pos - 10 != 54)) {
                Bitboard90.removeBit(whiteKing, pos - 10);
                hitPiece.add(Piece.KING);
            }

            if (getPieceAt(pos + 10) == Piece.KING
                    && (getPieceAt(pos + 20) == Piece.BLACK
                    || Bitboard90.getBit(blockedPieces, pos + 20)
                    || (Bitboard90.getBit(throne, pos + 20) && throneEmpty))
                    && (pos + 10 != 34 && pos + 10 != 43 && pos + 10 != 44 && pos + 10 != 45 && pos + 10 != 54)) {
                Bitboard90.removeBit(whiteKing, pos + 10);
                hitPiece.add(Piece.KING);
            }

            //normales Schlagen von Steinen
            if (getPieceAt(pos - 1) == Piece.WHITE && (getPieceAt(pos - 2) == Piece.BLACK)) {
                Bitboard90.removeBit(white, pos - 1);
                hitPiece.add(Piece.WHITE);
            }
            if (getPieceAt(pos + 1) == Piece.WHITE && (getPieceAt(pos + 2) == Piece.BLACK)) {
                Bitboard90.removeBit(white, pos + 1);
                hitPiece.add(Piece.WHITE);
            }
            if (getPieceAt(pos - 10) == Piece.WHITE && (getPieceAt(pos - 20) == Piece.BLACK)) {
                Bitboard90.removeBit(white, pos - 10);
                hitPiece.add(Piece.WHITE);
            }
            if (getPieceAt(pos + 10) == Piece.WHITE && (getPieceAt(pos + 20) == Piece.BLACK)) {
                Bitboard90.removeBit(white, pos + 10);
                hitPiece.add(Piece.WHITE);
            }
        }
        registerMoveForStalemate(move, hitPiece);
        return hitPiece;

    }


    // Prints labeled 9x9 board visualizing pieces and throne
    public  void printBoard() {
        System.out.println("    0 1 2 3 4 5 6 7 8");
        for (int row = 0; row < 9; row++) {
            StringBuilder line = new StringBuilder();
            line.append(row).append(" | ");
            for (int col = 0; col < 9; col++) {
                int pos = row * Bitboard90.cols + col;
                Piece piece = getPieceAt(pos);

                char symbol;
                if (piece == Piece.KING) {
                    symbol = 'K';
                } else if (piece == Piece.WHITE) {
                    symbol = 'W';
                } else if (piece == Piece.BLACK) {
                    symbol = 'B';
                } else if (piece == Piece.BLOCKED) {
                    symbol = 'X';
                } else if (Bitboard90.getBit(throne, pos)) {
                    symbol = 'T';
                } else {
                    symbol = '.';
                }

                line.append(symbol);
                if (col < 8) {
                    line.append(' ');
                }
            }
            line.append(" |");
            System.out.println(line);
        }
    }

    // Calculates all possible moves for a given bitboard and player type.
    // This uses current global board occupancy (white/black/king/blocked).
    public  Move[] generateLegalMoves(Bitboard90 board, Player player) {
        if (board == null) {
            throw new IllegalArgumentException("Board bitboard must not be null");
        }
        if (player == null) {
            throw new IllegalArgumentException("Player must be WHITE, BLACK or KING");
        }

        Bitboard90 occupied = Bitboard90.or(
                Bitboard90.or(white, whiteKing),
                Bitboard90.or(black, blockedPieces)
        );

        List<Move> moves = new ArrayList<>();
        // one row up, one row down, one column left, one column right (n,s,w,e)
        int[] directions = {-Bitboard90.cols, Bitboard90.cols, -1, 1};

        for (int row = 0; row < Bitboard90.rows; row++) {
            for (int col = 0; col < Bitboard90.cols - 1; col++) {
                int from = row * Bitboard90.cols + col;
                if (!Bitboard90.getBit(board, from)) {
                    continue;
                }

                Piece movedPiece = resolveMovedPiece(player, from);

                for (int direction : directions) {
                    int to = from + direction;
                    while (isPlayableSquare(to)) {
                        if (Bitboard90.getBit(occupied, to)) {
                            break;
                        }
                        moves.add(new Move(from, to, movedPiece));
                        to += direction;
                    }
                }
            }
        }

        return moves.toArray(new Move[0]);
    }

    // Checks if index points to a real board cell, not outside the board and not into the separator column of 9x10 bit layout
    private  boolean isPlayableSquare(int pos) {
        // reject out-of-range indices (<0 or >=90)
        if (pos < 0 || pos >= Bitboard90.rows * Bitboard90.cols) {
            return false;
        }
        // only valid playable columns 0 - 8, not 9 (separator bit)
        // e.g. 19 % 10 < 10 -1 => false (separator column)
        return (pos % Bitboard90.cols) < (Bitboard90.cols - 1);
    }

    private  Piece resolveMovedPiece(Player player, int from) {
        if (player == Player.BLACK) {
            return Piece.BLACK;
        }
        if (Bitboard90.getBit(whiteKing, from)) {
            return Piece.KING;
        }
        return Piece.WHITE;
    }

    //TODO:
    // Methode zum Überprüfen des Spielendes

     boolean hasBlackWon (){
        if(whiteKing.high + whiteKing.low == 0) {return true;} //Ist kein König mehr auf dem Board, sind beide vom Zahlenwert 0
        else {return false;}
    }

     boolean hasWhiteWon(){
        //Wenn König auf Eckfeld steht, ergibt die verANDung der beiden Bitboards ein nichtleeres Bitboard, d.h. es existiert ein gesetztes Bit
        int bitCount = Bitboard90.and(whiteKing, blockedPieces).bitCount();
        if (bitCount == 1) {
            return true;
        }
        else {return false;}
    }

    // Repetion and 50-move Rule probably should be counted by the main loop, not within the board class
    boolean isStalemate() {
        if (hasBlackWon() || hasWhiteWon()) {
            return false;
        }

        ensureStalemateTrackingInitialized();

        // *50 Zuege ohne geschlagene Figur;
        if (movesWithoutCapture >= STALEMATE_NO_CAPTURE_LIMIT) {
            return true;
        }

        // *wiederholte Stellung (Zyklenfreiheit),
        if (positionCounts.getOrDefault(currentPositionKey(), 0) >= STALEMATE_REPETITION_LIMIT) {
            return true;
        }

        // *kein Zug moeglich
        return hasNoLegalMovesForSideToMove();
    }

    public void resetStalemateTracking() {
        resetStalemateTracking(Player.BLACK);
    }

    public void resetStalemateTracking(Player sideToMove) {
        this.sideToMove = sideToMove;
        this.movesWithoutCapture = 0;
        this.stalemateTrackingInitialized = false;
        this.positionCounts.clear();
    }

    private void ensureStalemateTrackingInitialized() {
        if (stalemateTrackingInitialized) {
            return;
        }
        positionCounts.put(currentPositionKey(), 1);
        stalemateTrackingInitialized = true;
    }

    private void registerMoveForStalemate(Move move, List<Piece> hitPieces) {
        if (move == null) {
            return;
        }

        ensureStalemateTrackingInitialized();

        boolean captureOccurred = hitPieces != null && !hitPieces.isEmpty();
        movesWithoutCapture = captureOccurred ? 0 : movesWithoutCapture + 1;
        sideToMove = oppositeSide(move.movedPiece==Piece.BLACK?Player.BLACK:Player.WHITE);

        positionCounts.merge(currentPositionKey(), 1, Integer::sum);
    }

    private boolean hasNoLegalMovesForSideToMove() {
        if (sideToMove == Player.BLACK) {
            return generateLegalMoves(black, Player.BLACK).length == 0;
        }
        Bitboard90 whiteSide = Bitboard90.or(white, whiteKing);
        return generateLegalMoves(whiteSide, Player.WHITE).length == 0;
    }

    private PositionKey currentPositionKey() {
        return new PositionKey(
                white.low,
                white.high,
                whiteKing.low,
                whiteKing.high,
                black.low,
                black.high,
                blockedPieces.low,
                blockedPieces.high,
                throne.low,
                throne.high,
                sideToMove
        );
    }

    private Player oppositeSide(Player side) {
        return side == Player.BLACK ? Player.WHITE : Player.BLACK;
    }
}