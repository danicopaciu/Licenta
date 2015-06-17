package com.springapp.mvc.controller;

import com.springapp.mvc.model.cloud.FederationOfDataCenter;
import org.cloudbus.cloudsim.*;

import java.io.FileNotFoundException;
import java.util.List;

public abstract class CloudBuilder {

    protected FederationOfDataCenter federationOfDataCenters;
    protected List<Datacenter> dataCenterList;
    protected List<Host> hostList;
    protected List<Vm> vmList;
    protected List<Cloudlet> cloudletList;
    protected DatacenterBroker broker;
    protected int vmNumber;
    protected int hostNumber;

    public abstract FederationOfDataCenter createFederationOfDataCenters();

    public abstract List<Datacenter> createDataCenter();

    public abstract DatacenterBroker createBroker();

    public abstract List<Vm> createVMs();

    public abstract List<Host> createHosts();

    public abstract List<Cloudlet> createCloudletss() throws FileNotFoundException;

}
