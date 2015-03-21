package com.springapp.mvc.model.cloud;

import com.springapp.mvc.model.abc.FoodSource;
import com.springapp.mvc.model.abc.Nectar;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Daniel on 3/12/2015.
 */
public class FederationOfDataCenter extends SimEntity {

    private List<GreenDataCenter> dataCenterList;

    private List<GreenHost> hostList;

    private List<GreenVm> vmList;

    private List<Cloudlet> cloudletList;

    private DatacenterBroker broker;

    public FederationOfDataCenter(String name) {
        super(name);
    }

    public void migrateVMs(FoodSource bestFoodSource) {
        List<Nectar> nectarList = bestFoodSource.getNectarList();
        for (Nectar n : nectarList) {
            GreenVm vm = n.getVm();
            GreenHost host = n.getHost();
            System.out.println("Vm " + vm.getId() + " is on host " + vm.getHost().getId());
            GreenDataCenter dataCenter = (GreenDataCenter) host.getDatacenter();
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("vm", vm);
            data.put("host", vm);
            send(dataCenter.getId(), CloudSim.clock(), CloudSimTags.VM_MIGRATE, data);
            System.out.println("Vm " + vm.getId() + " is on host " + vm.getHost().getId());
        }
    }

    @Override
    public void startEntity() {

    }

    @Override
    public void processEvent(SimEvent ev) {

    }

    @Override
    public void shutdownEntity() {

    }

    public void setDataCenterList(List<GreenDataCenter> dataCenterList) {
        this.dataCenterList = dataCenterList;
    }

    public void setHostList(List<GreenHost> hostList) {
        this.hostList = hostList;
    }

    public void setVmList(List<GreenVm> vmList) {
        this.vmList = vmList;
    }

    public void setCloudletList(List<Cloudlet> cloudletList) {
        this.cloudletList = cloudletList;
    }

    public DatacenterBroker getBroker() {
        return broker;
    }

    public void setBroker(DatacenterBroker broker) {
        this.broker = broker;
    }
}
