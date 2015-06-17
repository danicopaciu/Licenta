package com.springapp.mvc.model.abc.fitness;

import com.springapp.mvc.controller.CloudController;
import com.springapp.mvc.model.abc.FoodSource;
import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import org.cloudbus.cloudsim.Vm;

import java.util.List;

public class LatencyPenalty implements Penalty {

    @Override
    public double getPenalty(FoodSource foodSource, GreenDataCenter dc) {
        double penalty;
        double maxPenalty = 0;
        double ram;
        List<GreenHost> hostList = dc.getHostList();
        for (GreenHost host : hostList) {
            ram = 0;
            List<Vm> assignedVms = foodSource.getVmListForHost(host);
            if (assignedVms != null) {
                for (Vm vm : assignedVms) {
                    ram += vm.getRam();
                }
            }
            if (CloudController.SIMULATION_TYPE != CloudController.LOW_LATENCY_VARIATION_SIMULATION) {
                penalty = ram / host.getAvailableBandwidth();
            } else {
                penalty = ram / 2000;
            }
            if (penalty > maxPenalty) {
                maxPenalty = penalty;
            }
        }
        return maxPenalty;
    }
}
