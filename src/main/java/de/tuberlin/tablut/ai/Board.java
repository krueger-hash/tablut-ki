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

    public static final long BLOCKED_LOW = (1L << 0) | (1L << 8);
    public static final long BLOCKED_HIGH = (1L << 16) | (1L << 24);


    public Bitboard90 white;
    public Bitboard90 whiteKing;
    public Bitboard90 black;


    public static final Bitboard90 BLOCKED_PIECES = new Bitboard90(BLOCKED_LOW, BLOCKED_HIGH);
    public static final  Bitboard90 THRONE = new Bitboard90(1L << 44, 0L);
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
            Player sideToMove
    ) {
    }

    //Konstruktoren:
    //Startaufstellung:
    public Board() {
        this.white = new Bitboard90(whiteLow, whiteHigh);
        this.whiteKing = new Bitboard90(whiteKingLow, whiteKingHigh);
        this.black = new Bitboard90(blackLow, blackHigh);
        resetStalemateTracking();
    }

    public Board(Bitboard90 white,
                 Bitboard90 whiteKing,
                 Bitboard90 black) {
        // Set the side to move to black
        this(white, whiteKing, black, Player.BLACK);
    }

    //Beliebige Aufstellungen:
    public Board(Bitboard90 white,
                 Bitboard90 whiteKing,
                 Bitboard90 black,
                 Player sideToMove) {
        this.sideToMove = sideToMove;
        this.white = new Bitboard90(white.low, white.high);
        this.whiteKing = new Bitboard90(whiteKing.low, whiteKing.high);
        this.black = new Bitboard90(black.low, black.high);
        resetStalemateTracking();
    }

    void main() {
        Bitboard90.printBBToConsole(white);
        System.out.println();
        Bitboard90.printBBToConsole(whiteKing);
        System.out.println();
        Bitboard90.printBBToConsole(black);
        System.out.println();
        Bitboard90.printBBToConsole(BLOCKED_PIECES);
        System.out.println();
        Bitboard90.printBBToConsole(THRONE);
        printBoard();
    }

    // This method creates a new deep copy of a given board
    public static Board deepCopy(Board board){
        Board copy = new Board(
                new Bitboard90(board.white.low, board.white.high),
                new Bitboard90(board.whiteKing.low, board.whiteKing.high),
                new Bitboard90(board.black.low, board.black.high),
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
        if (Bitboard90.getBit(BLOCKED_PIECES, pos)) return Piece.BLOCKED;
        if (Bitboard90.getBit(THRONE, pos)) return Piece.THRONE;

        return Piece.EMPTY;
    }


    //die Züge ausführen, also den alten Stein löschen und einen neuen an der neuen Position einfügen
    public void applyMove(Move move, ArrayList<Hit> hits) {
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
        hit(hits);
    }

    public void hit(ArrayList<Hit> hits){
        if (hits == null) return;
        for (Hit h : hits) {
            if (h.piece() == Piece.EMPTY || h.piece() == Piece.THRONE) continue;
            if (h.piece() == Piece.BLACK) {
                Bitboard90.removeBit(black, h.position());
            }
            if (h.piece() == Piece.KING) {
                Bitboard90.removeBit(whiteKing, h.position());
            }
            if (h.piece() == Piece.WHITE) {
                Bitboard90.removeBit(white, h.position());
            }
        }
    }

    //gibt den geschlagenen Piece zurück
    //gibt EMPTY zurück, wenn keine Figur geschlagen werden kann
    public ArrayList<Hit> checkHit(Move move) {
        int pos = move.to;

        ArrayList<Hit> hits = new ArrayList<>();        // Liste mit Hit(Piece, position)
        ArrayList<Piece> hitPieces = new ArrayList<>(); // Liste NUR mit Pieces

        boolean kingOnThrone = getPieceAt(44) == Piece.KING;
        boolean throneEmpty = !kingOnThrone;

        // WEISS oder KÖNIG schlägt SCHWARZ
        if (move.movedPiece == Piece.WHITE || move.movedPiece == Piece.KING) {

            // LINKS
            if (getPieceAt(pos - 1) == Piece.BLACK &&
                    (getPieceAt(pos - 2) == Piece.WHITE || getPieceAt(pos - 2) == Piece.KING
                            || Bitboard90.getBit(BLOCKED_PIECES, pos - 2)
                            || (Bitboard90.getBit(THRONE, pos - 2) && throneEmpty))) {

                hits.add(new Hit(Piece.BLACK, pos - 1));
                hitPieces.add(Piece.BLACK);
            }

            // RECHTS
            if (getPieceAt(pos + 1) == Piece.BLACK &&
                    (getPieceAt(pos + 2) == Piece.WHITE || getPieceAt(pos + 2) == Piece.KING
                            || Bitboard90.getBit(BLOCKED_PIECES, pos + 2)
                            || (Bitboard90.getBit(THRONE, pos + 2) && throneEmpty))) {

                hits.add(new Hit(Piece.BLACK, pos + 1));
                hitPieces.add(Piece.BLACK);
            }

            // OBEN
            if (getPieceAt(pos - 10) == Piece.BLACK &&
                    (getPieceAt(pos - 20) == Piece.WHITE || getPieceAt(pos - 20) == Piece.KING
                            || Bitboard90.getBit(BLOCKED_PIECES, pos - 20)
                            || (Bitboard90.getBit(THRONE, pos - 20) && throneEmpty))) {

                hits.add(new Hit(Piece.BLACK, pos - 10));
                hitPieces.add(Piece.BLACK);
            }

            // UNTEN
            if (getPieceAt(pos + 10) == Piece.BLACK &&
                    (getPieceAt(pos + 20) == Piece.WHITE || getPieceAt(pos + 20) == Piece.KING
                            || Bitboard90.getBit(BLOCKED_PIECES, pos + 20)
                            || (Bitboard90.getBit(THRONE, pos + 20) && throneEmpty))) {

                hits.add(new Hit(Piece.BLACK, pos + 10));
                hitPieces.add(Piece.BLACK);
            }
        }

        // SCHWARZ schlägt WEISS oder KÖNIG
        else if (move.movedPiece == Piece.BLACK) {

            // LINKS
            if (getPieceAt(pos - 1) == Piece.WHITE &&
                    (getPieceAt(pos - 2) == Piece.BLACK
                            || Bitboard90.getBit(BLOCKED_PIECES, pos - 2)
                            || (Bitboard90.getBit(THRONE, pos - 2) && throneEmpty))) {

                hits.add(new Hit(Piece.WHITE, pos - 1));
                hitPieces.add(Piece.WHITE);
            }

            // RECHTS
            if (getPieceAt(pos + 1) == Piece.WHITE &&
                    (getPieceAt(pos + 2) == Piece.BLACK
                            || Bitboard90.getBit(BLOCKED_PIECES, pos + 2)
                            || (Bitboard90.getBit(THRONE, pos + 2) && throneEmpty))) {

                hits.add(new Hit(Piece.WHITE, pos + 1));
                hitPieces.add(Piece.WHITE);
            }

            // OBEN
            if (getPieceAt(pos - 10) == Piece.WHITE &&
                    (getPieceAt(pos - 20) == Piece.BLACK
                            || Bitboard90.getBit(BLOCKED_PIECES, pos - 20)
                            || (Bitboard90.getBit(THRONE, pos - 20) && throneEmpty))) {

                hits.add(new Hit(Piece.WHITE, pos - 10));
                hitPieces.add(Piece.WHITE);
            }

            // UNTEN
            if (getPieceAt(pos + 10) == Piece.WHITE &&
                    (getPieceAt(pos + 20) == Piece.BLACK
                            || Bitboard90.getBit(BLOCKED_PIECES, pos + 20)
                            || (Bitboard90.getBit(THRONE, pos + 20) && throneEmpty))) {

                hits.add(new Hit(Piece.WHITE, pos + 10));
                hitPieces.add(Piece.WHITE);
            }

            // König auf dem Thron
            if (getPieceAt(44) == Piece.KING
                    && getPieceAt(34) == Piece.BLACK
                    && getPieceAt(43) == Piece.BLACK
                    && getPieceAt(45) == Piece.BLACK
                    && getPieceAt(54) == Piece.BLACK) {

                hits.add(new Hit(Piece.KING, 44));
                hitPieces.add(Piece.KING);
            }

            // König angrenzend zum Thron
            if ((getPieceAt(34) == Piece.KING && getPieceAt(33) == Piece.BLACK && getPieceAt(35) == Piece.BLACK && getPieceAt(24) == Piece.BLACK)
                    || (getPieceAt(43) == Piece.KING && getPieceAt(33) == Piece.BLACK && getPieceAt(42) == Piece.BLACK && getPieceAt(53) == Piece.BLACK)
                    || (getPieceAt(45) == Piece.KING && getPieceAt(35) == Piece.BLACK && getPieceAt(46) == Piece.BLACK && getPieceAt(55) == Piece.BLACK)
                    || (getPieceAt(54) == Piece.KING && getPieceAt(43) == Piece.BLACK && getPieceAt(53) == Piece.BLACK && getPieceAt(64) == Piece.BLACK)) {

                hits.add(new Hit(Piece.KING, pos));
                hitPieces.add(Piece.KING);
            }

            // König wie normaler Stein
            if (getPieceAt(pos - 1) == Piece.KING
                    && (getPieceAt(pos - 2) == Piece.BLACK
                    || Bitboard90.getBit(BLOCKED_PIECES, pos - 2)
                    || (Bitboard90.getBit(THRONE, pos - 2) && throneEmpty))
                    && (pos - 1 != 34 && pos - 1 != 43 && pos - 1 != 44 && pos - 1 != 45 && pos - 1 != 54)) {

                hits.add(new Hit(Piece.KING, pos - 1));
                hitPieces.add(Piece.KING);
            }

            if (getPieceAt(pos + 1) == Piece.KING
                    && (getPieceAt(pos + 2) == Piece.BLACK
                    || Bitboard90.getBit(BLOCKED_PIECES, pos + 2)
                    || (Bitboard90.getBit(THRONE, pos + 2) && throneEmpty))
                    && (pos + 1 != 34 && pos + 1 != 43 && pos + 1 != 44 && pos + 1 != 45 && pos + 1 != 54)) {

                hits.add(new Hit(Piece.KING, pos + 1));
                hitPieces.add(Piece.KING);
            }

            if (getPieceAt(pos - 10) == Piece.KING
                    && (getPieceAt(pos - 20) == Piece.BLACK
                    || Bitboard90.getBit(BLOCKED_PIECES, pos - 20)
                    || (Bitboard90.getBit(THRONE, pos - 20) && throneEmpty))
                    && (pos - 10 != 34 && pos - 10 != 43 && pos - 10 != 44 && pos - 10 != 45 && pos - 10 != 54)) {

                hits.add(new Hit(Piece.KING, pos - 10));
                hitPieces.add(Piece.KING);
            }

            if (getPieceAt(pos + 10) == Piece.KING
                    && (getPieceAt(pos + 20) == Piece.BLACK
                    || Bitboard90.getBit(BLOCKED_PIECES, pos + 20)
                    || (Bitboard90.getBit(THRONE, pos + 20) && throneEmpty))
                    && (pos + 10 != 34 && pos + 10 != 43 && pos + 10 != 44 && pos + 10 != 45 && pos + 10 != 54)) {

                hits.add(new Hit(Piece.KING, pos + 10));
                hitPieces.add(Piece.KING);
            }
        }

        // wichtig: beide Listen weitergeben
        registerMoveForStalemate(move, hitPieces);

        return hits;
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
                } else if (Bitboard90.getBit(THRONE, pos)) {
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
    public static ArrayList<Move> generateLegalMoves(Board board, Player player) {
        if (board == null) {
            throw new IllegalArgumentException("Board bitboard must not be null");
        }
        if (player == null) {
            throw new IllegalArgumentException("Player must be WHITE, BLACK or KING");
        }

        Bitboard90 occupied = Bitboard90.or(
                Bitboard90.or(board.white, board.whiteKing),
                Bitboard90.or(board.black, BLOCKED_PIECES)
        );

        ArrayList<Move> moves = new ArrayList<>();
        // one row up, one row down, one column left, one column right (n,s,w,e)
        int[] directions = {-Bitboard90.cols, Bitboard90.cols, -1, 1};

        for (int row = 0; row < Bitboard90.rows; row++) {
            for (int col = 0; col < Bitboard90.cols - 1; col++) {
                int from = row * Bitboard90.cols + col;
                // Select target bitboard based on player
                // If a piece from target player is found go further, otherwise continue to the next loop
                if (!belongsToPlayer(board, player, from)) {
                    continue;
                }

                // Get the piece type of current position: BLACK; WHITE; WHITE_KING
                Piece movedPiece = resolveMovedPiece(board, player, from);

                for (int direction : directions) {
                    int to = from + direction;
                    while (isLegalMoveTarget(from, to, direction)) {
                        if (Bitboard90.getBit(occupied, to)) {
                            break;
                        }
                        moves.add(new Move(from, to, movedPiece));
                        to += direction;
                    }
                }
            }
        }
        return moves;
    }

    private static boolean belongsToPlayer(Board board, Player player, int pos) {
        if (player == Player.BLACK) {
            return Bitboard90.getBit(board.black, pos);
        }
        return Bitboard90.getBit(board.white, pos) || Bitboard90.getBit(board.whiteKing, pos);
    }

    private static boolean isLegalMoveTarget(int from, int to, int direction) {
        // Reject indices outside the 9x10 encoded board.
        if (to < 0 || to >= Bitboard90.rows * Bitboard90.cols) {
            return false;
        }

        // Reject the separator column in the 9x10 layout.
        if ((to % Bitboard90.cols) >= (Bitboard90.cols - 1)) {
            return false;
        }

        // Horizontal moves must stay in the same encoded row.
        if (direction == -1 || direction == 1) {
            return (from / Bitboard90.cols) == (to / Bitboard90.cols);
        }

        return true;
    }

    private static Piece resolveMovedPiece(Board board, Player player, int from) {
        if (player == Player.BLACK) {
            return Piece.BLACK;
        }
        if (Bitboard90.getBit(board.whiteKing, from)) {
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
        int bitCount = Bitboard90.and(whiteKing, BLOCKED_PIECES).bitCount();
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
            return generateLegalMoves(this, Player.BLACK).isEmpty();
        }
        Bitboard90 whiteSide = Bitboard90.or(white, whiteKing);
        return generateLegalMoves(this, Player.WHITE).isEmpty();
    }

    private PositionKey currentPositionKey() {
        return new PositionKey(
                white.low,
                white.high,
                whiteKing.low,
                whiteKing.high,
                black.low,
                black.high,
                sideToMove
        );
    }

    private Player oppositeSide(Player side) {
        return side == Player.BLACK ? Player.WHITE : Player.BLACK;
    }
}
