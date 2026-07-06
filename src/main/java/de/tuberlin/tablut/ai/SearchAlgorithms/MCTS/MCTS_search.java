package de.tuberlin.tablut.ai.SearchAlgorithms.MCTS;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.Piece;

import java.util.ArrayList;

/**
 * MCTS Search Algorithm
 */
public class MCTS_search {

    // Root node of the search tree
    MCTS_node root;
    // Board of the root node
    Board rootBoard;
    // Mast heuristics shared by all nodes for the entire search
    MAST mast = new MAST();

    public MCTS_search(Board state){
        Board copy = Board.deepCopy(state);
        this.rootBoard = copy;
        this.root = new MCTS_node(null,null, copy, mast);
    }

    // Ursprungsboard als Parameter, damit für verschiedene Stellungen gesucht werden kann
    public Move search(long timeLimit_ms){

        // Zeitbegrenzung für Suche
        long stopTime = System.currentTimeMillis() + timeLimit_ms;


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
                if (current.movesForExpansion.isEmpty()) {
                    // genuine terminal node (game over): nothing to expand,
                    // just re-evaluate and backprop its deterministic result
                    current.backprop(SimulateNode.simulateMAST(searchBoard, current.mast));
                } else {
                    current.expand(searchBoard);
                    current.children.getLast().rollout(searchBoard);
                }
            }
        }

        //Rückgabe bei Zeitablauf
        return this.root.getBestChild(rootBoard).moveToThis;
    }

    //Update der Wurzel, um Teilbaum weiterverwenden zu können
    public void updateRoot(Move myMove, Move opponentMove){
        // rootBoard muss immer im Gleichschritt mit der Wurzel fortschreiten,
        // sonst sucht search() auf einem veralteten Board
        rootBoard.makeMove(myMove);
        rootBoard.makeMove(opponentMove);
        try {
            //entsprechenden Knoten für resultierendes Board im nächsten Zug finden
            this.root = this.root.findChildWithMove(myMove).findChildWithMove(opponentMove);
            // verworfenen Teilbaum abtrennen, damit backprop/UCT lokal bleiben
            this.root.parent = null;
        } catch(ChildNotFoundException e){
            //Stellung war noch nicht im Baum -> neue Wurzel aus dem fortgeschrittenen Board
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
//        mcts_BLACK.root.printTree();
        mcts_BLACK.root.aboveAverageChildren();
        System.out.println("### Best Move: "+bMove);




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
