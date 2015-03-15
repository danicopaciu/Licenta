package com.springapp.mvc.controller;

import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.power.PowerDatacenterBroker;
import org.cloudbus.cloudsim.power.models.PowerModelSpecPowerHpProLiantMl110G4Xeon3040;
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

    public GreenDataCenter createDatacenter(int id, int server_nr) {

        // Here are the steps needed to create a PowerDatacenter:
        // 1. We need to create a list to store
        //    our machine
        List<Host> hostList = new ArrayList<Host>();

        // 2. A Machine contains one or more PEs or CPUs/Cores.
        // In this example, it will have only one core.
        List<Pe> peList = new ArrayList<Pe>();

        int mips = 1000;

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating

        //4. Create Host with its id and list of PEs and add them to the list of machines
        //int hostId=0;
        int ram = 2048; //host memory (MB)
        long storage = 1000000; //host storage
        int bw = 10000;

        for (int i = 1; i <= server_nr; i++) {
            hostList.add(
                    new GreenHost(i, new RamProvisionerSimple(ram),new BwProvisionerSimple(bw),
                         storage, peList,new VmSchedulerTimeShared(peList), new PowerModelSpecPowerHpProLiantMl110G4Xeon3040())
            ); // This is our machine
        }


        // 5. Create a DatacenterCharacteristics object that stores the
        //    properties of a data center: architecture, OS, list of
        //    Machines, allocation policy: time- or space-shared, time zone
        //    and its price (G$/Pe time unit).
        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;        // the cost of using memory in this resource
        double costPerStorage = 0.001;    // the cost of using storage in this resource
        double costPerBw = 0.0;            // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();    //we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


        // 6. Finally, we need to create a PowerDatacenter object.
        GreenDataCenter datacenter = null;
        try {
            datacenter = new GreenDataCenter("Datacenter_" + ++id, characteristics, new VmAllocationPolicySimple(hostList), storageList, 300);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }
    public DatacenterBroker createBroker() {

        DatacenterBroker broker = null;
        try {
            broker = new PowerDatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }
    public ArrayList<Vm> createVMs(int vm_nr, int brokerId, int mips, long size, int ram, long bw, int pesNumber, String vmm, int priority, double schedInt) {

        ArrayList<Vm> vmlist = new ArrayList<Vm>();

        for (int i = 1; i <= vm_nr; i++) {
            vmlist.add(new GreenVm(i, brokerId, mips, pesNumber, ram, bw, size,priority, vmm, new CloudletSchedulerTimeShared(), schedInt));
        }

        return vmlist;
    }
    public ArrayList<Cloudlet> createCloudletss(int vm_nr, int brokerId, long length, int pesNumber,
                                                        long fileSize, long outputSize, String inputFolderName) throws FileNotFoundException {

        UtilizationModel utilizationModel = new UtilizationModelFull();
        UtilizationModel utilizationModelNull = new UtilizationModelNull();

        java.io.File inputFolder = new java.io.File(inputFolderName);
        java.io.File[] files = inputFolder.listFiles();
        ArrayList<Cloudlet> cloudlet_list = new ArrayList<Cloudlet>();

        for (int i = 1; i <= vm_nr; i++) {
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
            cloudlet_list.add(cloudlet);
        }

        return cloudlet_list;
    }
    public DatacenterBroker bindCloudletsToVM(DatacenterBroker broker, List<Cloudlet> cloudletList, List<Vm> vmlist){
        int size = cloudletList.size();

        for (int i = 0; i < size; i++) {
            broker.bindCloudletToVm(cloudletList.get(i).getCloudletId(), vmlist.get(i).getId());
        }
        return broker;
    }

}
