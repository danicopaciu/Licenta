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

/**
 * Created by Daniel on 3/12/2015.
 * GreenHost class implements a host green power aware
 */
public class GreenHost extends PowerHostUtilizationHistory {


    private List<Double> energyHistory;

    private List<Vm> migratingInVms;

    public GreenHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner,
                     long storage, List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
        energyHistory = new ArrayList<Double>();
        migratingInVms = new ArrayList<Vm>();
    }

    public double[] getGreenUtilizationHistory() {
        try {
            return getUtilizationHistory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getUtilizationHistory();
    }

    public synchronized void addEnergyHistory(Double d) {
        energyHistory.add(d);
    }

    public synchronized List<Double> getEnergyHistory() {
        return energyHistory;
    }

    public double getAvailableBandwidth() {
        return getBwProvisioner().getAvailableBw();
    }

    public double getMeanPower() {
        double mean = 0;
        if (energyHistory != null) {
            if (!energyHistory.isEmpty()) {
                for (Double d : energyHistory) {
                    mean += d;
                }
                int size = energyHistory.size();
                if (size != 0) {
                    mean /= energyHistory.size();
                    return mean;
                }
            }
        }
        return 0;
    }

    public void clearMigratingInVms() {
        migratingInVms.clear();
    }

    public boolean isMigrationPossible(Vm vm) {
        if (migratingInVms.isEmpty()) {

            if(isSuitableForVm(vm)){
                addMigratingVm(vm);
                return true;
            }

            return false;
        } else {
            VmScheduler scheduler = getVmScheduler();
            double peCapacity = scheduler.getPeCapacity();
            double currentRequestedMaxMips = vm.getCurrentRequestedMaxMips();
            if (peCapacity >= currentRequestedMaxMips) {
                double availableMips = scheduler.getAvailableMips();
                for (Vm i : migratingInVms) {
                    availableMips -= i.getCurrentRequestedTotalMips();
                }
                double currentRequestedTotalMips = vm.getCurrentRequestedTotalMips();
                if (availableMips >= currentRequestedTotalMips) {
                    double availableRam = getRamProvisioner().getAvailableRam();
                    for (Vm i : migratingInVms) {
                        availableRam -= i.getCurrentRequestedRam();
                    }
                    if (availableRam >= vm.getCurrentRequestedRam()) {
                        double availableBw = getBwProvisioner().getAvailableBw();
                        for (Vm i : migratingInVms) {
                            availableBw -= i.getCurrentRequestedBw();
                        }
                        if (availableBw >= vm.getCurrentRequestedBw()) {
                            addMigratingVm(vm);
                            return true;
                        }
                    }
                }

            }
        }
        return false;
    }

    public boolean addMigratingVm(Vm vm) {
        return migratingInVms.contains(vm) || migratingInVms.add(vm);
    }

    public boolean removeMigratingVm(Vm vm) {
        return migratingInVms.contains(vm) && migratingInVms.remove(vm);
    }

}
