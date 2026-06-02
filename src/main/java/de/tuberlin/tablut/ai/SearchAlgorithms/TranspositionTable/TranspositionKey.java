package de.tuberlin.tablut.ai.SearchAlgorithms.TranspositionTable;

public record TranspositionKey(long hash, int movesWithoutCapture) {
}
