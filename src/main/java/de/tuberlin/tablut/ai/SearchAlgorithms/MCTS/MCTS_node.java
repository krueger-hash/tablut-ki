package de.tuberlin.tablut.ai.SearchAlgorithms.MCTS;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.BoardEvaluator;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.Player;

import java.util.ArrayList;

public class MCTS_node {

    // parameter 'c' in UCT, erstmal final, ggf. später tunen
    final static double exploration_parameter = Math.sqrt(2);

    //Node-Parameter
    int score;
    int timesVisited;
    boolean isTerminal; // Knoten ohne Expansion sind terminal => initialisiert mit true; nach Aufruf von 'expand' -> false

    Board gameState;
    Move moveToThis;


    //Baumstruktur
    /// Folgeknoten oder entsprechende Moves speichern?
    /// Wie wird der Spielzustand getrackt? Das Board in die MCTS_nodes setzen, oder beim Bewegen durch Baum makeMove/unmakeMove?
    MCTS_node parent;
    ArrayList<MCTS_node> children;
    ArrayList<Move> movesForExpansion; ///oder doch besser die nodes, das wäre zumindest näher am kanonischen Algorithmus

    MCTS_node(MCTS_node parent, Move moveToThis, Board gameState){
        this.parent = parent;
        this.moveToThis = moveToThis;
        this.gameState = gameState;
        this.score = 0;
        this.timesVisited = 0;
        this.isTerminal = true;

        this.children = new ArrayList<MCTS_node>();

        //Keine Folgezüge, wenn das Spiel im aktuellen Zustand endet
        if(gameState.gameIsEnd()){movesForExpansion = new ArrayList<>();}
        else {
            this.movesForExpansion = Board.generateLegalMoves(gameState,gameState.sideToMove);
        }
    }

    void rollout(){
        if(this.score != 0|| this.timesVisited != 0) {
            throw new RuntimeException("Rollout for Node that was already rolled out!");
        }
        int newScore = BoardEvaluator.evaluate(this.gameState);
        this.backprop(newScore);
        return;
    }
    void backprop(int score){
        this.score += score;
        this.timesVisited += 1;
        if(this.parent != null){
            this.parent.backprop(score);
        }
        return;
    }

    //Fallunterscheidung für MIN und MAX
    double calcUCT(boolean isMax) throws NodeNotRolledOutException {
        double w = this.score;
        int n = this.timesVisited;
        int parent_n = this.parent.timesVisited;
        double c = MCTS_node.exploration_parameter;

        if(n == 0) {throw new NodeNotRolledOutException("Node has not been rolled out. UCT is INF!");}

        double mean = w/c;
        double exploration = Math.sqrt(Math.log(parent_n) / n);
        if(isMax) {
            return mean + exploration;
        }
        else{
            return -mean + exploration;
        }
    }

    MCTS_node getChildBestUCT(){
        /// Fallunterscheidung für MIN und MAX (grundsätzlich auch in calcUCT möglich, über .parent, aber da hab ich erstmal nen fehler gemacht... so ist es klarer, dass es die Perspektive des Elternknoten ist)
        boolean isMax = this.gameState.sideToMove == Player.BLACK;

        //Startwert
        MCTS_node bestChild = this.children.getFirst();
        double bestUCT;
        try {
            bestUCT = bestChild.calcUCT(isMax);
        } catch(NodeNotRolledOutException e) {return bestChild;}

        //Schleife über alle Werte
        for (MCTS_node node : this.children){
            double uct;
            try{
                uct = node.calcUCT(isMax);
            } catch(NodeNotRolledOutException e) {return node;}
            if(uct > bestUCT)  {
                bestChild = node;
                bestUCT = uct;
            }
        }
        return bestChild;
    }

    void expand(){
        for(Move move : this.movesForExpansion){
            this.children.add(
                    //BoardAfterMove arbeitet mit Kopien!
                    new MCTS_node(this,move,Board.boardAfterMove(this.gameState,move))
            );
        }
        this.movesForExpansion.clear(); // Nach Erstellen aller Kinder kann gecleart werden, da alle Kinder generiert sind
        this.isTerminal = false; // ein expandierter Knoten ist nicht mehr terminal!
    }

    MCTS_node findChildWithMove(Move move) throws ChildNotFoundException{
        for(MCTS_node node : this.children){
            if(node.moveToThis == move){
                return node;
            }
        }
        throw new ChildNotFoundException("No fitting Child found");
    }

    @Override
    public String toString() {
        return toString(0);
    }
    public String toString(int depth) {
        StringBuilder out = new StringBuilder();
        out.append("[ " +
                "w = " + score +
                ", n = " + timesVisited +
                ", moveToThis = " + moveToThis +
                " ]");
        for(MCTS_node child : this.children){
            if(child.timesVisited != 0) {
                out.append("\n\t");
                for (int i = 0; i<depth;i++){
                    out.append("\t");
                }
                out.append("|--" + child.toString(depth+1));
            }
        }
        return out.toString();
    }
    void printTree(){
        System.out.println(this.toString());
    }
}
