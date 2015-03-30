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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 3/12/2015.
 */
public class GreenHost extends PowerHostUtilizationHistory implements Serializable {

    private String dataCenterName;

    private List<Double> energyHystory;

    public GreenHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner,
                     long storage, List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
        energyHystory = new ArrayList<Double>();
    }

    public GreenHost(int id, RamProvisioner ramProvisioner, BwProvisioner bwProvisioner,
                     long storage, List<? extends Pe> peList, VmScheduler vmScheduler, PowerModel powerModel,
                     String dataCenterName) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler, powerModel);
        this.dataCenterName = dataCenterName;
    }

    public String getDataCenterName() {
        return dataCenterName;
    }

    public void setDataCenterName(String dataCenterName) {
        this.dataCenterName = dataCenterName;
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
        energyHystory.add(d);
    }

    public synchronized List<Double> getEnergyHystory() {
        return energyHystory;
    }

    public double getAvailableBandwidth() {
        return getBwProvisioner().getAvailableBw();
    }

    @Override
    public double updateVmsProcessing(double currentTime) {
        double smallerTime = 0;
        try {
            smallerTime = super.updateVmsProcessing(currentTime);
        } catch (Exception e) {
            e.printStackTrace();
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

            double am;
            double mips;
            if (!Log.isDisabled()) {
                try {
                    am = totalAllocatedMips;
                    mips = vm.getMips();
                    Log.formatLine(
                            "%.2f: [Host #" + getId() + "] Total allocated MIPS for VM #" + vm.getId()
                                    + " (Host #" + vm.getHost().getId()
                                    + ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)",
                            CloudSim.clock(),
                            totalAllocatedMips,
                            totalRequestedMips,
                            vm.getMips(),
                            totalRequestedMips / vm.getMips() * 100);
                } catch (Exception e) {
                    e.printStackTrace();
                }

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
