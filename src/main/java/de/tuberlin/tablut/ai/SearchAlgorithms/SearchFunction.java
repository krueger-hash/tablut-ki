package de.tuberlin.tablut.ai.SearchAlgorithms;

import de.tuberlin.tablut.ai.Board;

/**
 * Interface to use different search function
 */
@FunctionalInterface
public interface SearchFunction {
    SearchResult search(Board board, int depth, SearchContext context) throws SearchStoppedException;
}
