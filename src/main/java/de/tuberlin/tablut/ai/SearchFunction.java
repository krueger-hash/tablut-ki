package de.tuberlin.tablut.ai;

@FunctionalInterface
public interface SearchFunction {
    ABResult search(Board board, int depth, SearchContext context) throws SearchStoppedException;
}
