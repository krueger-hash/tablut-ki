package de.tuberlin.tablut.ai.SearchAlgorithms;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.BoardEvaluator;
import de.tuberlin.tablut.ai.Move;

import java.util.ArrayList;
import java.util.List;

import static de.tuberlin.tablut.ai.SearchAlgorithms.AlphaBeta.sortMoves;
import static de.tuberlin.tablut.ai.BoardEvaluator.MAX_PLAYER;
import static de.tuberlin.tablut.ai.BoardEvaluator.MIN_PLAYER;

public class PrincipalVariation {

    //2D-Array
    /*

    private static final int INF = 99999999;

    private final Move[][] pvTable;
    private final int[] pvLength;

    public PrincipalVariation(int maxDepth) {
        pvTable = new Move[maxDepth + 5][maxDepth + 5];
        pvLength = new int[maxDepth + 5];
    }

    public ABResult pvSearch(Board board, int depth, int alpha, int beta) {
        SearchContext ctx = new SearchContext();

        try {
            int score = pvs(board, depth, alpha, beta, ctx, 0);

            ArrayList<Move> pv = new ArrayList<>();
            for (int i = 0; i < pvLength[0]; i++) {
                pv.add(pvTable[0][i]);
            }

            return new ABResult(score, pv);

        } catch (SearchStoppedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int pvs(Board board, int depth, int alpha, int beta,
                    SearchContext ctx, int ply) throws SearchStoppedException {

        ctx.incrementPositions();
        if (ctx.shouldStop()) throw new SearchStoppedException("Zeitlimit erreicht");

        // Blatt / Terminal
        if (depth == 0 || board.gameIsEnd()) {
            ctx.incrementLeafs();
            pvLength[ply] = 0;
            int eval = BoardEvaluator.evaluate(board);
            // Negamax: Wert aus Sicht des Spielers am Zug
            if (board.sideToMove == MIN_PLAYER) eval = -eval;
            return eval;
        }

        ArrayList<Move> moves = Board.generateLegalMoves(board, board.sideToMove);
        sortMoves(board, moves);

        pvLength[ply] = 0;
        boolean firstMove = true;
        int bestScore = -INF;

        for (Move move : moves) {

            board.makeMove(move);
            int score;

            if (firstMove) {
                // voller Suchraum
                score = -pvs(board, depth - 1, -beta, -alpha, ctx, ply + 1);
                firstMove = false;
            } else {
                // Nullfenster
                score = -pvs(board, depth - 1, -alpha - 1, -alpha, ctx, ply + 1);

                // Fail-high → Re-Search mit vollem Fenster
                if (score > alpha && score < beta) {
                    score = -pvs(board, depth - 1, -beta, -alpha, ctx, ply + 1);
                }
            }

            board.unmakeMove();

            if (score > bestScore) {
                bestScore = score;

                pvTable[ply][0] = move;
                for (int j = 0; j < pvLength[ply + 1]; j++) {
                    pvTable[ply][j + 1] = pvTable[ply + 1][j];
                }
                pvLength[ply] = pvLength[ply + 1] + 1;
            }

            if (bestScore > alpha) alpha = bestScore;
            if (alpha >= beta) break; // Beta-Cutoff
        }

        return bestScore;
    }
}
*/


//einfache ArrayList

        private static final int INF = 99999999;

        public PrincipalVariation(int maxDepth) {}

        public ABResult pvSearch(Board board, int depth, int alpha, int beta) {
            SearchContext ctx = new SearchContext();

            try {
                return pvs(board, depth, alpha, beta, ctx);

            } catch (SearchStoppedException e) {
                e.printStackTrace();
                return null;
            }
        }

        private ABResult pvs(Board board, int depth, int alpha, int beta,
                             SearchContext ctx) throws SearchStoppedException {

            ctx.incrementPositions();
            if (ctx.shouldStop()) throw new SearchStoppedException("Zeitlimit erreicht");

            // Terminal / Blatt
            if (depth == 0 || board.gameIsEnd()) {
                ctx.incrementLeafs();
                int eval = BoardEvaluator.evaluate(board);
                if (board.sideToMove == MIN_PLAYER) eval = -eval;
                return new ABResult(eval, new ArrayList<>());
            }

            ArrayList<Move> moves = Board.generateLegalMoves(board, board.sideToMove);
            sortMoves(board, moves);

            boolean firstMove = true;
            int bestScore = -INF;
            List<Move> bestPV = new ArrayList<>();

            for (Move move : moves) {

                board.makeMove(move);
                ABResult child;

                if (firstMove) {
                    child = pvs(board, depth - 1, -beta, -alpha, ctx);
                    firstMove = false;
                } else {
                    child = pvs(board, depth - 1, -alpha - 1, -alpha, ctx);

                    if (child.value > alpha && child.value < beta) {
                        child = pvs(board, depth - 1, -beta, -alpha, ctx);
                    }
                }

                board.unmakeMove();

                int score = -child.value;

                if (score > bestScore) {
                    bestScore = score;

                    bestPV = new ArrayList<>();
                    bestPV.add(move);
                    bestPV.addAll(child.trace);
                }

                if (bestScore > alpha) alpha = bestScore;
                if (alpha >= beta) break; // Beta-Cutoff
            }

            return new ABResult(bestScore, bestPV);
        }




//verschachtelte Listen
        /*private static final int INF = 99999999;

        // PV wird pro Ply als Liste gespeichert
        private final List<List<Move>> pvTable = new ArrayList<>();
        private final List<Integer> pvLength = new ArrayList<>();

        public PrincipalVariation(int maxDepth) {
            for (int i = 0; i < maxDepth + 5; i++) {
                pvTable.add(new ArrayList<>());
                pvLength.add(0);
            }
        }

        public ABResult pvSearch(Board board, int depth, int alpha, int beta) {
            SearchContext ctx = new SearchContext();

            try {
                int score = pvs(board, depth, alpha, beta, ctx, 0);

                // Root-PV extrahieren
                ArrayList<Move> pv = new ArrayList<>(pvTable.get(0).subList(0, pvLength.get(0)));

                return new ABResult(score, pv);

            } catch (SearchStoppedException e) {
                e.printStackTrace();
                return null;
            }
        }

        private int pvs(Board board, int depth, int alpha, int beta,
                        SearchContext ctx, int ply) throws SearchStoppedException {

            ctx.incrementPositions();
            if (ctx.shouldStop()) throw new SearchStoppedException("Zeitlimit erreicht");

            // Terminal / Blatt
            if (depth == 0 || board.gameIsEnd()) {
                ctx.incrementLeafs();
                pvLength.set(ply, 0);
                int eval = BoardEvaluator.evaluate(board);
                if (board.sideToMove == MIN_PLAYER) eval = -eval;
                return eval;
            }

            ArrayList<Move> moves = Board.generateLegalMoves(board, board.sideToMove);
            sortMoves(board, moves);

            pvLength.set(ply, 0);
            boolean firstMove = true;
            int bestScore = -INF;

            for (Move move : moves) {

                board.makeMove(move);
                int score;

                if (firstMove) {
                    score = -pvs(board, depth - 1, -beta, -alpha, ctx, ply + 1);
                    firstMove = false;
                } else {
                    score = -pvs(board, depth - 1, -alpha - 1, -alpha, ctx, ply + 1);

                    if (score > alpha && score < beta) {
                        score = -pvs(board, depth - 1, -beta, -alpha, ctx, ply + 1);
                    }
                }

                board.unmakeMove();

                if (score > bestScore) {
                    bestScore = score;

                    // PV aktualisieren (Listen-Version)
                    List<Move> currentPV = pvTable.get(ply);
                    currentPV.clear();
                    currentPV.add(move);

                    List<Move> childPV = pvTable.get(ply + 1);
                    for (int j = 0; j < pvLength.get(ply + 1); j++) {
                        currentPV.add(childPV.get(j));
                    }

                    pvLength.set(ply, pvLength.get(ply + 1) + 1);
                }

                if (bestScore > alpha) alpha = bestScore;
                if (alpha >= beta) break; // Beta-Cutoff
            }

            return bestScore;
        }

*/
}
