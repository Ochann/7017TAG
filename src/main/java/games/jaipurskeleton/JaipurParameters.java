package games.jaipurskeleton;

import core.AbstractGameState;
import core.AbstractParameters;
import core.Game;
import evaluation.optimisation.TunableParameters;
import games.GameType;
import games.jaipurskeleton.components.JaipurCard;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>This class should hold a series of variables representing game parameters (e.g. number of cards dealt to players,
 * maximum number of rounds in the game etc.). These parameters should be used everywhere in the code instead of
 * local variables or hard-coded numbers, by accessing these parameters from the game state via {@link AbstractGameState#getGameParameters()}.</p>
 *
 * <p>It should then implement appropriate {@link #_copy()}, {@link #_equals(Object)} and {@link #hashCode()} functions.</p>
 *
 * <p>The class can optionally extend from {@link evaluation.optimisation.TunableParameters} instead, which allows to use
 * automatic game parameter optimisation tools in the framework.</p>
 */
//public class JaipurParameters extends AbstractParameters {
public class JaipurParameters extends TunableParameters {
    Map<JaipurCard.GoodType, Integer> goodNCardsMinimumSell = new HashMap<JaipurCard.GoodType, Integer>() {{
        put(JaipurCard.GoodType.Diamonds, 2);
        put(JaipurCard.GoodType.Gold, 2);
        put(JaipurCard.GoodType.Silver, 2);
        put(JaipurCard.GoodType.Cloth, 1);
        put(JaipurCard.GoodType.Spice, 1);
        put(JaipurCard.GoodType.Leather, 1);
        put(JaipurCard.GoodType.Magic, 8); // can not sell magic card only
    }};
    Map<Integer, Integer[]> bonusTokensAvailable = new HashMap<Integer, Integer[]>() {{
        put(3, new Integer[]{1,1,2,2,2,3,3});
        put(4, new Integer[]{4,4,5,5,6,6});
        put(5, new Integer[]{8,8,9,10,10});
    }};

    int nPointsMostCamels = 5;
    int nGoodTokensEmptyRoundEnd = 3;


    // for new parameters
    int nRoundsWinForGameWin = 2;
    Map<JaipurCard.GoodType, Integer[]> goodTokensProgression = new HashMap <JaipurCard.GoodType, Integer[]>() {{
        put ( JaipurCard.GoodType.Diamonds, new Integer []{5 ,5 ,5 ,7 ,7}) ;
        put ( JaipurCard.GoodType.Gold, new Integer []{5 ,5 ,5 ,6 ,6}) ;
        put ( JaipurCard.GoodType.Silver, new Integer []{5 ,5 ,5 ,5 ,5}) ;
        put ( JaipurCard.GoodType.Cloth, new Integer []{1 ,1 ,2 ,2 ,3 ,3 ,5}) ;
        put ( JaipurCard.GoodType.Spice, new Integer []{1 ,1 ,2 ,2 ,3 ,3 ,5}) ;
        put ( JaipurCard.GoodType.Leather, new Integer []{1 ,1 ,1 ,1 ,1 ,1 ,2 ,3 ,4}) ;
    }};
    // 5 more parameters
    int nPlayerHandLimit = 7;
    int nInitialCardsInHand = 5;
    int nMaximumCardsInMarket = 5;
    int nInitialCamelInMarket = 3;
    int nMaximumCamelInGame = 11;

    // for customized rules
    boolean ifCustomized = true;

    public JaipurParameters() {
        super();
        // add tunable params
        addTunableParameter("nPointsMostCamels", nPointsMostCamels, Arrays.asList(0, 2, 5, 7, 10));
        for(JaipurCard.GoodType gt: goodNCardsMinimumSell.keySet()) {
            addTunableParameter(gt.name() + "minSell", goodNCardsMinimumSell.get(gt), Arrays.asList(1, 2, 3, 4, 5));
        }
        // TODO: add the rest of params as tunable params(ignore goodTokensProgression and bonusTokensAvailable
        addTunableParameter("nGoodTokensEmptyRoundEnd", nGoodTokensEmptyRoundEnd, Arrays.asList(2, 3, 4, 5));
        addTunableParameter("nRoundsWinForGameWin", nRoundsWinForGameWin, Arrays.asList(2, 3));
        addTunableParameter("nPlayerHandLimit", nPlayerHandLimit, Arrays.asList(6, 7, 8, 9, 10));
        addTunableParameter("nInitialCardsInHand", nInitialCardsInHand, Arrays.asList(4, 5, 6));
        addTunableParameter("nMaximumCardsInMarket", nMaximumCardsInMarket, Arrays.asList(5, 6, 7, 8));
        addTunableParameter("nInitialCamelInMarket", nInitialCamelInMarket, Arrays.asList(2, 3, 4, 5));
        addTunableParameter("nMaximumCamelInGame", nMaximumCamelInGame, Arrays.asList(10, 11, 12, 13, 14));
        addTunableParameter("ifCustomized", ifCustomized, Arrays.asList(true, false));
    }


    // Copy constructor
    private JaipurParameters(JaipurParameters jaipurParameters) {
        super();

        this.goodNCardsMinimumSell = new HashMap<>(jaipurParameters.getGoodNCardsMinimumSell());
        this.bonusTokensAvailable = new HashMap<>();
        for (int n: jaipurParameters.getBonusTokensAvailable().keySet()) {
            this.bonusTokensAvailable.put(n, jaipurParameters.getBonusTokensAvailable().get(n).clone());
        }
        this.nPointsMostCamels = jaipurParameters.getNPointsMostCamels();
        this.nGoodTokensEmptyRoundEnd = jaipurParameters.getNGoodTokensEmptyGameEnd();
        // for new params
        this.nRoundsWinForGameWin = jaipurParameters.getNRoundsWinForGameWin();
        this.goodTokensProgression = new HashMap<>() ;
        for (JaipurCard.GoodType gt : jaipurParameters.getGoodTokensProgression().keySet()){
            this.goodTokensProgression.put(gt, jaipurParameters.getGoodTokensProgression().get(gt).clone());
        }
        this.nPlayerHandLimit = jaipurParameters.getNPlayerHandLimit();
        this.nInitialCardsInHand = jaipurParameters.getNInitialCardsInHand();
        this.nMaximumCardsInMarket = jaipurParameters.getNMaximumCardsInMarket();
        this.nInitialCamelInMarket = jaipurParameters.getNInitialCamelInMarket();
        this.nMaximumCamelInGame = jaipurParameters.getNMaximumCamelInGame();
        this.ifCustomized = jaipurParameters.getIfCustomized();
    }

    public Map<JaipurCard.GoodType, Integer> getGoodNCardsMinimumSell() {
        return goodNCardsMinimumSell;
    }

    public Map<Integer, Integer[]> getBonusTokensAvailable() {
        return bonusTokensAvailable;
    }

    public int getNPointsMostCamels() {
        return nPointsMostCamels;
    }

    public int getNGoodTokensEmptyGameEnd() { return nGoodTokensEmptyRoundEnd; }

    // new functions for new parameters
    public int getNRoundsWinForGameWin() { return nRoundsWinForGameWin; }
    public Map<JaipurCard.GoodType, Integer[]> getGoodTokensProgression() { return goodTokensProgression; }

    public int getNPlayerHandLimit() { return nPlayerHandLimit; }

    public int getNInitialCardsInHand() { return nInitialCardsInHand; }

    public int getNMaximumCardsInMarket() { return nMaximumCardsInMarket; }

    public int getNInitialCamelInMarket() { return nInitialCamelInMarket; }

    public int getNMaximumCamelInGame() { return nMaximumCamelInGame; }

    public boolean getIfCustomized() { return ifCustomized; }


    @Override
    protected AbstractParameters _copy() {
        return new JaipurParameters(this);
    }

    @Override
    public boolean _equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JaipurParameters that)) return false;
        if (!super.equals(o)) return false;
        return ifCustomized == that.ifCustomized && nMaximumCamelInGame == that.nMaximumCamelInGame && nInitialCamelInMarket == that.nInitialCamelInMarket && nMaximumCardsInMarket == that.nMaximumCardsInMarket && nInitialCardsInHand == that.nInitialCardsInHand && nPlayerHandLimit == that.nPlayerHandLimit && Objects.equals(goodTokensProgression, that.goodTokensProgression) && nRoundsWinForGameWin == that.nRoundsWinForGameWin && nPointsMostCamels == that.nPointsMostCamels && nGoodTokensEmptyRoundEnd == that.nGoodTokensEmptyRoundEnd && Objects.equals(goodNCardsMinimumSell, that.goodNCardsMinimumSell) && Objects.equals(bonusTokensAvailable, that.bonusTokensAvailable);
    }

    @Override
    public void _reset() {
        System.out.println("------------reset jaipurparams");
        nPointsMostCamels = (int) getParameterValue("nPointsMostCamels");
        goodNCardsMinimumSell.replaceAll((gt, v)->(Integer) getParameterValue(gt.name() + "minSell"));
        // TODO: add the rest of params as tunable params(ignore goodTokensProgression and bonusTokensAvailable
        nGoodTokensEmptyRoundEnd = (int) getParameterValue("nGoodTokensEmptyRoundEnd");
        nRoundsWinForGameWin = (int) getParameterValue("nRoundsWinForGameWin");
        nPlayerHandLimit = (int) getParameterValue("nPlayerHandLimit");
        nInitialCardsInHand = (int) getParameterValue("nInitialCardsInHand");
        nMaximumCardsInMarket = (int) getParameterValue("nMaximumCardsInMarket");
        nInitialCamelInMarket = (int) getParameterValue("nInitialCamelInMarket");
        nMaximumCamelInGame = (int) getParameterValue("nMaximumCamelInGame");
        ifCustomized = (boolean) getParameterValue("ifCustomized");
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ifCustomized, nInitialCamelInMarket, nMaximumCardsInMarket, nInitialCardsInHand, nPlayerHandLimit, goodTokensProgression, goodNCardsMinimumSell, bonusTokensAvailable, nPointsMostCamels, nGoodTokensEmptyRoundEnd);
    }

    @Override
    public Object instantiate() {
        return new Game(GameType.Jaipur, new JaipurForwardModel(), new JaipurGameState(this, GameType.Jaipur.getMinPlayers()));
    }

    // new function to print out the params configurations used in various experiments

    @Override
    public String toString() {
        return "JaipurParameters{" +
                "goodNCardsMinimumSell=" + goodNCardsMinimumSell +
                ", bonusTokensAvailable=" + bonusTokensAvailable +
                ", nPointsMostCamels=" + nPointsMostCamels +
                ", nGoodTokensEmptyRoundEnd=" + nGoodTokensEmptyRoundEnd +
                ", nRoundsWinForGameWin=" + nRoundsWinForGameWin +
                ", goodTokensProgression=" + goodTokensProgression +
                ", nPlayerHandLimit=" + nPlayerHandLimit +
                ", nInitialCardsInHand=" + nInitialCardsInHand +
                ", nMaximumCardsInMarket=" + nMaximumCardsInMarket +
                ", nInitialCamelInMarket=" + nInitialCamelInMarket +
                ", nMaximumCamelInGame=" + nMaximumCamelInGame +
                ", ifCustomized=" + ifCustomized +
                '}';
    }
}
