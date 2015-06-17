package com.springapp.mvc.model.abc.fitness;

import com.springapp.mvc.model.abc.FoodSource;
import com.springapp.mvc.model.cloud.GreenDataCenter;

public interface Penalty {
    double getPenalty(FoodSource foodSource, GreenDataCenter dc);
}
