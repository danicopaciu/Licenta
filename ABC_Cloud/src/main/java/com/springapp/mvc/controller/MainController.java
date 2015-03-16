package com.springapp.mvc.controller;

import com.springapp.mvc.model.cloud.FederationOfDatacenters;
import com.springapp.mvc.model.cloud.GreenDataCenter;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bogdan
 * Date: 15/03/15
 * Time: 17:42
 * To change this template use File | Settings | File Templates.
 */
public class MainController {

    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmlist;

    private static Resources res = new Resources();
    private static GreenCloudBuilder builder = new GreenCloudBuilder();


    public static void main(String[] args) {

        String inputFolder = "";
        Resource resource = new ClassPathResource("planetlab/20110303");
        try {
            inputFolder = resource.getFile().getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

//        String inputFolder = MainController.class.getClassLoader().getResource("planetlab/20110303").getPath();

        try {
            initLogOutput("output");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.printLine("Starting");

        try {
            // First step: Initialize the CloudSim package. It should be called
            // before creating any entities.
            int num_user = 1;   // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false;  // mean trace events

            // Initialize the CloudSim library
            CloudSim.init(num_user, calendar, trace_flag);

            List<PowerDatacenter> dc_list = new ArrayList<PowerDatacenter>();
            // Second step: Create Datacenters
            //Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation

            for (int i = 1; i <= res.data_center_nr; i++) {
                int server_nr = res.DC_to_server.get(i);
                dc_list.add(builder.createDatacenter(i, server_nr));
            }

            FederationOfDatacenters fed = new FederationOfDatacenters(dc_list);

            //Third step: Create Broker
            DatacenterBroker broker = builder.createBroker();
            int brokerId = broker.getId();

            //Fourth step: Create one virtual machine

            //VM description
            int mips = 250;
            long size = 10000; //image size (MB)
            int ram = 512; //vm memory (MB)
            long bw = 1000;
            int pesNumber = 1; //number of cpus
            String vmm = "Xen"; //VMM name

            vmlist = builder.createVMs(res.vm_nr, brokerId, mips, size, ram, bw, pesNumber, vmm, 1, 300);

            //submit vm list to the broker
            broker.submitVmList(vmlist);


            //Fifth step: Create two Cloudlets
            cloudletList = new ArrayList<Cloudlet>();

            //Cloudlet properties
            pesNumber = 1;
            long length = res.CLOUDLET_LENGTH;
            long fileSize = 300;
            long outputSize = 300;

            cloudletList = builder.createCloudletss(res.vm_nr, brokerId, length, pesNumber, fileSize, outputSize, inputFolder);

            //submit cloudlet list to the broker
            broker.submitCloudletList(cloudletList);


            //bind the cloudlets to the vms. This way, the broker
            // will submit the bound cloudlets only to the specific VM

            broker = builder.bindCloudletsToVM(broker, cloudletList, vmlist);

            // Sixth step: Starts the simulation

            //.terminateSimulation(24*60*60);
            CloudSim.startSimulation();
            CloudSim.terminateSimulation(24*60*60);



            // Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            Log.printLine("Received " + newList.size() + " cloudlets");

            CloudSim.stopSimulation();

            printCloudletList(newList);

            Log.printLine("CloudSim finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }




    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime()) +
                        indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }

    }



    private static void initLogOutput(String outputFolder) throws IOException {

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
