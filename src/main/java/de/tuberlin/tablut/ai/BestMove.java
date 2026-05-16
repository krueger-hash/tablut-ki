package de.tuberlin.tablut.ai;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

public class BestMove {

    private static final int ALPHA_INIT = -1_000_000;
    private static final int BETA_INIT = 1_000_000;


    // Diese Variablen könnten theoretisch auch in die Funktionslogik als lokale Variablen integriert werden, dann wäre jedoch der Zugriff von außen weniger leicht zum Testen.

    Move bestMove;
    int bestValue;
    List<Move> bestPath;
    int maxDepth;

    private final long tStart;
    @Getter
    private long runtime;
    @Getter
    private long runtimeDuringIteration;


    BestMove(){
        this.bestMove = null;
        this.tStart = System.currentTimeMillis();
        this.maxDepth = 0;
    }

    Move calcBestMoveInTime(Board originalState, int msTimeout) {

        SearchContext context = new SearchContext(msTimeout, originalState.sideToMove);
        Board state = Board.deepCopy(originalState);

        // * Logik von BestMove
        // Liste der Ausgangsmoves erstellen zwischen denen gewählt wird und zurücksetzen der statischen Variablen
        ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
        this.bestMove = moves.getFirst(); // hier könnte man die Auswahl auch random machen?
        this.bestValue = 0;// der erste Move erstmal als Ausgangspunkt

        //iterative Tiefensuche
        for (int depth = 1;; depth++) {
            if(context.shouldStop()){break;} // könnte eigentlich auch in Schleifenkopf, aber so lesbarer

            //Aufruf der Tiefensuche
            this.bestMoveAtDepth(state, depth, context);

            //weitere sinnvolle Abbruchbedingungen?
            long remainingTime = context.getEndTime() - System.currentTimeMillis();
            if(this.runtimeDuringIteration >= remainingTime){break;}
        }

        this.runtime = System.currentTimeMillis() - this.tStart;
        return this.bestMove;
    }

    void bestMoveAtDepth(Board state, int depth, SearchContext context){

        if (state.sideToMove != BoardEvaluator.MAX_PLAYER && state.sideToMove != BoardEvaluator.MIN_PLAYER){
            throw new IllegalStateException("Übergebenes Board hat ungültige .sideToMove");
        }

        //Initialisierung der Variablen für diese Suchtiefe
        long tStart = System.currentTimeMillis();

        //Abbruch bei Timeout // eigentlich hier wohl unnötig
        if(context.shouldStop()){return;}

        //Tiefe 0 ist der Wurzelknoten. dort gibt es noch keine Moves
        if(depth == 0){
            this.bestValue = BoardEvaluator.evaluate(state);
            this.bestMove = null;
            return;
        }

        //Während AB-Suche wird das SearchContext-Objekt aktualisiert
        try {
            ABResult result = AlphaBeta.sortedAlphaBetaSearch(state, depth, ALPHA_INIT, BETA_INIT, context);
            //Das sollte nicht mehr ausgeführt werden, wenn während AB-Suche die Exception geworfen wird
            this.bestValue = result.value;
            this.bestMove = result.trace.getLast();
            this.bestPath = result.trace.reversed();
            this.maxDepth = depth;
        } catch (SearchStoppedException e) { //Abbruch, wenn Zeit abgelaufen ist
            return;
        }

        this.runtimeDuringIteration = System.currentTimeMillis() - tStart;
        return;
    }
}
