package com.springapp.mvc.controller;

import com.springapp.mvc.model.cloud.FederationOfDataCenter;
import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import com.springapp.mvc.model.policy.MultipleDataCenterPowerVmAllocationPolicy;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: Bogdan
 * Date: 15/03/15
 * Time: 17:30
 * To change this template use File | Settings | File Templates.
 */
public class GreenCloudBuilder extends CloudBuilder {

    public static final String PLANETLAB_20110303 = "planetlab/20110303";

    public GreenCloudBuilder(int vmNumber, int hostNumber) {
        this.vmNumber = vmNumber;
        this.hostNumer = hostNumber;
    }

    @Override
    public FederationOfDataCenter createFederationOfDataCenters() {
        federationOfDataCenters = new FederationOfDataCenter("FederationOfDataCenters");
        return federationOfDataCenters;
    }

    public List<GreenDataCenter> createDataCenter() {


        dataCenterList = new ArrayList<GreenDataCenter>();
        for(int id = 0; id < Resources.DATACENTER_NUMBER; id++) {

            List<GreenHost> thisHostList = new ArrayList<GreenHost>();
            int index = id * hostNumer;
            for (int i = index; i < index + hostNumer; i++) {
                thisHostList.add(hostList.get(i));
            }


            String arch = "x86";
            String os = "Linux";
            String vmm = "Xen";
            double timeZone = 10.0;         // time zone this resource located
            double cost = 3.0;              // the cost of using processing in this resource
            double costPerMem = 0.05;        // the cost of using memory in this resource
            double costPerStorage = 0.001;    // the cost of using storage in this resource
            double costPerBw = 0.0;            // the cost of using bw in this resource
            LinkedList<Storage> storageList = new LinkedList<Storage>();    //we are not adding SAN devices by now
            GreenDataCenter dataCenter = getGreenDataCenter(id,
                    thisHostList, arch, os, vmm, timeZone, cost, costPerMem,
                    costPerStorage, costPerBw, storageList);
            dataCenterList.add(dataCenter);
        }
        return dataCenterList;
    }

    private GreenDataCenter getGreenDataCenter(int id,
                                               List<GreenHost> thisHostList, String arch, String os,
                                               String vmm, double timeZone, double cost, double costPerMem,
                                               double costPerStorage, double costPerBw,
                                               LinkedList<Storage> storageList) {

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, thisHostList, timeZone, cost, costPerMem, costPerStorage, costPerBw);


        GreenDataCenter dataCenter = null;
        try {
            dataCenter = new GreenDataCenter("DataCenter_" + id, characteristics,
                    new MultipleDataCenterPowerVmAllocationPolicy(thisHostList), storageList, 300, vmNumber / Resources.DATACENTER_NUMBER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataCenter;
    }

    public DatacenterBroker createBroker() {

        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    public List<GreenVm> createVMs() {

        int vmType = getVmType();

        int mips = Constants.VM_MIPS[vmType];
        long size = vmNumber;
        int ram = Constants.VM_RAM[vmType];
        long bw = Constants.VM_BW;
        int pesNumber = Constants.VM_PES[vmType];
        String vmm = "Xen";
        int priority = 1;
        double scheduleInterval = 300;

        vmList = new ArrayList<GreenVm>();
        for (int i = 0; i < vmNumber; i++) {
            GreenVm vm = new GreenVm(i, broker.getId(), mips, pesNumber, ram, bw, size, priority,
                    vmm, new CloudletSchedulerDynamicWorkload(mips, pesNumber), scheduleInterval);
            vmList.add(vm);
        }
        broker.submitVmList(vmList);
        return vmList;
    }

    private int getVmType() {
        Random random = new Random();
        return random.nextInt(Constants.VM_TYPES);
    }

    @Override
    public List<GreenHost> createHosts() {
        hostList = new ArrayList<GreenHost>();
        for (int i = 0; i < Resources.DATACENTER_NUMBER * hostNumer; i++) {
            int hostType = i % Constants.HOST_TYPES;
            List<Pe> peList = getPes(hostType);
            GreenHost host = getHost(i, hostType, peList);
            hostList.add(host);
        }
        return hostList;
    }

    private GreenHost getHost(int i, int hostType, List<Pe> peList) {
        return new GreenHost(
                        i,
                        new RamProvisionerSimple(Constants.HOST_RAM[hostType]),
                new BwProvisionerSimple(Constants.HOST_BW[0]),
                        Constants.HOST_STORAGE,
                        peList,
                        new VmSchedulerTimeSharedOverSubscription(peList),
                        Constants.HOST_POWER[hostType]);
    }

    private List<Pe> getPes(int hostType) {
        List<Pe> peList = new ArrayList<Pe>();
        for (int j = 0; j < Constants.HOST_PES[hostType]; j++) {
            peList.add(new Pe(j, new PeProvisionerSimple(Constants.HOST_MIPS[hostType])));
        }
        return peList;
    }

    public List<Cloudlet> createCloudletss() throws FileNotFoundException {
        String inputFolderName = getInputFolder();
        java.io.File inputFolder = new java.io.File(inputFolderName);
        java.io.File[] files = inputFolder.listFiles();
        cloudletList = new ArrayList<Cloudlet>();
        getCloudletList(files);
        broker.submitCloudletList(cloudletList);
        broker = bindCloudletsToVM();
        return cloudletList;
    }

    private void getCloudletList(File[] files) {
        int pesNumber = 1;
        long length = Resources.CLOUDLET_LENGTH;
        long fileSize = 300;
        long outputSize = 300;
        UtilizationModel utilizationModel = new UtilizationModelStochastic();
        if(files!= null) {
            for (int i = 0; i < vmNumber; i++) {
                Cloudlet cloudlet = null;
                try {
                    Random random = new Random();
                    int fileIndex = random.nextInt(files.length - 1);
                    UtilizationModelPlanetLabInMemory utilizationModelPlanetLabInMemory = new UtilizationModelPlanetLabInMemory(
                            files[fileIndex].getAbsolutePath(),
                            Resources.SCHEDULING_INTERVAL);
                    cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize,
                            utilizationModelPlanetLabInMemory,
                            utilizationModelPlanetLabInMemory, utilizationModelPlanetLabInMemory);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(0);

                }
                cloudlet.setUserId(broker.getId());
                cloudletList.add(cloudlet);
            }
        }
    }

    public DatacenterBroker bindCloudletsToVM(){
        int size = cloudletList.size();

        for (int i = 0; i < size; i++) {
            broker.bindCloudletToVm(cloudletList.get(i).getCloudletId(), vmList.get(i).getId());
        }
        return broker;
    }

    private String getInputFolder() {
        String inputFolder = "";
        Resource resource = new ClassPathResource(PLANETLAB_20110303);
        try {
            inputFolder = resource.getFile().getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            initLogOutput("output");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputFolder;
    }

    private void initLogOutput(String outputFolder) throws IOException {

        Log.setDisabled(false);

        java.io.File folder = new java.io.File(outputFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }

        java.io.File folder2 = new java.io.File(outputFolder + "/log");
        if (!folder2.exists()) {
            folder2.mkdir();
        }

        java.io.File file = new java.io.File(outputFolder + "/log/"
                + "licenta" + ".txt");
        file.createNewFile();
        Log.setOutput(new FileOutputStream(file));
    }


}
