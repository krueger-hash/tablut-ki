package de.tuberlin.tablut.ai.SearchAlgorithms;

import de.tuberlin.tablut.ai.Board;

@FunctionalInterface
public interface SearchFunction {
    ABResult search(Board board, int depth, SearchContext context) throws SearchStoppedException;
}
