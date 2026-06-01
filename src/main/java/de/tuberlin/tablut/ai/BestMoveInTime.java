package de.tuberlin.tablut.ai;

import de.tuberlin.tablut.ai.SearchAlgorithms.*;
import lombok.Getter;

public class BestMoveInTime {


    private static final int ALPHA_INIT = BoardEvaluator.ALPHA_INIT;
    private static final int BETA_INIT = BoardEvaluator.BETA_INIT;

    private final Board originalState;
    private final int msTime;

    @Getter
    private SearchReport finalReport;

    public BestMoveInTime(Board originalState, int msTime) {
        this.originalState = Board.deepCopy(originalState);
        this.msTime = msTime;
    }

    // TODO: getter methods for other search algorithms e.g. minimax
    public Move getMove() {
        finalReport = searchInTime(
                originalState,
                msTime,
                AlphaBetaTransposition::search
        );
        return finalReport.bestMove();
    }

    // calls search algorithm with time limit and default max depth
    public static SearchReport searchInTime(Board originalState, int msTime, SearchFunction search) {

        return searchInTime(originalState, Integer.MAX_VALUE, msTime, search);
    }

    public static SearchReport searchInTime(Board originalState, int maxDepth, int msTime, SearchFunction search) {
        long start = System.currentTimeMillis();
        SearchContext context = new SearchContext(msTime);
        SearchReport lastCompleted = null;

        // * Iterative Tiefensuche
        for (int depth = 1; depth <= maxDepth; depth++) {
            long iterationStart = System.currentTimeMillis();
            Board state = Board.deepCopy(originalState);

            // * Aufruf der Tiefensuche auf fester Ebene; Timeout über Exception umgesetzt
            try {
                lastCompleted = searchAtDepth(state, depth, start, search, context);
            } catch (SearchStoppedException e) {
                break;
            }

            // * weitere Abbruchbedingungen für Tiefensuche
            // Sieg erkannt
            if (originalState.sideToMove == BoardEvaluator.MAX_PLAYER && lastCompleted.value() > BoardEvaluator.ASSUME_BLACK_VICTORY_SCORE){ break;}
            if (originalState.sideToMove == BoardEvaluator.MIN_PLAYER && lastCompleted.value() < BoardEvaluator.ASSUME_WHITE_VICTORY_SCORE){ break;}

            // Nicht mehr genug Zeit für weitere Tiefen -
            // TODO: besserer Check
            long iterationTime = System.currentTimeMillis() - iterationStart;
            long remainingTime = context.getEndTime() - System.currentTimeMillis();
            if (context.shouldStop() || iterationTime > remainingTime) {
                break;
            }
        }
//        System.out.println("Runtime: "+(System.currentTimeMillis()-start));

        return lastCompleted;
    }

    public static SearchReport searchAtDepth(Board originalState, int depth, long startTime, SearchFunction search, SearchContext context) throws SearchStoppedException {
//        long start = System.currentTimeMillis();
//        SearchContext context = new SearchContext(msTime);
        Board state = Board.deepCopy(originalState);

        ABResult result = search.search(state, depth, context);
        long now = System.currentTimeMillis();
        return new SearchReport(
                result.getBestMoveAtNode(),
                result.getValue(),
                depth,
                context.getPositions(),
                context.getLeafs(),
                now - startTime,
                true,
                result.getTrace().reversed()

        );
    }

    public static ABResult alphaBetaSearch(Board board, int depth, SearchContext context) throws SearchStoppedException {
        return AlphaBeta.sortedAlphaBetaSearch(board, depth, ALPHA_INIT, BETA_INIT, context);
    }
}