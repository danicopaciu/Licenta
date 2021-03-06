package com.springapp.mvc.model.cloud;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerVm;

import java.util.List;

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
        double hostUtilisedMips = host.getUtilisedMips();
        double hostEnergyMean = host.getMeanPower();

//        double hostEnergyLast = host.getLastEnergy();
//        double hostPredictedEnergy = host.getPredictedEnergy();
//        if (hostPredictedEnergy == 0){
//            hostPredictedEnergy = host.getMeanPower();
//        }

        if (hostUtilisedMips != 0) {
            return hostEnergyMean * getUtilizationMean() / hostUtilisedMips;
        }
        return 0;
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
        if (currentTime > getPreviousTime() && (currentTime - FederationOfDataCenter.dataCenterAllocationDelay) % getSchedulingInterval() == 0) {
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
