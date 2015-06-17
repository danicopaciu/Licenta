package com.springapp.mvc.model.abc.fitness;

public class ConsumedEnergyVariationFitnessFunction implements FitnessFunction {


    private static final int HEAT_FACTOR = 0;
    private static final int ENERGY_COST = 1;

    @Override
    public double computeFitness(double greenEnergy, double consumedEnergy, double heat, double cooling, double penalty) {
        return (((consumedEnergy + cooling) * ENERGY_COST) /
                (greenEnergy + HEAT_FACTOR * heat)) - penalty;
    }
}
