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
    double progressiveBiasHeuristic;
    MAST mast;


    //Baumstruktur
    /// Folgeknoten oder entsprechende Moves speichern?
    /// Wie wird der Spielzustand getrackt? Das Board in die MCTS_nodes setzen, oder beim Bewegen durch Baum makeMove/unmakeMove?
    MCTS_node parent;
    ArrayList<MCTS_node> children;
    List<Move> movesForExpansion;

    MCTS_node(MCTS_node parent, Move moveToThis, Board gameState, MAST mast) {
        this.parent = parent;
        this.moveToThis = moveToThis;
        this.score = 0;
        this.timesVisited = 0;
        this.isTerminal = true;
        this.mast = mast;

        this.children = new ArrayList<MCTS_node>();

        this.progressiveBiasHeuristic = MCTS_Evaluator.boardScore(gameState);

        if (gameState.gameIsEnd()) {
            movesForExpansion = new ArrayList<>();
        } else {
            movesForExpansion = Board.generateLegalMoves(gameState, gameState.sideToMove);
        }
    }

    void rollout(Board gameState) {
        if (this.score != 0 || this.timesVisited != 0) {
            throw new RuntimeException("Rollout for Node that was already rolled out!");
        }
        int newScore;
        if (MCTS_Control_Parameters.MAST_ACTIVE) {
            newScore = SimulateNode.simulateMAST(gameState, this.mast);
        } else {
            newScore = SimulateNode.randomMoves(gameState);
        }
        this.backprop(newScore);
        return;
    }

    void backprop(int score) {
        this.score += score;
        this.timesVisited += 1;
        if (this.parent != null) {
            mast.update(this.moveToThis, score);
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
            bestUCT += calcProgressiveBias(isMax);

        } catch (NodeNotRolledOutException e) {
            return bestChild;
        }

        //Schleife über alle Werte
        for (MCTS_node node : this.children) {
            double uct;
            try {
                uct = node.calcUCT(isMax);
                uct += calcProgressiveBias(isMax);
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

    MCTS_node getBestChild(Board gameState) {

        MCTS_node best = this.children.getFirst();
        double weighted_best = best.normalizeScore();
        for (MCTS_node node : this.children) {
            double weighted_node = node.normalizeScore();
            if (gameState.sideToMove == Player.BLACK) {
                if (weighted_node > weighted_best) {
                    best = node;
                    weighted_best = weighted_node;
                }
            } else {
                if (weighted_node < weighted_best) {
                    best = node;
                    weighted_best = weighted_node;
                }
            }
        }
        return best;
    }

    public void aboveAverageChildren() {
        double meanVisited = ((double) this.timesVisited) / this.children.size();
        System.out.println("###### "+this.nodeToString()+" #######");
        for (MCTS_node child : this.children) {
            if (child.timesVisited > meanVisited) {
                System.out.println(" - "+child.nodeToString());
            }
        }
    }

    double normalizeScore() {
        double SCORE_BIAS = ((double) this.parent.timesVisited) / this.parent.children.size();
        double score = this.score * ((double) this.timesVisited) / (this.timesVisited + SCORE_BIAS);
        return score;
    }

    double calcProgressiveBias(boolean isMax) {
        double bias;
        // interval is currently [0,1] - needs adjustment for min player resulting in [-1, 0]
        if (isMax) {
            bias = this.progressiveBiasHeuristic;
        } else {
            bias = this.progressiveBiasHeuristic - 1;
        }
        if (MCTS_Control_Parameters.PROGRESSIVE_BIAS_ACTIVE) {
            return bias / (this.timesVisited + 1);
        } else {
            return 0;
        }
    }

    /* Expansion eines Knotens:
        der erste unexplorierte Move wird aus Liste entfernt und simuliert
        Falls Liste unexplorierte Moves leer ist, verliert der Knoten Terminal-Status
     */
    void expand(Board gameState) {
        Move move = this.movesForExpansion.removeFirst();
        gameState.makeMove(move);
        MCTS_node newChild = new MCTS_node(this, move, gameState, this.mast);
        this.children.add(newChild);
        if (movesForExpansion.isEmpty()) {
            this.isTerminal = false;
        }
        newChild.rollout(gameState);
    }

    MCTS_node findChildWithMove(Move move) throws ChildNotFoundException {
        for (MCTS_node node : this.children) {
            if (node.moveToThis.equals(move)) {
                return node;
            }
        }
        throw new ChildNotFoundException("No fitting Child found");
    }

    public String nodeToString() {
        String out = "[ " +
                "w = " + this.score +
                ", n = " + this.timesVisited +
                ", moveToThis = " + this.moveToThis +
                " ]";
        return out;
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
