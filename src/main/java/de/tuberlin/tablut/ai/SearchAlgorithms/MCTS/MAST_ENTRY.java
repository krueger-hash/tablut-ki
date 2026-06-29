package de.tuberlin.tablut.ai.SearchAlgorithms.MCTS;

import de.tuberlin.tablut.ai.Move;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MAST_ENTRY {
    Move action;
    int mean_reward;
    int n_played;

    public MAST_ENTRY(Move action, int reward) {
        this.action = action;
        this.mean_reward = reward;
        this.n_played = 1;
    }

    public void update(int reward) {
        // ++n_played because a move has been played one more time
        mean_reward = mean_reward + (reward - mean_reward) / (++n_played);
    }

}
