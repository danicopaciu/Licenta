package com.springapp.mvc.model.abc.fitness;

import com.springapp.mvc.model.abc.FoodSource;
import com.springapp.mvc.model.cloud.GreenDataCenter;

/**
 * Created by Daniel on 6/14/2015.
 */
public interface Penalty {
    public double getPenalty(FoodSource foodSource, GreenDataCenter dc);
}
