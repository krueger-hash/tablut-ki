package de.tuberlin.tablut.ai.SearchAlgorithms.MCTS;

/**
 * Error if the child for a node is not found during updateRoot()
 */
public class ChildNotFoundException extends Exception {
    public ChildNotFoundException(String message) {
        super(message);
    }
}
