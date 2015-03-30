package com.springapp.mvc.model.cloud;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerVm;

import java.util.List;

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
        GreenHost host = (GreenHost) getHost();
        if (host == null) {
            return 0;
        }
        double[] hostHistory = host.getGreenUtilizationHistory();
        double hostHistoryMean = 0;
        for (double aHostHistory : hostHistory) {
            hostHistoryMean += aHostHistory;
        }
        hostHistoryMean /= hostHistory.length;
        double hostUtilisedMips = hostHistoryMean * host.getTotalMips();

        double hostEnergyMean = 0;
        for (double d : host.getEnergyHystory()) {
            hostEnergyMean += d;
        }
        hostEnergyMean /= host.getEnergyHystory().size();

        double energyVm = hostEnergyMean * getUtilizationMean() / hostUtilisedMips;

        return energyVm;
    }

    /**
     * Updates the processing of cloudlets running on this VM.
     *
     * @param currentTime current simulation time
     * @param mipsShare   array with MIPS share of each Pe available to the scheduler
     * @return time predicted completion time of the earliest finishing cloudlet, or 0 if there is
     * no next events
     * @pre currentTime >= 0
     * @post $none
     */
    @Override
    public double updateVmProcessing(final double currentTime, final List<Double> mipsShare) {
        double time = super.updateVmProcessing(currentTime, mipsShare);
        if (currentTime > getPreviousTime() && (currentTime - FederationOfDataCenter.allocatedDC) % getSchedulingInterval() == 0) {
            double utilization = getTotalUtilizationOfCpu(getCloudletScheduler().getPreviousTime());
            if (CloudSim.clock() != 0 || utilization != 0) {
                addUtilizationHistoryValue(utilization);
            }
            setPreviousTime(currentTime);
        }
        return time;
    }

    public List<Double> getUtilizationHistory() {
        return super.getUtilizationHistory();
    }


}
