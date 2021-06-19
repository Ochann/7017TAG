package games.dicemonastery.heuristics;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.actions.AbstractAction;
import games.dicemonastery.DiceMonasteryGameState;
import games.dicemonastery.DiceMonasteryStateAttributes;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class Advantage003 extends AbstractPlayer {

    Random rnd = new Random(System.currentTimeMillis());

    private double RND_WEIGHT;

    double[][] coefficients = new double[300][20];
    Map<Integer, Integer> hashToRowIndex = new HashMap<>();
    List<DiceMonasteryStateAttributes> features;


    public Advantage003() {
        this("Advantage003.csv");
    }
    public Advantage003(String filename) {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String weight = reader.readLine();
            RND_WEIGHT = Double.parseDouble(weight);
            List<String> header = Arrays.asList(reader.readLine().split(","));
            // assume the first two columns are the Hash and Intercept
            features = header.subList(2, header.size()).stream().map(DiceMonasteryStateAttributes::valueOf).collect(toList());

            //   List<List<Double>> data = new ArrayList<>();
            String nextLine = reader.readLine();
            int counter = 0;
            while (nextLine != null) {
                List<Double> data = Arrays.stream(nextLine.split(",")).map(Double::valueOf).collect(toList());

                int hash = data.get(0).intValue();
                hashToRowIndex.put(hash, counter);
                coefficients[counter] = data.subList(1, data.size()).stream().mapToDouble(Double::valueOf).toArray();
                counter++;
                nextLine = reader.readLine();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public AbstractAction getAction(AbstractGameState gameState, List<AbstractAction> possibleActions) {

        if (possibleActions.size() == 1)
            return possibleActions.get(0);

        DiceMonasteryGameState state = (DiceMonasteryGameState) gameState;
        int player = state.getCurrentPlayer();

        // first we calculate each feature for the state
        double[] featureVal = features.stream().mapToDouble(f -> {
            Object obj = f.get(state, player);
            if (obj instanceof Number) return ((Number) obj).doubleValue();
            return 0.0;
        }).toArray();


        double bestValue = Double.NEGATIVE_INFINITY;
        AbstractAction retValue = possibleActions.get(0);
        for (AbstractAction action : possibleActions) {
            int hash = action.hashCode();
            double actionValue = 0.0;
            if (hashToRowIndex.containsKey(hash)) {
                double[] coeffs = coefficients[hashToRowIndex.get(action.hashCode())];
                actionValue = coeffs[0]; // the intercept
                for (int i = 1; i <= features.size(); i++) {
                    actionValue += coeffs[i] * featureVal[i - 1];
                }
            }
            actionValue += rnd.nextDouble() * RND_WEIGHT;
            if (actionValue > bestValue) {
                retValue = action;
                bestValue = actionValue;
            }
        }
        return retValue;
    }
}
