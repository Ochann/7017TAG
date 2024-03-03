package games.jaipurskeleton;

import com.google.common.collect.ImmutableMap;
import core.AbstractGameState;
import core.CoreConstants;
import core.StandardForwardModel;
import core.actions.AbstractAction;
import core.components.Counter;
import core.components.Deck;
import games.jaipurskeleton.actions.SellCards;
import games.jaipurskeleton.actions.TakeCards;
import games.jaipurskeleton.components.JaipurCard;
import games.jaipurskeleton.components.JaipurToken;

import java.util.*;

import static core.CoreConstants.GameResult.*;
import static games.jaipurskeleton.components.JaipurCard.GoodType.*;

/**
 * Jaipur rules: <a href="https://www.fgbradleys.com/rules/rules2/Jaipur-rules.pdf">pdf here</a>
 */
public class JaipurForwardModel extends StandardForwardModel {

    /**
     * Initializes all variables in the given game state. Performs initial game setup according to game rules, e.g.:
     * <ul>
     *     <li>Sets up decks of cards and shuffles them</li>
     *     <li>Gives player cards</li>
     *     <li>Places tokens on boards</li>
     *     <li>...</li>
     * </ul>
     *
     * @param firstState - the state to be modified to the initial game state.
     */
    @Override
    protected void _setup(AbstractGameState firstState) {
        JaipurGameState gs = (JaipurGameState) firstState;
        JaipurParameters jp = (JaipurParameters) firstState.getGameParameters();

        // Initialize variables
        gs.market = new HashMap<>();
        for (JaipurCard.GoodType gt: JaipurCard.GoodType.values()) {
            // 5 cards in the market
            gs.market.put(gt, new Counter(0, 0, jp.nMaximumCardsInMarket, "Market: " + gt));
        }

        gs.drawDeck = new Deck<>("Draw deck", CoreConstants.VisibilityMode.HIDDEN_TO_ALL);
        gs.playerHands = new ArrayList<>();
        gs.playerHerds = new ArrayList<>();
        gs.nGoodTokensSold = new Counter(0, 0, JaipurCard.GoodType.values().length, "N Good Tokens Fully Sold");
        gs.goodTokens = new HashMap<>();
        gs.bonusTokens = new HashMap<>();

        // Initialize player scores, rounds won trackers, and other player-specific variables
        gs.playerScores = new ArrayList<>();
        gs.playerNRoundsWon = new ArrayList<>();
        gs.playerNGoodTokens = new ArrayList<>();
        gs.playerNBonusTokens = new ArrayList<>();
        for (int i = 0; i < gs.getNPlayers(); i++) {
            gs.playerScores.add(new Counter(0, 0, Integer.MAX_VALUE, "Player " + i + " score"));
            gs.playerNRoundsWon.add(new Counter(0, 0, Integer.MAX_VALUE, "Player " + i + " n rounds won"));
            gs.playerNGoodTokens.add(new Counter(0, 0, Integer.MAX_VALUE, "Player " + i + " n good tokens"));
            gs.playerNBonusTokens.add(new Counter(0, 0, Integer.MAX_VALUE, "Player " + i + " n bonus tokens"));

            // Create herds, maximum 11 camels in the game
            gs.playerHerds.add(new Counter(0, 0, jp.nMaximumCamelInGame, "Player " + i + " herd"));

            Map<JaipurCard.GoodType, Counter> playerHand = new HashMap<>();
            for (JaipurCard.GoodType gt: JaipurCard.GoodType.values()) {
                if (gt != JaipurCard.GoodType.Camel) {
                    // Hand limit of 7
                    playerHand.put(gt, new Counter(0, 0, jp.nPlayerHandLimit, "Player " + i + " hand: " + gt));
                }
            }
            gs.playerHands.add(playerHand);
        }

        // Set up the first round
        setupRound(gs, jp);
    }

    private void setupRound(JaipurGameState gs, JaipurParameters jp) {
        Random r = new Random(jp.getRandomSeed());

        // Market initialisation
        // Place 3 camel cards in the market
        for (JaipurCard.GoodType gt: JaipurCard.GoodType.values()) {
            if (gt == JaipurCard.GoodType.Camel) {
                gs.market.get(gt).setValue(jp.nInitialCamelInMarket);
            } else {
                gs.market.get(gt).setValue(0);
            }
        }

        // Create deck of cards
        gs.drawDeck.clear();
        // new all-purpose cards
        if(jp.ifCustomized) {
            for (int i = 0; i < 6; i++) {  // 6 All-purpose cards
                JaipurCard card = new JaipurCard(Magic);
                gs.drawDeck.add(card);
            }
        }
        for (int i = 0; i < 6; i++) {  // 6 Diamond cards
            JaipurCard card = new JaipurCard(Diamonds);
            gs.drawDeck.add(card);
        }
        for (int i = 0; i < 6; i++) {  // 6 Gold cards
            JaipurCard card = new JaipurCard(Gold);
            gs.drawDeck.add(card);
        }
        for (int i = 0; i < 6; i++) {  // 6 Silver cards
            JaipurCard card = new JaipurCard(Silver);
            gs.drawDeck.add(card);
        }
        for (int i = 0; i < 8; i++) {  // 8 Cloth cards
            JaipurCard card = new JaipurCard(JaipurCard.GoodType.Cloth);
            gs.drawDeck.add(card);
        }
        for (int i = 0; i < 8; i++) {  // 8 Spice cards
            JaipurCard card = new JaipurCard(JaipurCard.GoodType.Spice);
            gs.drawDeck.add(card);
        }
        for (int i = 0; i < 10; i++) {  // 10 Leather cards
            JaipurCard card = new JaipurCard(JaipurCard.GoodType.Leather);
            gs.drawDeck.add(card);
        }
        for (int i = 0; i < jp.nMaximumCamelInGame - jp.nInitialCamelInMarket; i++) {  // 11 Camel cards, - 3 already in the market
            JaipurCard card = new JaipurCard(JaipurCard.GoodType.Camel);
            gs.drawDeck.add(card);
        }
        gs.drawDeck.shuffle(r);

        // Deal N cards to each player
        for (int i = 0; i < gs.getNPlayers(); i++) {
            Map<JaipurCard.GoodType, Counter> playerHand = gs.playerHands.get(i);

            // First, reset
            gs.playerHerds.get(i).setValue(0);
            for (JaipurCard.GoodType gt: JaipurCard.GoodType.values()) {
                if (gt != JaipurCard.GoodType.Camel) {
                    playerHand.get(gt).setValue(0);
                }
            }

            // Deal cards
            for (int j = 0; j < jp.nInitialCardsInHand; j++) {  // 5 cards in hand
                JaipurCard card = gs.drawDeck.draw();

                // If camel, it goes into the herd instead
                if (card.goodType == JaipurCard.GoodType.Camel) {
                    gs.playerHerds.get(i).increment();
                } else {
                    // Otherwise, into the player's hand
                    playerHand.get(card.goodType).increment();
                }
            }
        }

        // Take first 2 cards from the deck and place them face up in the market.
        for (int i = 0; i < jp.nMaximumCardsInMarket-jp.nInitialCamelInMarket; i++) {
            JaipurCard card = gs.drawDeck.draw();
            gs.market.get(card.goodType).increment();
        }

        // Initialize tokens
        gs.nGoodTokensSold.setValue(0);
        gs.goodTokens.clear();
        gs.bonusTokens.clear();

        // Initialize the good tokens
        // 4.2
        for (JaipurCard.GoodType type: jp.goodTokensProgression.keySet()) {
            Integer[] progression = jp.goodTokensProgression.get(type) ;
            Deck<JaipurToken> tokenDeck = new Deck<>("Good tokens " + type, CoreConstants.VisibilityMode.VISIBLE_TO_ALL) ;
            for (int p: progression) {
                tokenDeck.add(new JaipurToken(type, p));
            }
            // Shuffle later?
            gs.goodTokens.put(type, tokenDeck);
        }

        // Initialize the bonus tokens
        for (int nSold: jp.bonusTokensAvailable.keySet()) {
            Integer[] values = jp.bonusTokensAvailable.get(nSold);
            Deck<JaipurToken> tokenDeck = new Deck<>("Bonus tokens " + nSold, CoreConstants.VisibilityMode.HIDDEN_TO_ALL);
            for (int v: values) {
                tokenDeck.add(new JaipurToken(v));
            }
            // Shuffle
            tokenDeck.shuffle(r);
            gs.bonusTokens.put(nSold, tokenDeck);
        }

        // Reset player-specific variables that don't persist between rounds
        for (int i = 0; i < gs.getNPlayers(); i++) {
            gs.playerScores.get(i).setValue(0);
            gs.playerNGoodTokens.get(i).setValue(0);
            gs.playerNBonusTokens.get(i).setValue(0);
        }

        // First player
        gs.setFirstPlayer(0);
    }

    /**
     * Calculates the list of currently available actions, possibly depending on the game phase.
     * @return - List of AbstractAction objects.
     */
    @Override
    protected List<AbstractAction> _computeAvailableActions(AbstractGameState gameState) {
        List<AbstractAction> actions = new ArrayList<>();
        JaipurGameState jgs = (JaipurGameState) gameState;
        JaipurParameters jp = (JaipurParameters) gameState.getGameParameters();
        int currentPlayer = gameState.getCurrentPlayer();
        Map<JaipurCard.GoodType, Counter> playerHand = jgs.playerHands.get(currentPlayer);

        System.out.println("-------------------------------");

        // Can sell cards from hand
        // Can sell cards with all-purpose cards
        // TODO: Follow lab 1 instructions (Section 3.1) to fill in this method here.
        for(JaipurCard.GoodType gt: playerHand.keySet()) {
            if(jp.ifCustomized) {
                if (gt != JaipurCard.GoodType.Magic) {
                    int minimumRequired = jp.getGoodNCardsMinimumSell().get(gt);
                    int inHand = playerHand.get(gt).getValue();
                    if(inHand > 0) {
                        int magicInHand = playerHand.get(JaipurCard.GoodType.Magic).getValue();
                        int allInHand = inHand + magicInHand;
                        if (allInHand >= minimumRequired) {
                            for (int i = minimumRequired; i <= inHand; i++) {
                                actions.add(new SellCards(gt, i, false, 0));
                            }
                            for (int i = 1; i <= magicInHand; i++) {
                                actions.add(new SellCards(gt, inHand + i, true, i));
                            }
                        }
                    }
                }
            }
            else {
                int minimumRequired = jp.getGoodNCardsMinimumSell().get(gt);
                int inHand = playerHand.get(gt).getValue();
                if (inHand >= minimumRequired) {
                    for (int i = minimumRequired; i <= inHand; i++) {
                        actions.add(new SellCards(gt, i, false, 0));
                    }
                }
            }
        }

        // Can take cards from the market, respecting hand limit
        // Option C: Take all camels, they don't count towards hand limit
        // Q: How to count towards hand limit?
        // TODO 1: Check how many camel cards are in the market. If more than 0,
        // TODO 1:construct one TakeCards action object and add it to the `actions` ArrayList. (The `howManyPerTypeGiveFromHand` argument should be null)
        if(jgs.getMarket().containsKey(JaipurCard.GoodType.Camel)) {
            int camelNum = jgs.getMarket().get(JaipurCard.GoodType.Camel).getValue();
            System.out.println("Camel num: " + camelNum);
            if (camelNum > 0) {
                actions.add(new TakeCards(ImmutableMap.of(JaipurCard.GoodType.Camel, camelNum), null, currentPlayer));
                System.out.println("Added take camel actions");
            }
        }

        int nCardsInHand = 0;
        for (JaipurCard.GoodType gt: playerHand.keySet()) {
            nCardsInHand += playerHand.get(gt).getValue();
        }
        System.out.println("Player hand: " + nCardsInHand);
        System.out.println("Player hand limit: " + jp.nPlayerHandLimit);

        // Check hand limit for taking non-camel cards in hand
        if (nCardsInHand < jp.nPlayerHandLimit) {
            // Option B: Take a single (non-camel) card from the market
            // TODO 2: For each good type in the market, if there is at least 1 of that type (which is not a Camel),
            // TODO 2: construct one TakeCards action object to take 1 of that type from the market, and add it to the `actions` ArrayList. (The `howManyPerTypeGiveFromHand` argument should be null)
            System.out.println("Take non-camel card...");
            for(JaipurCard.GoodType gt: jgs.getMarket().keySet()) {
                if(gt != JaipurCard.GoodType.Camel && jgs.getMarket().get(gt).getValue() > 0) {
                    actions.add(new TakeCards(ImmutableMap.of(gt, 1), null, currentPlayer));
                    System.out.println("Added take card action: " + gt);
                }
            }
        }

        // Option A: Take several (non-camel) cards and replenish with cards of different types from hand (or with camels)
        // TODO (Advanced, bonus, optional): Calculate legal option A variations

        return actions;
    }

    @Override
    protected void _afterAction(AbstractGameState currentState, AbstractAction actionTaken) {
        if (currentState.isActionInProgress()) return;

        // Check game end
        JaipurGameState jgs = (JaipurGameState) currentState;
        JaipurParameters jp = (JaipurParameters) currentState.getGameParameters();
        if (actionTaken instanceof TakeCards && ((TakeCards)actionTaken).isTriggerRoundEnd() || jgs.nGoodTokensSold.getValue() == jp.nGoodTokensEmptyRoundEnd) {
            // Round end!
            endRound(currentState);

            // Check most camels, add extra points
            int maxCamels = 0;
            HashSet<Integer> pIdMaxCamels = new HashSet<>();
            for (int i = 0; i < jgs.getNPlayers(); i++) {
                if (jgs.playerHerds.get(i).getValue() > maxCamels) {
                    maxCamels = jgs.playerHerds.get(i).getValue();
                    pIdMaxCamels.clear();
                    pIdMaxCamels.add(i);
                } else if (jgs.playerHerds.get(i).getValue() == maxCamels) {
                    pIdMaxCamels.add(i);
                }
            }
            if (pIdMaxCamels.size() == 1) {
                // Exactly 1 player has most camels, they get bonus. If tied, nobody gets bonus.
                int player = pIdMaxCamels.iterator().next();
                jgs.playerScores.get(player).increment(jp.nPointsMostCamels);
                if (jgs.getCoreGameParameters().recordEventHistory) {
                    jgs.recordHistory("Player " + player + " earns the " + jp.nPointsMostCamels + " Camel bonus points (" + maxCamels + " camels)");
                }
            }

            // Decide winner of round
            int roundsWon = 0;
            int winner = -1;
            StringBuilder scores = new StringBuilder();
            for (int p = 0; p < jgs.getNPlayers(); p++) {
                int o = jgs.getOrdinalPosition(p);
                scores.append(p).append(":").append(jgs.playerScores.get(p).getValue());
                if (o == 1) {
                    jgs.playerNRoundsWon.get(p).increment();
                    roundsWon = jgs.playerNRoundsWon.get(p).getValue();
                    winner = p;
                    scores.append(" (win)");
                }
                scores.append(", ");
            }
            scores.append(")");
            scores = new StringBuilder(scores.toString().replace(", )", ""));
            if (jgs.getCoreGameParameters().recordEventHistory) {
                jgs.recordHistory("Round scores: " + scores);
            }
            // TODO : add my own event to pass the last winner for this round , also remember to change in jaipurmetrics
            //jgs.logEvent();

            if (roundsWon == jp.nRoundsWinForGameWin) {
                // Game over, this player won
                jgs.setGameStatus(CoreConstants.GameResult.GAME_END);
                for (int i = 0; i < jgs.getNPlayers(); i++) {
                    if (i == winner) {
                        jgs.setPlayerResult(WIN_GAME, i);
                    } else {
                        jgs.setPlayerResult(LOSE_GAME, i);
                    }
                }
                return;
            }

            // Reset and set up for next round
            setupRound(jgs, jp);

        } else {
            // It's next player's turn
            endPlayerTurn(jgs);
        }
    }
}
