package com.springapp.mvc.controller;

import com.springapp.mvc.model.abc.ArtificialBeeColony;
import com.springapp.mvc.model.cloud.FederationOfDataCenter;
import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenVm;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Bogdan
 * Date: 15/03/15
 * Time: 17:42
 * To change this template use File | Settings | File Templates.
 */
public class MainController {


    private static FederationOfDataCenter fed;
    private static List<Double> windSpeed;

    public static void main(String[] args) {

        Log.printLine("Starting");

        try {
            int num_user = 1;   // number of cloud users
            Calendar calendar = Calendar.getInstance();

            CloudSim.init(num_user, calendar, false);

            fed = getFederationOfDatacenters();
            fed.setWindSpeedMap(initWindPower());
            CloudSim.startSimulation();
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
        System.exit(0);
    }

    private static double getAllocatedDC() {
        double counter = 0;
        for (GreenDataCenter dc : fed.getDataCenterList()) {
            if (!dc.getVmList().isEmpty()) {
                counter++;
            }
        }
        return counter / 10;
    }

    private static FederationOfDataCenter getFederationOfDatacenters() throws FileNotFoundException {
        CloudDirector cloudDirector = new CloudDirector();
        CloudBuilder builder = new GreenCloudBuilder();
        return cloudDirector.constructFederationOfDataCenters(builder);
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


    private static void migrateVMs(List<GreenVm> vmList){
        Random rand = new Random();
        int vmNr = rand.nextInt(vmList.size() - 2) + 1;

        Set<GreenVm> migratingSet = new HashSet<GreenVm>();

        while (migratingSet.size() <= vmNr) {
            int index = rand.nextInt(vmList.size() -1);
            GreenVm vm = vmList.get(index);
            if (migratingSet.add(vm)) {
            }
        }
        List<GreenVm> migratingList = new ArrayList<GreenVm>();
        if (migratingSet.size() != 0) {
            migratingList.addAll(migratingSet);
        }

        List<GreenDataCenter> DClist = fed.getDataCenterList();

        ArtificialBeeColony abc = new ArtificialBeeColony(DClist, migratingList);
        abc.runAlgorithm();
    }


    private static void initWindList() throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader("wind_speed_Datacenter_0.txt"));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                String[] data = line.split(" ");
                windSpeed.add(Double.parseDouble(data[1]));
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Map<String, List<Double>> initWindPower() throws FileNotFoundException {
        Map<String, List<Double>> windSpeedMap = new HashMap<String, List<Double>>();
        for (GreenDataCenter dc : fed.getDataCenterList()) {
            List<Double> windValues = new ArrayList<Double>();
            String fileName = "wind_speed_" + dc.getName() + ".txt";
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    String[] data = line.split(" ");
                    windValues.add(Double.parseDouble(data[1]));
                    line = br.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            windSpeedMap.put(dc.getName(), windValues);
        }
        return windSpeedMap;
    }

}
