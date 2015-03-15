package com.springapp.mvc.model.abc;

import java.util.List;

/**
 * Created by Daniel on 3/14/2015.
 */
public class OnlookerBee extends Bee {

    public OnlookerBee(int id, FoodSource foodSource) {
        super(id, foodSource);
    }

    public double computeProbability(List<FoodSource> foodSourceList){
        double fitnessSum = 0;
        for(FoodSource fs : foodSourceList){
            double fitnessValue = fs.getFitness();
            fitnessSum += fitnessValue;
        }
        FoodSource thisFoodSource = getFoodSource();
        double thisFitnessValue = thisFoodSource.getFitness();
        double thisProbability = thisFitnessValue / fitnessSum;
        thisFoodSource.setProbability(thisProbability);
        return thisProbability;
    }
}
