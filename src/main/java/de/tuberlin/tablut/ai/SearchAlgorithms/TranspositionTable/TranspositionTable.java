package de.tuberlin.tablut.ai.SearchAlgorithms.TranspositionTable;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.Move;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Transposition table - stores key informations about nodes for the whole search
 */
public class TranspositionTable {

    @Getter
    private final Map<TranspositionKey, TranspositionEntry> transpositionTable = new HashMap<>();

    public TranspositionEntry get(Board board) {
        return transpositionTable.get(key(board));
    }

    public TranspositionKey key(Board board) {
        return new TranspositionKey(board.getCurrentHash(), board.movesWithoutCapture);
    }
}
