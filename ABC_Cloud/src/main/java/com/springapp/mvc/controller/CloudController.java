package com.springapp.mvc.controller;

import com.springapp.mvc.model.cloud.FederationOfDataCenter;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.*;
import java.util.*;

public class CloudController {


    public static final int CONSUMED_ENERGY_VARIATION_SIMULATION = 1;
    public static final int HEAT_VARIATION_SIMULATION = 2;
    public static final int HIGH_LATENCY_VARIATION_SIMULATION = 3;
    public static final int LOW_LATENCY_VARIATION_SIMULATION = 4;
    public static final int ENERGY_HIGH_COST_VARIATION_SIMULATION = 5;
    public static final int ENERGY_LOW_COST_VARIATION_SIMULATION = 6;
    public static int SIMULATION_TYPE;
    private FederationOfDataCenter fed;


    public static void main(String[] args) {
        CloudController cloudController = new CloudController();
        cloudController.start(500, 100, 12000);
        cloudController.getOverallResults(0);
    }

    public void start(int vmNumber, int hostNumber, int simulationPeriod) {

        Log.printLine("Starting");

        try {
            int num_user = 1;   // number of cloud users
            Calendar calendar = Calendar.getInstance();
            CloudSim.init(num_user, calendar, false);
            fed = getFederationOfDatacenters(vmNumber, hostNumber);
            fed.setWindSpeedMap(initWindPower());
            fed.setSimulationPeriod(simulationPeriod);
            CloudSim.startSimulation();
            CloudSim.stopSimulation();
            Log.printLine("CloudSim finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("The simulation has been terminated due to an unexpected error");
        }
    }

    private FederationOfDataCenter getFederationOfDatacenters(int vmNumber, int hostNumber) throws FileNotFoundException {
        CloudDirector cloudDirector = new CloudDirector();
        CloudBuilder builder = new GreenCloudBuilder(vmNumber, hostNumber);
        return cloudDirector.constructFederationOfDataCenters(builder);
    }

    private Map<String, List<Double>> initWindPower() throws FileNotFoundException {
        Map<String, List<Double>> windSpeedMap = new HashMap<String, List<Double>>();
        for (Datacenter dc : fed.getDataCenterList()) {
            List<Double> windValues = new ArrayList<Double>();
            String fileName = "/wind_speed_Datacenter_0.txt";
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputStreamReader);
            try {
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

    public Map<String, Double> getPartialResults(int dataCenterId) {
        return fed.getResultForDataCenter(CloudSim.clock(), dataCenterId);
    }

    public Map<Double, Map<String, Double>> getOverallResults(int datacenterId) {
        return fed.getOverallResultsForDataCenter(datacenterId);
    }

    public List<Map<String, Object>> getGraphResults(int datacenterId) {
        return fed.getGraphResultsForDataCenter(datacenterId);
    }


    public double getSimulationProgress() {
        return fed.getSimulationProgress();
    }
}
