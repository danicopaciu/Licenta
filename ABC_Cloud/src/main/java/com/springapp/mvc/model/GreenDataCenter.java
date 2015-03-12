package com.springapp.mvc.model;

import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.power.PowerDatacenter;

import java.util.List;

/**
 * Created by Daniel on 3/12/2015.
 */
public class GreenDataCenter extends PowerDatacenter {


    private double greenEnergyQuantity;

    public GreenDataCenter(String name, DatacenterCharacteristics characteristics,
                           VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
                           double schedulingInterval) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
    }

    public GreenDataCenter(String name, DatacenterCharacteristics characteristics,
                           VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
                           double schedulingInterval, double greenEnergyQuantity) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
        this.greenEnergyQuantity = greenEnergyQuantity;
    }
}
