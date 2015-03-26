package com.springapp.mvc.model.cloud;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmScheduler;
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
        return getUtilizationHistory();
    }

    public void addEnergyHistory(Double d) {
        energyHystory.add(d);
    }

    public List<Double> getEnergyHystory() {
        return energyHystory;
    }
}
