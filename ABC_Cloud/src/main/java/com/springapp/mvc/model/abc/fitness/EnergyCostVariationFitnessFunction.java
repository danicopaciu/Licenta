package com.springapp.mvc.model.abc.fitness;

import com.springapp.mvc.controller.CloudController;

/**
 * Created by Daniel on 6/14/2015.
 */
public class EnergyCostVariationFitnessFunction implements FitnessFunction {

    private static final int HEAT_FACTOR = 0;

    @Override
    public double computeFitness(double greenEnergy, double consumedEnergy, double heat, double cooling, double penalty) {
        double energyCost;
        switch (CloudController.SIMULATION_TYPE) {
            case CloudController.ENERGY_HIGH_COST_VARIATION_SIMULATION:
                energyCost = 20;
                break;
            case CloudController.ENERGY_LOW_COST_VARIATION_SIMULATION:
                energyCost = 0.2;
                break;
            default:
                energyCost = 1;
        }
        return (((consumedEnergy + cooling) * energyCost) /
                (greenEnergy + HEAT_FACTOR * heat)) - penalty;
    }
}
