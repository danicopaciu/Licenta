package com.springapp.mvc.model.abc.initializer;

import com.springapp.mvc.model.abc.FoodSource;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

import java.util.List;

public interface SolutionInitializer {

    public List<FoodSource> getSolutions(List<Vm> migratedVms, List<Host> hostList, int solutionNumber);

}
