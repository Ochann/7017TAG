package pandemic;

import core.Game;

import java.util.HashSet;

public class PandemicGame extends Game {

    public PandemicGame() {
        gameState = new PandemicGameState();
        forwardModel = new PandemicForwardModel();
        // TODO: load data from files
        forwardModel.setup(gameState);
    }

    @Override
    public void run() {
        while (!isEnded()) {

            // Get actions of current active player for their turn
            int activePlayer = gameState.getActivePlayer();
            int[] actions = players.get(activePlayer).getActions(gameState);

            // Resolve actions and game rules for the turn
            forwardModel.next(gameState, actions);

            // It's next player's turn!
            ((PandemicGameState)gameState).setActivePlayer((activePlayer+1) % players.size());

            // TODO: GUI
        }

        System.out.println("Game Over");
        System.out.println(winners().toString());
    }

    @Override
    public boolean isEnded() {
        // TODO: check if game is over
        return false;
    }

    @Override
    public HashSet<Integer> winners() {
        // TODO: all or nothing, check gamestate
        return null;
    }
}
