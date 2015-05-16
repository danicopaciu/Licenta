package com.springapp.mvc.controller;

import com.springapp.mvc.model.cloud.FederationOfDataCenter;
import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bogdan
 * Date: 15/03/15
 * Time: 17:11
 * To change this template use File | Settings | File Templates.
 */
public abstract class CloudBuilder {

    protected FederationOfDataCenter federationOfDataCenters;
    protected List<GreenDataCenter> dataCenterList;
    protected List<GreenHost> hostList;
    protected List<GreenVm> vmList;
    protected List<Cloudlet> cloudletList;
    protected DatacenterBroker broker;
    protected int vmNumber;
    protected int hostNumer;

    public abstract FederationOfDataCenter createFederationOfDataCenters();
    public abstract List<GreenDataCenter> createDataCenter();
    public abstract DatacenterBroker createBroker();
    public abstract List<GreenVm> createVMs();
    public abstract List<GreenHost> createHosts();
    public abstract List<Cloudlet> createCloudletss() throws FileNotFoundException;

    public FederationOfDataCenter getFederationOfDataCenters() {
        return federationOfDataCenters;
    }

    public void setFederationOfDataCenters(FederationOfDataCenter federationOfDataCenters) {
        this.federationOfDataCenters = federationOfDataCenters;
    }

    public List<GreenDataCenter> getDataCenterList() {
        return dataCenterList;
    }

    public void setDataCenterList(List<GreenDataCenter> dataCenterList) {
        this.dataCenterList = dataCenterList;
    }

    public List<GreenHost> getHostList() {
        return hostList;
    }

    public void setHostList(List<GreenHost> hostList) {
        this.hostList = hostList;
    }

    public List<GreenVm> getVmList() {
        return vmList;
    }

    public void setVmList(List<GreenVm> vmList) {
        this.vmList = vmList;
    }

    public List<Cloudlet> getCloudletList() {
        return cloudletList;
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
