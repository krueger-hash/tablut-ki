package de.tuberlin.tablut.ai;

import java.util.ArrayList;
import java.util.Stack;
import java.util.concurrent.*;

import lombok.Getter;

public class BestMove {
    private static final Player maxPlayer = BoardEvaluator.MAX_PLAYER;
    private static final Player minPlayer = BoardEvaluator.MIN_PLAYER;

    private static final int ALPHA_INIT = -1_000_000;
    private static final int BETA_INIT = 1_000_000;


    // Diese Variablen könnten theoretisch auch in die Funktionlogik als lokale Variablen integriert werden, dann wäre jedoch der Zugriff von außen weniger leicht zum Testen.
    private Move bestMove;

    @Getter
    private int bestValue;
    @Getter
    private Move bestMoveDuringIteration;
    @Getter
    private int bestValueDuringIteration;

    private long tStart;
    @Getter
    private long runtime;
    @Getter
    private long runtimeDuringIteration;
    @Getter
    private int maxDepth;
    @Getter
    private ArrayList<Move> bestSequence = null;
    @Getter
    private ArrayList<Move> bestSequenceDuringIteration = null;

    BestMove(){
        this.bestValue = 0;
        this.bestMove = null;
        this.bestValueDuringIteration = 0;
        this.bestMoveDuringIteration = null;
        this.tStart = System.currentTimeMillis();
        this.maxDepth = 0;
    }

    Move getBestMove(Board originalState, int msTimeout) {

        SearchContext context = new SearchContext(msTimeout);
        Board state = Board.deepCopy(originalState);

        // * Logik von BestMove
        // Liste der Ausgangsmoves erstellen zwischen denen gewählt wird und zurücksetzen der statischen Variablen
        ArrayList<Move> moves = Board.generateLegalMoves(state, state.sideToMove);
        this.bestMove = moves.getFirst(); // hier könnte man die Auswahl auch random machen?
        this.bestValue = 0;// der erste Move erstmal als Ausgangspunkt

        //iterative Tiefensuche
        for (int depth = 1;; depth++) {
            if(context.shouldStop()){break;} // könnte eigentlich auch in Schleifenkopf, aber so lesbarer

            this.bestMoveAtDepth(state, moves, depth, context);

            if(context.shouldStop()){break;} // Verhindere, dass bestMove und bestValue bei abgebrochener Tiefensuche überschrieben werden
            this.bestMove = this.bestMoveDuringIteration;
            this.bestValue = this.bestValueDuringIteration;
            this.maxDepth = depth;
            this.bestSequence = this.bestSequenceDuringIteration;
            System.out.println(this.maxDepth);

            //weitere sinnvolle Abbruchbedingungen?
            long remainingTime = context.getEndTime() - System.currentTimeMillis();
            if(this.runtimeDuringIteration >= remainingTime){break;}
        }

        this.runtime = System.currentTimeMillis() - this.tStart;
        return this.bestMove;
    }

    void bestMoveAtDepth(Board state, ArrayList<Move> moves, int depth, SearchContext context){

        if (state.sideToMove != maxPlayer && state.sideToMove != minPlayer){
            throw new IllegalStateException("Übergebenes Board hat ungültige .sideToMove");
        }

        //Initialisierung der Variablen für diese Suchtiefe
        long tStart = System.currentTimeMillis();
        this.bestMoveDuringIteration = this.bestMove;
        boolean isMaxing = (state.sideToMove == maxPlayer);
        // bestValue muss auf jeder Suchtiefe neu initialisiert werden, da ggf. bei größerer Tiefe identische Züge schlechter bewertet werden können, als mit geringerer Tiefe
        if(isMaxing){this.bestValueDuringIteration = ALPHA_INIT;}
        else {this.bestValueDuringIteration = BETA_INIT;}

        if(context.shouldStop()){return;}
        //Tiefe 0 ist der Wurzelknoten. dort gibt es noch keine Moves
        if(depth == 0){
            this.bestValueDuringIteration = BoardEvaluator.evaluate(state);
            this.bestMove = null;
            return;
        }

        for (Move move : moves){
            if(context.shouldStop()){break;}
            context.bestSequence.push(move);
            state.makeMove(move);
            //Aufruf mit depth-1, da die erste Ebene (die moves) bereits generiert wurde; d.h. wird mit depth = 1 aufgerufen, wird der Wert des ersten Halbzugs ausgewertet
            int value;
            try {
                value = AlphaBeta.sortedAlphaBetaSearch(state, depth - 1, ALPHA_INIT, BETA_INIT, context);
            } catch (SearchStoppedException e) { //Abbruch, wenn Zeit abgelaufen ist
                break;
            }
            state.unmakeMove();
            context.bestSequence.pop();

            if(isMaxing) {
                if (value > this.bestValueDuringIteration) {
                    this.bestValueDuringIteration = value;
                    this.bestMoveDuringIteration = move;
                }
            }
            else{
                if (value < this.bestValueDuringIteration) {
                    this.bestValueDuringIteration = value;
                    this.bestMoveDuringIteration = move;
                }
            }
            this.bestSequenceDuringIteration = new ArrayList<>(context.bestSequence); // Move Stack aus Suche in Liste konvertieren
        }
        this.runtimeDuringIteration = System.currentTimeMillis() - tStart;
        return;
    }
}
