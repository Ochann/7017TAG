package games.jaipurskeleton.stats;

import core.actions.AbstractAction;
import core.interfaces.IGameEvent;
import evaluation.listeners.MetricsGameListener;
import evaluation.metrics.AbstractMetric;
import evaluation.metrics.Event;
import evaluation.metrics.IMetricsCollection;
import games.jaipurskeleton.JaipurGameState;
import games.jaipurskeleton.actions.TakeCards;
import games.jaipurskeleton.components.JaipurCard;

import java.util.*;

public class JaipurMetrics implements IMetricsCollection {
    static public class RoundScoreDifference extends AbstractMetric {
        public RoundScoreDifference() {
            super();
        }

        @Override
        protected boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
            JaipurGameState gs = (JaipurGameState) e.state;
            double scoreDiff = 0;
            for(int i=0; i<gs.getNPlayers()-1; i++){
                scoreDiff += Math.abs(gs.getPlayerScores().get(i).getValue() - gs.getPlayerScores().get(i+1).getValue());
            }
            scoreDiff /= (gs.getNPlayers() - 1);
            records.put("ScoreDiff", scoreDiff);
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

    static public class PurchaseFromMarket extends AbstractMetric {
        JaipurCard.GoodType[] goodTypes;

        public PurchaseFromMarket(){
            super();
            goodTypes = JaipurCard.GoodType.values();
        }

        public PurchaseFromMarket(String[] args){
            super(args);
            goodTypes = new JaipurCard.GoodType[args.length];
            for(int i=0; i< args.length; i++) {
                goodTypes[i] = JaipurCard.GoodType.valueOf(args[i]);
            }
        }

        @Override
        protected boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
            AbstractAction action = e.action;
            if(action instanceof TakeCards tc) {
                for(JaipurCard.GoodType type : goodTypes) {
                    if(tc.howManyPerTypeTakeFromMarket.containsKey(type)) {
                        records.put("Purchase-" + type.name(), tc.howManyPerTypeTakeFromMarket.get(type));
                        // add another record to column Purchase ( the type of good purchased from the market
                        records.put("Purchase", type.name());
                    }
                }

                return true;
            }
            return false;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return Collections.singleton(Event.GameEvent.ACTION_CHOSEN);
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            for(JaipurCard.GoodType type : goodTypes) {
                columns.put("Purchase-"+type.name(), Integer.class);
            }
            // add another column ( the type of good purchased from the market
            columns.put("Purchase", String.class);
            return columns;
        }
    }

    // TODO: Exercise - add two more metric classes
    // new metric 1: if the player going first wins more games?
    static public class WinGamesFirstPlayer extends AbstractMetric {
        public WinGamesFirstPlayer() {
            super();
        }

        @Override
        protected boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
            JaipurGameState gs = (JaipurGameState) e.state;
            int winGames = 0;
            if(gs.getWinners().contains(gs.getFirstPlayer())) winGames += 1;
            //System.out.println("Winner: ");
            //System.out.println(gs.getWinners());
            //System.out.println("FirstPlayer: ");
            //System.out.println(gs.getFirstPlayer());
            records.put("WinGames", winGames);
            return true;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return Collections.singleton(Event.GameEvent.GAME_OVER);
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            columns.put("WinGames", Integer.class);
            return columns;
        }
    }

    // new metric 2: if the player prefer camels wins more rounds?
    static public class WinRoundsWithMoreCamels extends  AbstractMetric {
        public  WinRoundsWithMoreCamels() { super(); }

        @Override
        protected boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
            JaipurGameState gs = (JaipurGameState) e.state;
            int winRound = 0;
            int maxCamels = 0;
            int roundWinner = 0;
            HashSet<Integer> pIdMaxCamels = new HashSet<>();
            for (int i = 0; i < gs.getNPlayers(); i++) {
                int camels = gs.getPlayerHerds().get(i).getValue();
                //System.out.println("The herds of Player" + Integer.toString(i) + ": "+ Integer.toString(camels) + "camels");
                if (camels > maxCamels) {
                    maxCamels = gs.getPlayerHerds().get(i).getValue();
                    pIdMaxCamels.clear();
                    pIdMaxCamels.add(i);
                } else if (gs.getPlayerHerds().get(i).getValue() == maxCamels) {
                    pIdMaxCamels.add(i);
                }
                if(gs.getOrdinalPosition(i) == 1) roundWinner = i;
            }
            //if (pIdMaxCamels.size() == 1 && gs.getWinners().contains(pIdMaxCamels.iterator().next())) {
            //    winRound += 1;
            //}
            if (pIdMaxCamels.size() == 1 && roundWinner == pIdMaxCamels.iterator().next()) {
                winRound += 1;
            }
            //System.out.println("Player with max camels: " + Integer.toString(pIdMaxCamels.iterator().next()));
            //System.out.println("Player 0 ordinal pos: " + Integer.toString(gs.getOrdinalPosition(0)));
            //System.out.println("Player 1 ordinal pos: " + Integer.toString(gs.getOrdinalPosition(1)));
            //System.out.println("Round Winner: " + Integer.toString(roundWinner));
            //System.out.println("If player with more camels win this round: " + Integer.toString(winRound));
            //System.out.println("--------------------------------------------");

            records.put("winRound", winRound);
            return true;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return Collections.singleton(Event.GameEvent.ROUND_OVER);
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            columns.put("winRound", Integer.class);
            return columns;
        }
    }
}



