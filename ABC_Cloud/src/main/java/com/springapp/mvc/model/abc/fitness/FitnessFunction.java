package com.springapp.mvc.model.abc.fitness;

/**
 * Created by Daniel on 6/14/2015.
 */
public interface FitnessFunction {

    public double computeFitness(double greenEnergy,
                                 double consumedEnergy,
                                 double heat,
                                 double cooling,
                                 double penalty);
}
