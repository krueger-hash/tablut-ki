package de.tuberlin.tablut.ai;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BestMoveInTime {

    private static final Player maxPlayer = BoardEvaluator.MAX_PLAYER;
    private static final Player minPlayer = BoardEvaluator.MIN_PLAYER;

    private static final int ALPHA_INIT = -1_000_000;
    private static final int BETA_INIT = 1_000_000;

    private volatile Move bestMove;
    private volatile int bestValue;
    private volatile int maxDepth;
    private volatile long leafs;
    private volatile long positions;
    private volatile long runtime;
    private volatile ABResult bestResult;

    private volatile Move bestMoveDuringIteration;
    private volatile int bestValueDuringIteration;

    private final CompletableFuture<Move> future;

    public BestMoveInTime(Board originalState, int msTime) {
        this.future = CompletableFuture.supplyAsync(() -> {
            SearchReport report = searchInTime(originalState, msTime, Integer.MAX_VALUE, BestMoveInTime::alphaBetaSearch);
            updateFromReport(report);
            return this.bestMove;
        }).orTimeout(msTime, TimeUnit.MILLISECONDS);
    }

    public Move getMove() {
        try {
            return future.join();
        } catch (Exception e) {
            return bestMove;
        }
    }

    public int getBestValueDuringIteration() {
        return bestValueDuringIteration;
    }

    public Move getBestMoveDuringIteration() {
        return bestMoveDuringIteration;
    }

    public int getBestValue() {
        return bestValue;
    }

    public static SearchReport searchInTime(Board originalState, int msTime, int maxDepth, SearchFunction search) {
        long start = System.currentTimeMillis();
        SearchContext context = new SearchContext(msTime, originalState.sideToMove);
        SearchReport lastCompleted = emptyReport(originalState, start);

        for (int depth = 1; depth <= maxDepth; depth++) {
            long iterationStart = System.currentTimeMillis();
            Board state = Board.deepCopy(originalState);

            try {
                ABResult result = search.search(state, depth, context);
                long now = System.currentTimeMillis();
                lastCompleted = new SearchReport(
                        result.getBestMove(),
                        result.getValue(),
                        depth,
                        context.getPositions(),
                        context.getLeafs(),
                        now - start,
                        true,
                        result
                );
            } catch (SearchStoppedException e) {
                break;
            }

            long iterationTime = System.currentTimeMillis() - iterationStart;
            long remainingTime = context.getEndTime() - System.currentTimeMillis();
            if (context.shouldStop() || iterationTime > remainingTime) {
                break;
            }
        }

        return lastCompleted;
    }

    public static SearchReport searchAtDepth(Board originalState, int depth, int msTime, SearchFunction search) {
        long start = System.currentTimeMillis();
        SearchContext context = new SearchContext(msTime, originalState.sideToMove);
        Board state = Board.deepCopy(originalState);

        try {
            ABResult result = search.search(state, depth, context);
            long now = System.currentTimeMillis();
            return new SearchReport(
                    result.getBestMove(),
                    result.getValue(),
                    depth,
                    context.getPositions(),
                    context.getLeafs(),
                    now - start,
                    true,
                    result
            );
        } catch (SearchStoppedException e) {
            long now = System.currentTimeMillis();
            return new SearchReport(
                    null,
                    0,
                    depth,
                    context.getPositions(),
                    context.getLeafs(),
                    now - start,
                    false,
                    null
            );
        }
    }

    private void updateFromReport(SearchReport report) {
        this.bestMove = report.bestMove();
        this.bestValue = report.value();
        this.maxDepth = report.depth();
        this.leafs = report.leafs();
        this.positions = report.positions();
        this.runtime = report.millis();
        this.bestResult = report.result();
    }

    private static SearchReport emptyReport(Board originalState, long start) {
        ArrayList<Move> moves = Board.generateLegalMoves(originalState, originalState.sideToMove);
        Move fallbackMove = moves.isEmpty() ? null : moves.getFirst();
        return new SearchReport(fallbackMove, 0, 0, 0, 0, System.currentTimeMillis() - start, false, null);
    }

    private static ABResult alphaBetaSearch(Board board, int depth, SearchContext context) throws SearchStoppedException {
        return AlphaBeta.sortedAlphaBetaSearch(board, depth, ALPHA_INIT, BETA_INIT, context);
    }

    // Illustration der Anwendung
    static void main() {
        String fen = "3bbb3/4b4/4w4/b3w3b/bbwwKwwbb/b3w3b/4w4/4b4/3bbb3 S 48";
        Board test = Board.fenToBoard(fen);

        Move niceMove = new BestMoveInTime(test, 1).getMove();

        // ungetestet :/
    }

    // während einer Suchtiefe wird der beste Move auf der Iterationsvariable gespeichert, damit bestMove nur basierend auf einer vollständig durchsuchten Ebene zurückgegeben wird;
    // bestValue benötigt keine intermediate Variable
    ABResult bestMoveAtDepth(Board state, ArrayList<Move> moves, int depth) {

        if (state.sideToMove != maxPlayer && state.sideToMove != minPlayer) {
            throw new IllegalStateException("Übergebenes Board hat ungültige .sideToMove");
        }

        this.bestMoveDuringIteration = this.bestMove;
        boolean isMaxing = (state.sideToMove == maxPlayer);
        if (isMaxing) {
            this.bestValueDuringIteration = ALPHA_INIT;
        } else {
            this.bestValueDuringIteration = BETA_INIT;
        }

        if (depth == 0) {
            this.bestValueDuringIteration = BoardEvaluator.evaluate(state);
            this.bestMove = null;
            ABResult result = new ABResult(this.bestValueDuringIteration, new ArrayList<>());
            this.bestResult = result;
            return result;
        }

        ABResult bestResultDuringIteration = null;
        for (Move move : moves) {
            state.makeMove(move);
            ABResult result = AlphaBeta.sortedAlphaBetaSearch(state, depth - 1, ALPHA_INIT, BETA_INIT);
            int value = result.getValue();
            state.unmakeMove();

            boolean isBetter = isMaxing
                    ? value > this.bestValueDuringIteration
                    : value < this.bestValueDuringIteration;

            if (bestResultDuringIteration == null || isBetter) {
                this.bestValueDuringIteration = value;
                this.bestMoveDuringIteration = move;

                ArrayList<Move> trace = new ArrayList<>(result.getTrace());
                trace.add(move);
                bestResultDuringIteration = new ABResult(value, trace);
            }
        }
        return bestResultDuringIteration;
    }
}
