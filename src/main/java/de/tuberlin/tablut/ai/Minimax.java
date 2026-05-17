package de.tuberlin.tablut.ai;

import java.util.ArrayList;

public final class Minimax {
    private static final int ALPHA_INIT = -1_000_000;
    private static final int BETA_INIT = 1_000_000;

    private Minimax() {
    }

    public static ABResult minimaxSearch(Board state, int depth, SearchContext context) throws SearchStoppedException {
        context.incrementPositions();
        if (context.shouldStop()) {
            throw new SearchStoppedException("Zeitlimit erreicht");
        }

        if (depth == 0 || state.gameIsEnd()) {
            context.incrementLeafs();
            return new ABResult(BoardEvaluator.evaluate(state), new ArrayList<>());
        }

        boolean isMaxing = state.sideToMove == BoardEvaluator.MAX_PLAYER;
        int bestValue = isMaxing ? ALPHA_INIT : BETA_INIT;
        ArrayList<Move> bestPath = new ArrayList<>();
        ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);

        if (moves.isEmpty()) {
            context.incrementLeafs();
            return new ABResult(BoardEvaluator.evaluate(state), bestPath);
        }

        for (Move move : moves) {
            if (context.shouldStop()) {
                throw new SearchStoppedException("Zeitlimit erreicht");
            }

            state.makeMove(move);
            ABResult child = minimaxSearch(state, depth - 1, context);
            state.unmakeMove();

            int score = child.getValue();
            if (isBetterValue(score, bestValue, isMaxing)) {
                bestValue = score;
                bestPath = new ArrayList<>(child.getTrace());
                bestPath.add(move);
            }
        }

        return new ABResult(bestValue, bestPath);
    }

    private static boolean isBetterValue(int value, int bestValue, boolean isMaxing) {
        return isMaxing ? value > bestValue : value < bestValue;
    }
}
