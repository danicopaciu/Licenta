package com.springapp.mvc.controller;

import com.springapp.mvc.model.cloud.FederationOfDataCenters;
import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Bogdan
 * Date: 15/03/15
 * Time: 17:42
 * To change this template use File | Settings | File Templates.
 */
public class MainController {


    public static void main(String[] args) {

        Log.printLine("Starting");

        try {
            int num_user = 1;   // number of cloud users
            Calendar calendar = Calendar.getInstance();

            CloudSim.init(num_user, calendar, false);

            FederationOfDataCenters fed = getFederationOfDatacenters();

            CloudSim.startSimulation();
            CloudSim.terminateSimulation(24 * 60 * 60);

//          Final step: Print results when simulation is over
            DatacenterBroker broker = fed.getBroker();
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

    private static FederationOfDataCenters getFederationOfDatacenters() throws FileNotFoundException {
        CloudDirector cloudDirector = new CloudDirector();
        CloudBuilder builder = new GreenCloudBuilder();
        FederationOfDataCenters fed = cloudDirector.constructFederationOfDataCenters(builder);
        return fed;
    }

//    private static String getInputFolder() {
//        String inputFolder = "";
//        Resource resource = new ClassPathResource("planetlab/20110303");
//        try {
//            inputFolder = resource.getFile().getAbsolutePath();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            initLogOutput("output");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return inputFolder;
//    }
//
//    private static List<Cloudlet> getCloudletList(String inputFolder, int brokerId) throws FileNotFoundException {
//        List<Cloudlet> cloudletList;
//        int pesNumber = 1;
//        long length = Resources.CLOUDLET_LENGTH;
//        long fileSize = 300;
//        long outputSize = 300;
//
//        cloudletList = builder.createCloudletss(Resources.VM_NUMBER, brokerId,
//                length, pesNumber, fileSize, outputSize, inputFolder);
//        return cloudletList;
//    }
//
//    private static List<Vm> getVmList(int brokerId) {
//        int mips = 250;
//        long size = 10000; //image size (MB)
//        int ram = 512; //vm memory (MB)
//        long bw = 1000;
//        int pesNumber = 1; //number of cpus
//        String vmm = "Xen"; //VMM name
//
//        return builder.createVMs(Resources.VM_NUMBER, brokerId, mips,
//                size, ram, bw, pesNumber, vmm, 1, 300);
//    }


    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (Cloudlet aList : list) {
            cloudlet = aList;
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime()) +
                        indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }

    }



//    private void initLogOutput(String outputFolder) throws IOException {
//
//        Log.setDisabled(false);
//
//        java.io.File folder = new java.io.File(outputFolder);
//        if (!folder.exists()) {
//            folder.mkdir();
//        }
//
//        java.io.File folder2 = new java.io.File(outputFolder + "/log");
//        if (!folder2.exists()) {
//            folder2.mkdir();
//        }
//
//        java.io.File file = new java.io.File(outputFolder + "/log/"
//                + "licenta" + ".txt");
//        file.createNewFile();
//        Log.setOutput(new FileOutputStream(file));
//    }




}
