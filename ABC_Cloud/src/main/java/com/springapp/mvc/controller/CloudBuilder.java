package com.springapp.mvc.controller;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Vm;

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

    public abstract Datacenter createDatacenter(int id, int server_nr);
    public abstract DatacenterBroker createBroker();
    public abstract ArrayList<Vm> createVMs(int vm_nr, int brokerId, int mips, long size, int ram, long bw, int pesNumber, String vmm, int priority, double schedInt);
    public abstract ArrayList<Cloudlet> createCloudletss(int vm_nr, int brokerId, long length, int pesNumber,
                                                long fileSize, long outputSize, String inputFolderName) throws FileNotFoundException;
    }
