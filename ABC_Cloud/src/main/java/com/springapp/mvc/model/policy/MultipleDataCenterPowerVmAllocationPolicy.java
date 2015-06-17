package com.springapp.mvc.model.policy;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;

import java.util.List;
import java.util.Map;


public class MultipleDataCenterPowerVmAllocationPolicy extends PowerVmAllocationPolicyAbstract {

    /**
     * Instantiates a new power vm allocation policy abstract.
     *
     * @param list the list
     */
    public MultipleDataCenterPowerVmAllocationPolicy(List<? extends Host> list) {
        super(list);
    }

    @Override
    public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
        return null;
    }

    @Override
    public boolean allocateHostForVm(Vm vm, Host host) {
        if (host == null) {
            Log.formatLine("%.2f: No suitable host found for VM #" + vm.getId() + "\n", CloudSim.clock());
            return false;
        }
        if (host.vmCreate(vm)) { // if vm has been succesfully created in the host
            Datacenter dc = host.getDatacenter();
            VmAllocationPolicy allocationPolicy = dc.getVmAllocationPolicy();
            if (allocationPolicy instanceof PowerVmAllocationPolicyAbstract) {
                PowerVmAllocationPolicyAbstract powerVmAllocationPolicyAbstract = (PowerVmAllocationPolicyAbstract) allocationPolicy;
                powerVmAllocationPolicyAbstract.getVmTable().put(vm.getUid(), host);
                Log.formatLine(
                        "%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(),
                        CloudSim.clock());
                return true;
            }
        }
        Log.formatLine(
                "%.2f: Creation of VM #" + vm.getId() + " on the host #" + host.getId() + " failed\n",
                CloudSim.clock());
        return false;
    }
}
