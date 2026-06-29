package de.tuberlin.tablut.ai.SearchAlgorithms.MCTS;

import de.tuberlin.tablut.ai.Move;

import java.util.HashMap;
import java.util.Map;

public class MAST {
    Map<Move, MAST_ENTRY> mast = new HashMap<>();

    private MAST_ENTRY get(Move move){
        return mast.get(move);
    }

    public int getScore(Move move){
        if(mast.containsKey(move)){
            return mast.get(move).mean_reward;
        }else{
            return 0;
        }
    }

    public void update(Move move, int reward){
        if(mast.containsKey(move)){
            mast.get(move).update(reward);
        }else{
            mast.put(move, new MAST_ENTRY(move, reward));
        }
    }
}
