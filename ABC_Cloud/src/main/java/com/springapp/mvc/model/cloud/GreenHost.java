package com.springapp.mvc.model.cloud;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
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


    public GreenHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner,
                     long storage, List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
        energyHistory = new ArrayList<Double>();
    }

    public double[] getGreenUtilizationHistory() {
        return getUtilizationHistory();
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


    public double updateVmsProcessing(double currentTime) {

        double smallerTime = Double.MAX_VALUE;

        for (Vm vm : getVmList()) {
            double time = vm.updateVmProcessing(currentTime, getVmScheduler().getAllocatedMipsForVm(vm));
            if (time > 0.0 && time < smallerTime) {
                smallerTime = time;
            }
        }
        setPreviousUtilizationMips(getUtilizationMips());
        setUtilizationMips(0);
        double hostTotalRequestedMips = 0;

        for (Vm vm : getVmList()) {
            getVmScheduler().deallocatePesForVm(vm);
        }

        for (Vm vm : getVmList()) {
            getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips());
        }

        for (Vm vm : getVmList()) {
            double totalRequestedMips = vm.getCurrentRequestedTotalMips();
            double totalAllocatedMips = getVmScheduler().getTotalAllocatedMipsForVm(vm);

            if (!Log.isDisabled()) {


                if (vm.getHost() == null){
                    System.out.println("");
                }

                Log.formatLine(
                        "%.2f: [Host #" + getId() + "] Total allocated MIPS for VM #" + vm.getId()
                                + " (Host #" + vm.getHost().getId()
                                + ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)",
                        CloudSim.clock(),
                        totalAllocatedMips,
                        totalRequestedMips,
                        vm.getMips(),
                        totalRequestedMips / vm.getMips() * 100);

                List<Pe> pes = getVmScheduler().getPesAllocatedForVM(vm);
                StringBuilder pesString = new StringBuilder();
                for (Pe pe : pes) {
                    pesString.append(String.format(" PE #" + pe.getId() + ": %.2f.", pe.getPeProvisioner()
                            .getTotalAllocatedMipsForVm(vm)));
                }
                Log.formatLine(
                        "%.2f: [Host #" + getId() + "] MIPS for VM #" + vm.getId() + " by PEs ("
                                + getNumberOfPes() + " * " + getVmScheduler().getPeCapacity() + ")."
                                + pesString,
                        CloudSim.clock());
            }

            if (getVmsMigratingIn().contains(vm)) {
                Log.formatLine("%.2f: [Host #" + getId() + "] VM #" + vm.getId()
                        + " is being migrated to Host #" + getId(), CloudSim.clock());
            } else {
                if (totalAllocatedMips + 0.1 < totalRequestedMips) {
                    Log.formatLine("%.2f: [Host #" + getId() + "] Under allocated MIPS for VM #" + vm.getId()
                            + ": %.2f", CloudSim.clock(), totalRequestedMips - totalAllocatedMips);
                }

                vm.addStateHistoryEntry(
                        currentTime,
                        totalAllocatedMips,
                        totalRequestedMips,
                        (vm.isInMigration() && !getVmsMigratingIn().contains(vm)));

                if (vm.isInMigration()) {
                    Log.formatLine(
                            "%.2f: [Host #" + getId() + "] VM #" + vm.getId() + " is in migration",
                            CloudSim.clock());
                    totalAllocatedMips /= 0.9; // performance degradation due to migration - 10%
                }
            }

            setUtilizationMips(getUtilizationMips() + totalAllocatedMips);
            hostTotalRequestedMips += totalRequestedMips;
        }

        addStateHistoryEntry(
                currentTime,
                getUtilizationMips(),
                hostTotalRequestedMips,
                (getUtilizationMips() > 0));

        return smallerTime;
    }


}
