package de.tuberlin.tablut.ai.SearchAlgorithms.MCTS;

import de.tuberlin.tablut.ai.Move;

import java.util.HashMap;
import java.util.Map;

public class MAST {
    Map<Move, MAST_ENTRY> mast = new HashMap<>();

    private MAST_ENTRY get(Move move){
        return mast.get(move);
    }

    public double getScore(Move move){
        if(mast.containsKey(move)){
            return mast.get(move).mean_reward;
        }else{
            return 0;
        }
    }

    public void update(Move move, int reward){
        MAST_ENTRY entry = mast.get(move);
        if(entry != null){
            entry.update(reward);
        }else{
            mast.put(move, new MAST_ENTRY(move, reward));
        }
    }
}
