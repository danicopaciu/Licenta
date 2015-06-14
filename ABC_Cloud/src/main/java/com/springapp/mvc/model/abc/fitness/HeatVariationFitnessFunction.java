package com.springapp.mvc.model.abc.fitness;

/**
 * Created by Daniel on 6/14/2015.
 */
public class HeatVariationFitnessFunction implements FitnessFunction {

    private static final int HEAT_FACTOR = 1;

    @Override
    public double computeFitness(double greenEnergy, double consumedEnergy,
                                 double heat, double cooling, double penalty) {
        return ((consumedEnergy + cooling) /
                (greenEnergy + HEAT_FACTOR * heat)) - penalty;
    }
}
