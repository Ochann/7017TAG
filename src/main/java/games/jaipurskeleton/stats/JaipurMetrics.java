package games.jaipurskeleton.stats;

import core.interfaces.IGameEvent;
import evaluation.listeners.MetricsGameListener;
import evaluation.metrics.AbstractMetric;
import evaluation.metrics.Event;
import games.jaipurskeleton.JaipurGameState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class JaipurMetrics {
}

class RoundScoreDifference extends AbstractMetric {

    @Override
    protected boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
        JaipurGameState gs = (JaipurGameState) e.state;
        double scoreDiff = 0;
        for(int i=0; i<gs.getNPlayers()-1; i++){
            scoreDiff += Math.abs(gs.getPlayerScores().get(i).getValue() - gs.getPlayerScores().get(i+1).getValue());
        }
        scoreDiff /= (gs.getNPlayers() - 1);
        return true;
    }

    @Override
    public Set<IGameEvent> getDefaultEventTypes() {
        return Collections.singleton(Event.GameEvent.ROUND_OVER);
    }

    @Override
    public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
        Map<String, Class<?>> columns = new HashMap<>();
        columns.put("ScoreDiff", Double.class);
        return columns;
    }
}
