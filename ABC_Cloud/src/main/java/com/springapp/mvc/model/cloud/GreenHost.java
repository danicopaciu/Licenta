package com.springapp.mvc.model.cloud;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.ArrayList;
import java.util.List;

public class GreenHost extends PowerHostUtilizationHistory {


    private List<Double> energyHistory;


    public GreenHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner,
                     long storage, List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
        energyHistory = new ArrayList<Double>();
    }

    public synchronized void addEnergyHistory(Double d) {
        energyHistory.add(d);
    }

    public double getUtilizationMean() {
        double[] hostHistory = getUtilizationHistory();
        double hostHistoryMean = 0;
        for (double aHostHistory : hostHistory) {
            hostHistoryMean += aHostHistory;
        }
        hostHistoryMean /= hostHistory.length;
        return hostHistoryMean;
    }

    public double getUtilisedMips() {
        double hostHistoryMean = getUtilizationMean();
        double hostTotalMips = getTotalMips();
        return hostHistoryMean * hostTotalMips;
    }

    public synchronized List<Double> getEnergyHistory() {
        return energyHistory;
    }

    public double getAvailableBandwidth() {
        return getBwProvisioner().getAvailableBw();
    }

    public double getMeanPower() {
        double mean = 0;
        int size = 0;
        if (energyHistory != null) {
            if (!energyHistory.isEmpty()) {
                for (Double d : energyHistory) {
                    if (d > 0) {
                        mean += d;
                        size++;
                    }
                }
                if (size != 0) {
                    mean /= size;
                    return mean;
                }
            }
        }
        return 0;
    }


    public boolean isMigrationPossible(Vm vm, List<Vm> migratingInVms) {
        if (migratingInVms == null || migratingInVms.isEmpty()) {
            return isSuitableForVm(vm);
        } else {
            VmScheduler scheduler = getVmScheduler();
            double peCapacity = scheduler.getPeCapacity();
            double currentRequestedMaxMips = vm.getCurrentRequestedMaxMips();
            if (peCapacity >= currentRequestedMaxMips) {
                double availableMips = scheduler.getAvailableMips();
                for (Vm i : migratingInVms) {
                    if (i != vm) {
                        availableMips -= i.getCurrentRequestedTotalMips();
                    }
                }
                double currentRequestedTotalMips = vm.getCurrentRequestedTotalMips();
                if (availableMips >= currentRequestedTotalMips) {
                    double availableRam = getRamProvisioner().getAvailableRam();
                    for (Vm i : migratingInVms) {
                        if (i != vm) {
                            availableRam -= i.getCurrentRequestedRam();
                        }
                    }
                    if (availableRam >= vm.getCurrentRequestedRam()) {
                        double availableBw = getBwProvisioner().getAvailableBw();
                        for (Vm i : migratingInVms) {
                            if (i != vm) {
                                availableBw -= i.getCurrentRequestedBw();
                            }
                        }
                        if (availableBw >= vm.getCurrentRequestedBw()) {
                            return true;
                        }
                    }
                }

            }
        }
        return false;
    }

    public void setBwProvisioner(BwProvisioner bwProvisioner) {
        super.setBwProvisioner(bwProvisioner);
    }
}
