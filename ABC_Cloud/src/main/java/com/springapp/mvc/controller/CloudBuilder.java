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

    public abstract FederationOfDataCenter createFederationOfDataCenters();
    public abstract List<GreenDataCenter> createDataCenter();
    public abstract DatacenterBroker createBroker();
    public abstract List<GreenVm> createVMs();
    public abstract List<GreenHost> createHosts();
    public abstract List<Cloudlet> createCloudletss() throws FileNotFoundException;
    }
