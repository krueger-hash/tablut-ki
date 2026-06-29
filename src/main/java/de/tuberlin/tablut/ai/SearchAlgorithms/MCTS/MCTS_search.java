package de.tuberlin.tablut.ai.SearchAlgorithms.MCTS;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.Piece;

import java.util.ArrayList;

public class MCTS_search {

    MCTS_node root;
    Board rootBoard;
    MAST mast = new MAST();

    /// Für Weiterverwendung des Baums sinnvoll?
    ArrayList<MCTS_node> visitedStates; // Besser als Zobrist-List? /Nutzen, um Knoten mit Rollout zu tracken?

    MCTS_search(Board state){
        Board copy = Board.deepCopy(state);
        this.rootBoard = copy;
        this.root = new MCTS_node(null,null, copy, mast);
    }


    /// Wie sorgt man dafür, dass Baum wiederverwendbar bleibt?
    // Ursprungsboard als Parameter, damit für verschiedene Stellungen gesucht werden kann
    Move search(long timeLimit_ms){

        // Zeitbegrenzung für Suche
        long stopTime = System.currentTimeMillis() + timeLimit_ms;

        //Wurzel initialisieren => jetzt im Konstruktor und als Instanzvariable für Wiederverwendbarkeit

        //Schleife über Suchbaum mit UCT
        while(System.currentTimeMillis() < stopTime){
            Board searchBoard = Board.deepCopy(this.rootBoard);

            MCTS_node current = root;

            //Traversieren gemäß UCT bis terminaler Knoten gefunden wurde
            while(!current.isTerminal){
                current = current.getChildBestUCT(searchBoard);
                searchBoard.makeMove(current.moveToThis);
            }

            //Knoten ohne Rollout => Rollout
            if(current.timesVisited == 0){ // Knoten noch nicht besucht == Knoten hatte kein Rollout
                current.rollout(searchBoard); //rollout und backpropagation
            }
            //Knoten mit Rollout => Expandieren
            else {
                //Expandieren - aktuealisiert board auf das Board des expandierten Kindes
                current.expand(searchBoard);
                //Rollout eines der neuen Kindknoten (hier ggf. vorher noch eine Sortierung mittels Heuristik?)
                current.children.getLast().rollout(searchBoard);
            }
        }

        //Rückgabe bei Zeitablauf
        return this.root.getChildBestUCT(rootBoard).moveToThis;
    }

    //Update der Wurzel, um Teilbaum weiterverwenden zu können
    void updateRoot(Move myMove, Move opponentMove){
        try {
            //entsprechenden Knoten für resultierendes Board im nächsten Zug finden
            this.root = this.root.findChildWithMove(myMove).findChildWithMove(opponentMove);
        } catch(ChildNotFoundException e){
            e.printStackTrace();
            //Board manuell updaten, wenn es nicht schon im Baum ist
            rootBoard.makeMove(myMove);
            rootBoard.makeMove(opponentMove);
            this.root = new MCTS_node(null,null, rootBoard, this.mast);
        }
    }

    //grobes Anwendungsbeispiel ~
    static void main() {
        Board board = Board.fenToBoard();

        //MCTS-Baum für Schwarz
        MCTS_search mcts_BLACK = new MCTS_search(board);
//        mcts_BLACK.root.gameState.printBoard();
        Move bMove = mcts_BLACK.search(20_000);
        mcts_BLACK.root.printTree();


//        board.makeMove(bMove);
//
//        //MCTS-Baum für WEIß
//        MCTS_search mcts_WHITE = new MCTS_search(board);
////        mcts_WHITE.root.gameState.printBoard();
//        Move wMove = mcts_WHITE.search(1_000);
//
//        //Updaten der Wurzel für Schwarz
//        mcts_BLACK.updateRoot(bMove,wMove); // man kann hier auch den geworfenen ChildNotFoundFehler beobachten
//        mcts_WHITE.root.gameState.printBoard();
    }


}
