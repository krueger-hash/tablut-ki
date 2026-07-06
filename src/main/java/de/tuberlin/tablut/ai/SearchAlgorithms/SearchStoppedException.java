package de.tuberlin.tablut.ai.SearchAlgorithms;

/**
 * Signal to stop the search if time is exceeded
 */
public class SearchStoppedException extends Exception {

    public SearchStoppedException(String msg){
        super(msg);
    }
}
