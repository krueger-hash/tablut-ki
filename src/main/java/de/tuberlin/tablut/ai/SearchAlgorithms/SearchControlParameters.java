package de.tuberlin.tablut.ai.SearchAlgorithms;

import lombok.Getter;

public class SearchControlParameters {
    // Sort moves configurations
    public static final boolean SORT_MOVES_ACTIVE = true;
    public static final boolean HISTORY_HEURISTICS_ACTIVE = true;
    public static final boolean SORT_MOVES_BY_VALUE = true;

    // Search configurations
    public static final boolean TRANSPOSITION_TABLE_ACTIVE = true;
    public static final boolean PVS_ACTIVE = true;
}
