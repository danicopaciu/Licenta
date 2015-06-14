package com.springapp.mvc.model.abc.fitness;

/**
 * Created by Daniel on 6/14/2015.
 */
public class FitnessFactory {

    public static FitnessFunction getFitnessFunction(int simType) {
        switch (simType) {
            case 1:
                return new ConsumedEnergyVariationFitnessFunction();
            case 2:
                return new HeatVariationFitnessFunction();
            case 3:
                return new LatencyVariationFitnessFunction();
            case 4:
                return new LatencyVariationFitnessFunction();
            case 5:
                return new EnergyCostVariationFitnessFunction();
            case 6:
                return new EnergyCostVariationFitnessFunction();
            default:
                return null;
        }
    }
}
