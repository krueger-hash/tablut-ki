package de.tuberlin.tablut.ai.ZobristHasher;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.Hit;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.Piece;
import de.tuberlin.tablut.ai.Player;

import java.util.List;
import java.util.Random;

public class TablutZobristHasher {
    private static final int BOARD_SIZE = 9;

    private static final int ROWS = 9;
    private static final int COLS = 10;
    private static final int ENCODED_SQUARE_COUNT = ROWS * COLS; // 90

    private final long[][] pieceSquareKeys;
    private final long blackToMoveKey;

    private static final long DEFAULT_SEED = 123456789L;

    /* TODO's:
        - should cache be managed by callers? e.g. 50-move rule and 1-move repetition limit
     */

    // Default constructor, initializes a new tablut zobrist hasher with default seed
    public TablutZobristHasher(){
        this(DEFAULT_SEED);
    }

    public TablutZobristHasher(long seed) {
        Random random = new Random(seed);

        Piece[] pieces = Piece.values();
        pieceSquareKeys = new long[pieces.length][ENCODED_SQUARE_COUNT];

        for (Piece piece : pieces) {
            if (piece == Piece.EMPTY || piece == Piece.BLOCKED || piece == Piece.THRONE) {
                continue;
            }

            for (int square = 0; square < ENCODED_SQUARE_COUNT; square++) {
                pieceSquareKeys[piece.ordinal()][square] = random.nextLong();
            }
        }

        blackToMoveKey = random.nextLong();
    }

    // Hashes a complete board position - should be ideally only at the start of a game
    public long hashPosition(Board board){
        long hash = 0L;
        // Map over actual playable board fields (9*9 = 81 playable fields)
        for(int row = 0; row < ROWS; row++){
            for(int col = 0; col < BOARD_SIZE; col++){
                // Only select positions that are playable
                int pos = row*COLS+col;
                Piece piece = board.getPieceAt(pos);

                if(piece == Piece.WHITE || piece == Piece.BLACK || piece == Piece.KING){
                    hash ^= pieceSquareKeys[piece.ordinal()][pos];
                }
            }
        }
        if(board.sideToMove == Player.BLACK){
            hash ^= blackToMoveKey;
        }
        return hash;
    }

    // Update hash position with move and hits - should be called after each move
    public long updateHashPosition(long currentHash, Move move, List<Hit> hits) {
        long hash = currentHash;
        Piece movedPiece = move.getMovedPiece();

        hash ^= pieceSquareKeys[movedPiece.ordinal()][move.getFrom()];
        hash ^= pieceSquareKeys[movedPiece.ordinal()][move.getTo()];

        if (hits != null) {
            for (Hit hit : hits) {
                Piece hitPiece = hit.piece();
                if (hitPiece == Piece.WHITE || hitPiece == Piece.BLACK || hitPiece == Piece.KING) {
                    hash ^= pieceSquareKeys[hitPiece.ordinal()][hit.position()];
                }
            }
        }

        return hash ^ blackToMoveKey;
    }
}
