package com.springapp.mvc.model.cloud;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.power.PowerVm;

/**
 * Created by Daniel on 3/12/2015.
 */
public class GreenVm extends PowerVm {


    public GreenVm(int id, int userId, double mips, int pesNumber, int ram, long bw,
                   long size, int priority, String vmm, CloudletScheduler cloudletScheduler,
                   double schedulingInterval) {
        super(id, userId, mips, pesNumber, ram, bw, size, priority, vmm, cloudletScheduler, schedulingInterval);
    }

    public double getNecessaryEnergy() {
        return getUtilizationMean();
    }
}
