package de.tuberlin.tablut.ai.SearchAlgorithms.MCTS;

import de.tuberlin.tablut.ai.Board;
import de.tuberlin.tablut.ai.BoardEvaluator;
import de.tuberlin.tablut.ai.Move;
import de.tuberlin.tablut.ai.Player;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class MCTS_node {

    // parameter 'c' in UCT, erstmal final, ggf. später tunen
    final static double exploration_parameter = Math.sqrt(2);

    //Node-Parameter
    int score;
    int timesVisited;
    boolean isTerminal; // Knoten ohne Expansion sind terminal => initialisiert mit true; nach Aufruf von 'expand' -> false
    Move moveToThis;



    //Baumstruktur
    /// Folgeknoten oder entsprechende Moves speichern?
    /// Wie wird der Spielzustand getrackt? Das Board in die MCTS_nodes setzen, oder beim Bewegen durch Baum makeMove/unmakeMove?
    MCTS_node parent;
    ArrayList<MCTS_node> children;
    List<Move> movesForExpansion;

    MCTS_node(MCTS_node parent, Move moveToThis, Board gameState) {
        this.parent = parent;
        this.moveToThis = moveToThis;
        this.score = 0;
        this.timesVisited = 0;
        this.isTerminal = true;

        this.children = new ArrayList<MCTS_node>();

        if (gameState.gameIsEnd()) {
            movesForExpansion = new ArrayList<>();
        } else {
            movesForExpansion = Board.generateLegalMoves(gameState, gameState.sideToMove);
        }
    }
    // a -> b -> c ->
    // generierung
    // zufälliger zug
    // anwenden
    // wechsel seite
    // weiderholung bis end
    void rollout(Board gameState) {
        if (this.score != 0 || this.timesVisited != 0) {
            throw new RuntimeException("Rollout for Node that was already rolled out!");
        }
        int newScore = SimulateNode.randomMoves(gameState); // BoardEvaluator.evaluate(gameState);
        this.backprop(newScore);
        return;
    }

    void backprop(int score) {
        this.score += score;
        this.timesVisited += 1;
        if (this.parent != null) {
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

        if (n == 0) {
            throw new NodeNotRolledOutException("Node has not been rolled out. UCT is INF!");
        }

        double mean = w / n;
        double exploration = c * Math.sqrt(Math.log(parent_n) / n);
        if (isMax) {
            return mean + exploration;
        } else {
            return -mean + exploration;
        }
    }

    MCTS_node getChildBestUCT(Board gameState) {
        /// Fallunterscheidung für MIN und MAX (grundsätzlich auch in calcUCT möglich, über .parent, aber da hab ich erstmal nen fehler gemacht... so ist es klarer, dass es die Perspektive des Elternknoten ist)
        boolean isMax = gameState.sideToMove == Player.BLACK;

        //Startwert
        MCTS_node bestChild = this.children.getFirst();
        double bestUCT;
        try {
            bestUCT = bestChild.calcUCT(isMax);
        } catch (NodeNotRolledOutException e) {
            return bestChild;
        }

        //Schleife über alle Werte
        for (MCTS_node node : this.children) {
            double uct;
            try {
                uct = node.calcUCT(isMax);
            } catch (NodeNotRolledOutException e) {
                return node;
            }
            if (uct > bestUCT) {
                bestChild = node;
                bestUCT = uct;
            }
        }
        return bestChild;
    }

    void expand(Board gameState) {
        Move move = this.movesForExpansion.removeFirst();
        gameState.makeMove(move);
        this.children.add(
                //BoardAfterMove arbeitet mit Kopien!
                new MCTS_node(this, move, gameState)
        );
        if(movesForExpansion.isEmpty()){
            this.isTerminal = false;
        }
    }

    MCTS_node findChildWithMove(Move move) throws ChildNotFoundException {
        for (MCTS_node node : this.children) {
            if (node.moveToThis.equals(move)) {
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
        for (MCTS_node child : this.children) {
            if (child.timesVisited != 0) {
                out.append("\n\t");
                for (int i = 0; i < depth; i++) {
                    out.append("\t");
                }
                out.append("|--" + child.toString(depth + 1));
            }
        }
        return out.toString();
    }

    void printTree() {
        String result = this.toString();
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("result"));
            writer.write(result);

            writer.close();
        } catch (Exception e) {
        }
    }
}
