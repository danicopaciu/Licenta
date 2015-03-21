package com.springapp.mvc.controller;

import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.examples.power.Constants;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicySimple;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bogdan
 * Date: 15/03/15
 * Time: 17:30
 * To change this template use File | Settings | File Templates.
 */
public class GreenCloudBuilder extends CloudBuilder {

    public GreenDataCenter createDatacenter(int id, List<GreenHost> hostList) {

        List<GreenHost> thisHostList = new ArrayList<GreenHost>();
        int index = id * Resources.HOST_NUMBER_PER_DATACENTER;
        for(int i = index; i < index + Resources.HOST_NUMBER_PER_DATACENTER; i++){
            thisHostList.add(hostList.get(i));
        }


        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double timeZone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;        // the cost of using memory in this resource
        double costPerStorage = 0.001;    // the cost of using storage in this resource
        double costPerBw = 0.0;            // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();    //we are not adding SAN devices by now

        return getGreenDataCenter(id,
                hostList, thisHostList, arch, os, vmm, timeZone, cost, costPerMem,
                costPerStorage, costPerBw, storageList);
    }

    private GreenDataCenter getGreenDataCenter(int id, List<GreenHost> hostList,
                                               List<GreenHost> thisHostList, String arch, String os,
                                               String vmm, double timeZone, double cost, double costPerMem,
                                               double costPerStorage, double costPerBw,
                                               LinkedList<Storage> storageList) {
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, thisHostList, timeZone, cost, costPerMem, costPerStorage, costPerBw);


        GreenDataCenter dataCenter = null;
        try {
            dataCenter = new GreenDataCenter("DataCenter_" + ++id, characteristics,
                    new PowerVmAllocationPolicySimple(hostList), storageList, 300);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataCenter;
    }

    public DatacenterBroker createBroker() {

        DatacenterBroker broker;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    public List<Vm> createVMs(int vm_nr, int brokerId, int mips, long size, int ram, long bw,
                              int pesNumber, String vmm, int priority, double scheduleInterval) {

        List<Vm> vmList = new ArrayList<Vm>();

        for (int i = 1; i <= vm_nr; i++) {
            vmList.add(new GreenVm(i, brokerId, mips, pesNumber, ram, bw, size, priority,
                    vmm, new CloudletSchedulerDynamicWorkload(mips, pesNumber), scheduleInterval));
        }

        return vmList;
    }

    @Override
    public List<GreenHost> createHosts(int hostNumber) {
        List<GreenHost> hostList = new ArrayList<GreenHost>();
        for (int i = 0; i < hostNumber; i++) {
            int hostType = i % Constants.HOST_TYPES;

            List<Pe> peList = new ArrayList<Pe>();
            for (int j = 0; j < Constants.HOST_PES[hostType]; j++) {
                peList.add(new Pe(j, new PeProvisionerSimple(Constants.HOST_MIPS[hostType])));
            }

            hostList.add(new GreenHost(
                    i,
                    new RamProvisionerSimple(Constants.HOST_RAM[hostType]),
                    new BwProvisionerSimple(Constants.HOST_BW),
                    Constants.HOST_STORAGE,
                    peList,
                    new VmSchedulerTimeSharedOverSubscription(peList),
                    Constants.HOST_POWER[hostType]));
        }
        return hostList;
    }

    public List<Cloudlet> createCloudletss(int vmNumber, int brokerId, long length, int pesNumber,
                                                        long fileSize, long outputSize, String inputFolderName) throws FileNotFoundException {

        UtilizationModel utilizationModelNull = new UtilizationModelNull();

        java.io.File inputFolder = new java.io.File(inputFolderName);
        java.io.File[] files = inputFolder.listFiles();
        ArrayList<Cloudlet> cloudletList = new ArrayList<Cloudlet>();

        for (int i = 1; i <= vmNumber; i++) {
            Cloudlet cloudlet = null;
            try {
                cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize,
                        new UtilizationModelPlanetLabInMemory(
                                files[i].getAbsolutePath(),
                                300),
                        utilizationModelNull, utilizationModelNull);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);

            }
            cloudlet.setUserId(brokerId);
            cloudletList.add(cloudlet);
        }

        return cloudletList;
    }
    public DatacenterBroker bindCloudletsToVM(DatacenterBroker broker,
                                              List<Cloudlet> cloudletList, List<Vm> vmList){
        int size = cloudletList.size();

        for (int i = 0; i < size; i++) {
            broker.bindCloudletToVm(cloudletList.get(i).getCloudletId(), vmList.get(i).getId());
        }
        return broker;
    }

}
