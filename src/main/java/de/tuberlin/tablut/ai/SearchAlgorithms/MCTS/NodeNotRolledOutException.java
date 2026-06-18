package de.tuberlin.tablut.ai.SearchAlgorithms.MCTS;

public class NodeNotRolledOutException extends Exception {
    public NodeNotRolledOutException(String message) {
        super(message);
    }
}
