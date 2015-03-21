package com.springapp.mvc.controller;

import com.springapp.mvc.model.cloud.FederationOfDataCenters;
import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.lists.VmList;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bogdan
 * Date: 15/03/15
 * Time: 17:11
 * To change this template use File | Settings | File Templates.
 */
public abstract class CloudBuilder {

    protected FederationOfDataCenters federationOfDataCenters;
    protected List<GreenDataCenter> dataCenterList;
    protected List<GreenHost> hostList;
    protected List<GreenVm> vmList;
    protected List<Cloudlet> cloudletList;
    protected DatacenterBroker broker;

    public abstract FederationOfDataCenters createFederationOfDataCenters();
    public abstract List<GreenDataCenter> createDataCenter();
    public abstract DatacenterBroker createBroker();
    public abstract List<GreenVm> createVMs();
    public abstract List<GreenHost> createHosts();
    public abstract List<Cloudlet> createCloudletss() throws FileNotFoundException;
    }
