package de.tuberlin.tablut.ai;

public final class BoardEvaluator {
    public static final Player MAX_PLAYER = Player.BLACK;
    public static final Player MIN_PLAYER = Player.WHITE;

    private static final int WIN_SCORE = 100_000;
    private static final int MATERIAL_WEIGHT = 100;
    private static final int KING_DISTANCE_WEIGHT = 30;
    private static final int KING_OPEN_ESCAPE_WEIGHT = 2_500;
    private static final int KING_PRESSURE_WEIGHT = 600;
    private static final int KING_DANGER_WEIGHT = 1500;
    private static final int MOBILITY_WEIGHT = 4;
    private static final int TWO_OPEN_CORNERS_FOR_KING = 10_000;
    public static int HISTORY_HEURISTIC_WEIGHT = 1;

    /*
    Aktuell ist Wertebereich für Score ohne Victory [-4676,4364];
    ALPHA und BETA INIT müssen entsprechend angepasst werden, damit keine hohen Siege abgeschnitten werden;
    Zahlen erstmal großzügig gewählt
     */
    public static final int ASSUME_BLACK_VICTORY_SCORE = 80_000;
    public static final int ASSUME_WHITE_VICTORY_SCORE = -ASSUME_BLACK_VICTORY_SCORE;
    public static final int ALPHA_INIT = -1_000_000;
    public static final int BETA_INIT = 1_000_000;


    private static final int[] ESCAPE_SQUARES = {0, 8, 80, 88};
    private static final int[] DIRECTIONS = {-Bitboard90.cols, Bitboard90.cols, -1, 1};

    // Max_player maximizes the value, Min player minimizes the value
    public static int evaluate(Board board){

        if (board == null) {
            throw new IllegalArgumentException("board must not be null");
        }
//        /////////////////////////////////////////////////////7
//        /// Terminale Scores (Material, Mobility)
//        //////////////////////////////////////////////////////
//        // Stalemate ist terminal, da die KI sich das trotzdem erarbeiten muss
//        if(board.isStalemate()){
//            return 0;
//        }
//        // Siegbedingungen additiv, damit die KI bei erkannter Niederlage trotzdem weiterhin die besten Züge probiert
//        if(board.hasBlackWon()){
//            return WIN_SCORE;
//        }
//        if(board.hasWhiteWon()){
//            return -WIN_SCORE;
//        }

        // Returns the overall score of the current board. Max player maximizes, min player minimizes
        return boardScore(board);
    }

    private static int boardScore(Board board) {
        int score = 0;
        /////////////////////////////////////////////////////7
        /// Terminale Scores (Material, Mobility)
        //////////////////////////////////////////////////////
        // Stalemate ist terminal, da die KI sich das trotzdem erarbeiten muss
//        if(board.isStalemate()){
//            return 0;
//        }
        // Siegbedingungen additiv, damit die KI bei erkannter Niederlage trotzdem weiterhin die besten Züge probiert
        if(board.hasBlackWon()){
            score += WIN_SCORE;
        }
        if(board.hasWhiteWon()){
            score -= WIN_SCORE;
        }



        /////////////////////////////////////////////////////7
        /// Allgemeine Scores (Material, Mobility)
        //////////////////////////////////////////////////////
        // * Material - White pieces are weighted higher because there are fewer defenders.
        int blackCount = board.black.bitCount(); // 0 - 16
        int whiteCount = board.white.bitCount(); // 0 - 8
        score += MATERIAL_WEIGHT * (blackCount - 2 * whiteCount);

        // * Mobility - Black wants many options and wants to restrict white.
//        int blackMoves = Board.generateLegalMoves(board, Player.BLACK).size();
//        int whiteMoves = Board.generateLegalMoves(board, Player.WHITE).size();
//        score += MOBILITY_WEIGHT * (blackMoves - whiteMoves);


        /////////////////////////////////////////////////////7
        /// Scores abhängig vom König
        //////////////////////////////////////////////////////
        //Position des Königs finden für folgende Berechnungen
        int kingPosition = findKingPosition(board);
        //Wenn kein König mehr auf Feld ist (Schwarz hat gewonnen), dann folgende Bewertungen überspringen
        if (kingPosition == -1) {
            return score;
        }

        // * King distance to escape squares. Larger distance is better for black.
        score += KING_DISTANCE_WEIGHT * minDistanceToEscape(kingPosition);

        // * Free escape lines are very dangerous for black.
        int openLines = countOpenEscapeLines(board, kingPosition);
        if (openLines <= 1) {score -= KING_OPEN_ESCAPE_WEIGHT * openLines;}
        else {score -= TWO_OPEN_CORNERS_FOR_KING;} // wenn es 2 offene Linien nach einem weißen Zug gibt, hat Weiß im Folgezug gewonnen, aber das Spiel ist nicht vorbei; WIN_SCORE/2 ist verhindert, dass Suche nicht bis Terminalknoten fortgesetzt wird, der relevante Zug, aber auf jeden Fall gewinnt

        // * Direct pressure around the king is valuable for black.
        // Bewertung so, dass bei maximaler Bedrohung (nur 1 Piece fehlt zum schlagen) die volle DANGER-WEIGHT angewendet wird; dazwischen gleichförmig
        int hostileSides = countHostileSidesAroundKing(board, kingPosition);
        if(kingIsOnThrone(kingPosition)){
            score += KING_DANGER_WEIGHT * hostileSides/3; //
        }
        else if (kingIsNextToThrone(kingPosition)) {
            score += KING_DANGER_WEIGHT * hostileSides/2;
        }
        else {score += hostileSides* KING_DANGER_WEIGHT;}

        return score;
    }

    //Gibt Index der Position des Königs aus Spanne 0-89
    public static int findKingPosition(Board board) {
        if (board.whiteKing.low != 0) {
            return Long.numberOfTrailingZeros(board.whiteKing.low);
        }
        if (board.whiteKing.high != 0) {
            return 64 + Long.numberOfTrailingZeros(board.whiteKing.high);
        }
        return -1;
    }

    // Returns the minimum Manhattan distance to the nearest escape square.
    private static int minDistanceToEscape(int position) {
        // Set max value
        int minDistance = Integer.MAX_VALUE;
        int row = position / Bitboard90.cols;
        int col = position % Bitboard90.cols;

        // Use nearest escape square
        for (int escapeSquare : ESCAPE_SQUARES) {
            int escapeRow = escapeSquare / Bitboard90.cols;
            int escapeCol = escapeSquare % Bitboard90.cols;
            // Manhattan distance
            int distance = Math.abs(row - escapeRow) + Math.abs(col - escapeCol);
            // If distance is closer, replace minDistance
            minDistance = Math.min(minDistance, distance);
        }

        return minDistance;
    }

    // Returns the number of open escape lines around the king towards escape squares.
    private static int countOpenEscapeLines(Board board, int kingPosition) {
        int openLines = 0;
        for (int escapeSquare : ESCAPE_SQUARES) {
            if (kingPosition == escapeSquare) {
                continue;
            }
            if (sameRowOrColumn(kingPosition, escapeSquare) && isPathClear(board, kingPosition, escapeSquare)) {
                openLines++;
            }
        }
        return openLines;
    }

    // Returns true if a and b are in the same row or same column
    private static boolean sameRowOrColumn(int a, int b) {
        return a / Bitboard90.cols == b / Bitboard90.cols || a % Bitboard90.cols == b % Bitboard90.cols;
    }

    // Returns true if all fields between from and to are empty
    private static boolean isPathClear(Board board, int from, int to) {
        int step;
        // If from and to in the same column
        if (from / Bitboard90.cols == to / Bitboard90.cols) {
            step = from < to ? 1 : -1;
        } else {
            // If from and to in the same row (because of sameRowOrColumn check)
            step = from < to ? Bitboard90.cols : -Bitboard90.cols;
        }

        // Go through all fields in the direction "step" towards "to"
        for (int position = from + step; position != to; position += step) {
            if (board.getPieceAt(position) != Piece.EMPTY) {
                return false;
            }
        }
        return true;
    }

    // Diese Implementierung sind sehr stark abhängig von der detaillierten Implementierung des Bitboards und des Boards, wenn bspw. die Zeilen/Spalten im Bitboard geändert werden, stimmt hier gar nichts mehr. darüber hinaus sehr anfällig für Flüchtigkeitsfehler bei den Zahlen
    private static boolean kingIsOnThrone(int kingPosition){
        return kingPosition == 44;
    }
    private static boolean kingIsNextToThrone(int kingPosition){
        return (kingPosition == 43 || kingPosition == 45 || kingPosition == 34 || kingPosition == 54);
    }

    // Returns the number of hostile squares around the king
    private static int countHostileSidesAroundKing(Board board, int kingPosition) {
        int hostileSides = 0;
        // Map through all kind adjacent fields n, s, w, e
        for (int direction : DIRECTIONS) {
            int position = kingPosition + direction;
            // Check if position is in the game
            if (!isPlayablePosition(position)) {
                continue;
            }
            // Check if position is hostile to the king
            if (isHostileToKing(board, position)) {
                hostileSides++;
            }
        }
        return hostileSides;
    }

    // Returns true if king is in immediate danger (potentially one move before game-over)
    private static boolean isKingInImmediateDanger(int kingPosition, int hostileSides) {
        // If king is on the throne, he is in danger if surrounded by 3 or more hostile squares
        if (kingPosition == 44) {
            return hostileSides >= 3;
        }
        // If kind is next to the throne, he is in danger if surrounded by 2 or more hostile squares
        if (kingPosition == 34 || kingPosition == 43 || kingPosition == 45 || kingPosition == 54) {
            return hostileSides >= 2;
        }
        // Otherwise, king is in danger if surrounded by 1 or more hostile squares
        return hostileSides >= 1;
    }

    // Returns true if position is hostile to the king, e.g. Back, Blocked, Throne
    private static boolean isHostileToKing(Board board, int position) {
        Piece piece = board.getPieceAt(position);
        return piece == Piece.BLACK || piece == Piece.THRONE || piece == Piece.BLOCKED;
    }

    // Returns if position is playable - not in the separation layer or outside the game
    private static boolean isPlayablePosition(int position) {
        return position >= 0
                && position < Bitboard90.rows * Bitboard90.cols
                && position % Bitboard90.cols < Bitboard90.cols - 1;
    }
}
