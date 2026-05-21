package de.tuberlin.tablut.ai;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class BestMoveInTime {

    private static final Player maxPlayer = BoardEvaluator.MAX_PLAYER;
    private static final Player minPlayer = BoardEvaluator.MIN_PLAYER;


    private static final int ALPHA_INIT = -1_000_000;
    private static final int BETA_INIT = 1_000_000;

    private final Board originalState;
    private final int msTime;

    @Getter
    private SearchReport finalReport;

    public BestMoveInTime(Board originalState, int msTime) {
        this.originalState = Board.deepCopy(originalState);
        this.msTime = msTime;
    }

    public Move getMove() {
        finalReport = searchInTime(
                originalState,
                msTime,
                BestMoveInTime::alphaBetaSearch
        );
        return finalReport.bestMove();
    }

    public static SearchReport searchInTime(Board originalState, int msTime, SearchFunction search) {
        return searchInTime(originalState, Integer.MAX_VALUE, msTime, search);
    }

    public static SearchReport searchInTime(Board originalState, int maxDepth, int msTime, SearchFunction search) {
        long start = System.currentTimeMillis();
        SearchContext context = new SearchContext(msTime);
        SearchReport lastCompleted = null;

        for (int depth = 1; depth <= maxDepth; depth++) {
            long iterationStart = System.currentTimeMillis();
            Board state = Board.deepCopy(originalState);

            try {
                lastCompleted = searchAtDepth(state, depth, msTime, search);
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

    public static SearchReport searchAtDepth(Board originalState, int depth, int msTime, SearchFunction search) throws SearchStoppedException {
        long start = System.currentTimeMillis();
        SearchContext context = new SearchContext(msTime);
        Board state = Board.deepCopy(originalState);

        ABResult result = search.search(state, depth, context);
        long now = System.currentTimeMillis();
        return new SearchReport(
                result.getBestMoveAtNode(),
                result.getValue(),
                depth,
                context.getPositions(),
                context.getLeafs(),
                now - start,
                true,
                result
        );
    }

    public static ABResult alphaBetaSearch(Board board, int depth, SearchContext context) throws SearchStoppedException {
        return AlphaBeta.sortedAlphaBetaSearch(board, depth, ALPHA_INIT, BETA_INIT, context);
    }
}