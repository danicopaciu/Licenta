package com.springapp.mvc.model.abc.fitness;

public interface FitnessFunction {

    double computeFitness(double greenEnergy,
                          double consumedEnergy,
                          double heat,
                          double cooling,
                          double penalty);
}
