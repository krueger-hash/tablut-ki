package de.tuberlin.tablut.ai.SearchAlgorithms;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class SearchControlParameters {
    // Sort moves configurations
    public static boolean SORT_MOVES_ACTIVE = true;
    public static boolean HISTORY_HEURISTICS_ACTIVE = true;
    public static boolean SORT_MOVES_BY_VALUE = true;

    // Search configurations
    public static boolean TRANSPOSITION_TABLE_ACTIVE = true;
    public static boolean PVS_ACTIVE = true;

    public static void updateSearchControlParameters(boolean sortMovesActive, boolean historyHeuristicsActive, boolean sortMovesByValue, boolean transpositionTableActive, boolean pvsActive){
        SORT_MOVES_ACTIVE = sortMovesActive;
        HISTORY_HEURISTICS_ACTIVE = historyHeuristicsActive;
        SORT_MOVES_BY_VALUE = sortMovesByValue;
        TRANSPOSITION_TABLE_ACTIVE = transpositionTableActive;
        PVS_ACTIVE = pvsActive;
    }
}
