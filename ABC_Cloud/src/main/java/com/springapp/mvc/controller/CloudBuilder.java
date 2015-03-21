package com.springapp.mvc.controller;

import com.springapp.mvc.model.cloud.GreenHost;
import org.cloudbus.cloudsim.*;

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

    public abstract Datacenter createDatacenter(int id, List<GreenHost> hostList);
    public abstract DatacenterBroker createBroker();
    public abstract List<Vm> createVMs(int vmNumber, int brokerId, int mips, long size, int ram,
                                       long bw, int pesNumber, String vmm, int priority, double schedInt);
    public abstract List<GreenHost> createHosts(int hostNumber);
    public abstract List<Cloudlet> createCloudletss(int vmNumber, int brokerId, long length, int pesNumber,
                                                long fileSize, long outputSize, String inputFolderName) throws FileNotFoundException;
    }
