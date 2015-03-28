package com.springapp.mvc.model.cloud;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.PowerVm;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.cloudbus.cloudsim.util.MathUtil;

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

    protected double[] getUtilizationHistory() {
        double[] utilizationHistory = new double[PowerVm.HISTORY_LENGTH];
        int i;
        GreenVm vm1;
        double hostMips = getTotalMips();
        try {
            for (PowerVm vm : this.<PowerVm>getVmList()) {
                vm1 = (GreenVm) vm;
                for (i = 0; i < vm1.getUtilizationHistory().size(); i++) {
                    System.out.println(vm1.getUtilizationHistory().size());
                    if (i == 30) {
                        System.out.println(CloudSim.clock());
                    }
                    utilizationHistory[i] += vm1.getUtilizationHistory().get(i) * vm.getMips() / hostMips;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return MathUtil.trimZeroTail(utilizationHistory);
    }
}
