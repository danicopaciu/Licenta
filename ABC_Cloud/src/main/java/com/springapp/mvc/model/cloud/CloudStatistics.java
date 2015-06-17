package com.springapp.mvc.model.cloud;

import com.springapp.mvc.controller.CloudController;
import com.springapp.mvc.model.abc.FoodSource;
import com.springapp.mvc.model.abc.Nectar;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Daniel on 5/17/2015.
 * CloudStatistics
 */
public class CloudStatistics {

    private static final String FILE_PATH = "results";
    private static final String FILE_EXTENSION = ".csv";
    private static final String ENCODING = "utf-8";
    private Map<Double, Map<Datacenter, List<Double>>> results;
    private int totalCloudVms;
    private int migratedVms;

    public CloudStatistics() {
        this.results = new TreeMap<Double, Map<Datacenter, List<Double>>>();
    }

    public void addSolutionResult(Double time, List<Datacenter> datacenters, FoodSource solution) {
        Map<Datacenter, List<Double>> stepResults = new HashMap<Datacenter, List<Double>>();

        Map<Datacenter, int[]> result = analyzeSolution(solution, datacenters);
        totalCloudVms = 0;
        for (Datacenter dc : datacenters) {
            GreenDataCenter greenDc = (GreenDataCenter) dc;
            int[] vms = result.get(dc);
            totalCloudVms += getOverallVms(dc);
            addPartialResult(stepResults, dc, GreenDataCenter.VMS_IN, vms[0]);
            addPartialResult(stepResults, dc, GreenDataCenter.VMS_OUT, vms[1]);
            int totalVms = greenDc.getTotalVms() - vms[1] + vms[0];
            greenDc.setTotalVms(totalVms);
            addPartialResult(stepResults, dc, GreenDataCenter.TOTAL_VMS, greenDc.getTotalVms());
            addPartialResult(stepResults, dc, GreenDataCenter.GREEN_ENERGY, greenDc.getGreenEnergyQuantity());
            addPartialResult(stepResults, dc, GreenDataCenter.BROWN_ENERGY, greenDc.getBrownEnergyQuantity());
            addPartialResult(stepResults, dc, GreenDataCenter.SERVERS_ENERGY, greenDc.getPower());
            addPartialResult(stepResults, dc, GreenDataCenter.HEAT, greenDc.getHeatGained());
            addPartialResult(stepResults, dc, GreenDataCenter.COOLING, greenDc.getCoolingEnergy());
        }
        List<Double> dc0List = stepResults.get(datacenters.get(0));
        dc0List.add(8, (double) totalCloudVms);
        dc0List.add(9, (double) migratedVms);
        results.put(time, stepResults);
    }

    private Map<Datacenter, int[]> analyzeSolution(FoodSource solution, List<Datacenter> datacenters) {
        Map<Datacenter, int[]> result = new HashMap<Datacenter, int[]>();
        for (Datacenter dc : datacenters) {
            int[] vms = new int[2];
            vms[0] = vms[1] = 0;
            result.put(dc, vms);
        }
        if (solution != null) {
            for (Nectar n : solution.getNectarList()) {
                Vm vm = n.getVm();
                Host prevHost = vm.getHost();
                Host nextHost = n.getHost();
                Datacenter prevDc = prevHost.getDatacenter();
                Datacenter nextDc = nextHost.getDatacenter();
                int[] out = result.get(prevDc);
                out[1]++;
                int[] in = result.get(nextDc);
                in[0]++;
            }
        }
        return result;
    }

    private int getOverallVms(Datacenter dc) {
        int overallVms = 0;
        for (Host h : dc.getHostList()) {
            overallVms = overallVms + h.getVmList().size();
        }
        return overallVms;
    }

    public void addPartialResult(Map<Datacenter, List<Double>> stepResults, Datacenter dc, int type, double value) {
        if (!stepResults.containsKey(dc)) {
            stepResults.put(dc, new ArrayList<Double>(Collections.nCopies(10, 0.0)));
        }
        List<Double> values = stepResults.get(dc);
        values.set(type, value);
    }

    public Map<Double, Map<Datacenter, List<Double>>> getResults() {
        return results;
    }

    public int getTotalCloudVms() {
        return totalCloudVms;
    }

    public int getMigratedVms() {
        return migratedVms;
    }

    public void setMigratedVms(int migratedVms) {
        this.migratedVms = migratedVms;
    }

    public Map<Datacenter, List<Double>> getStepResult(Double time) {
        return results.get(time);
    }

    public Map<String, Double> getStepResultsForDataCenter(Double time, GreenDataCenter dc) {
        Map<String, Double> data = null;
        Map<Datacenter, List<Double>> datacenterListMap;
        datacenterListMap = results.get(time);
        if (datacenterListMap == null) {
            datacenterListMap = results.get(time - 300);
        }
        List<Double> dataCenterResults = datacenterListMap.get(dc);
        data = new HashMap<String, Double>();
        data.put("clock", CloudSim.clock());
        data.put("greenEnergy", dataCenterResults.get(GreenDataCenter.GREEN_ENERGY));
        data.put("serverEnergy", dataCenterResults.get(GreenDataCenter.SERVERS_ENERGY));
        data.put("dcVms", dataCenterResults.get(GreenDataCenter.TOTAL_VMS));
        data.put("overallVms", (double) getTotalCloudVms());
        data.put("VmsIn", dataCenterResults.get(GreenDataCenter.VMS_IN));
        data.put("VmsOut", dataCenterResults.get(GreenDataCenter.VMS_OUT));
        data.put("migratedVms", (double) getMigratedVms());
        return data;
    }

    public Map<Double, Map<String, Double>> getOverallResultsForDatacenter(Datacenter dc) {
        Map<Double, Map<String, Double>> dataCenterResult = new TreeMap<Double, Map<String, Double>>();
        for (Map.Entry<Double, Map<Datacenter, List<Double>>> entry : results.entrySet()) {
            double key = entry.getKey();
            Map<Datacenter, List<Double>> values = entry.getValue();
            List<Double> valueList = values.get(dc);
            Map<String, Double> data = new LinkedHashMap<String, Double>();
            if (valueList != null) {
                data.put("greenEnergy", truncateTwoDecimals(valueList.get(GreenDataCenter.GREEN_ENERGY)));
                data.put("brownEnergy", truncateTwoDecimals(valueList.get(GreenDataCenter.BROWN_ENERGY)));
                data.put("serverEnergy", truncateTwoDecimals(valueList.get(GreenDataCenter.SERVERS_ENERGY)));
                data.put("cooling", truncateTwoDecimals(valueList.get(GreenDataCenter.COOLING)));
                data.put("heat", truncateTwoDecimals(valueList.get(GreenDataCenter.HEAT)));
                data.put("VmsIn", valueList.get(GreenDataCenter.VMS_IN));
                data.put("VmsOut", valueList.get(GreenDataCenter.VMS_OUT));
                data.put("dcVms", valueList.get(GreenDataCenter.TOTAL_VMS));
                data.put("totalCloudVms", valueList.get(8));
                data.put("migratedVms", valueList.get(9));
                dataCenterResult.put(key, data);
            }
        }
        writeFile(dataCenterResult);
        return dataCenterResult;
    }

    public List<Map<String, Object>> getGraphResultsForDatacenter(Datacenter dc) {
        List<Map<String, Object>> dataCenterResult = new ArrayList<Map<String, Object>>();
        for (Map.Entry<Double, Map<Datacenter, List<Double>>> entry : results.entrySet()) {
            double key = entry.getKey();
            Map<Datacenter, List<Double>> values = entry.getValue();
            List<Double> valueList = values.get(dc);
            if (valueList != null) {
                double serverEnergy = valueList.get(GreenDataCenter.SERVERS_ENERGY);
                String name = null;
                if (CloudController.SIMULATION_TYPE == CloudController.CONSUMED_ENERGY_VARIATION_SIMULATION) {
                    double greenEnergy = valueList.get(GreenDataCenter.GREEN_ENERGY);
                    dataCenterResult.add(getMapResult(key, greenEnergy, "GreenEnergy"));
                    name = "ServerEnergy";
                } else if (CloudController.SIMULATION_TYPE == CloudController.HEAT_VARIATION_SIMULATION) {
                    name = "ServerEnergy_heat";
                }
                dataCenterResult.add(getMapResult(key, serverEnergy, name));
            }
        }
        return dataCenterResult;
    }

    public List<Map<String, Object>> getResultForEnergyCost(Datacenter dc) {
        List<Map<String, Object>> dataCenterResult = new ArrayList<Map<String, Object>>();
        for (Map.Entry<Double, Map<Datacenter, List<Double>>> entry : results.entrySet()) {
            double key = entry.getKey();
            Map<Datacenter, List<Double>> values = entry.getValue();
            List<Double> valueList = values.get(dc);
            if (valueList != null) {
                String name = null;
                double greenEnergy = valueList.get(GreenDataCenter.VMS_IN);
                if (CloudController.SIMULATION_TYPE == CloudController.ENERGY_LOW_COST_VARIATION_SIMULATION) {
                    name = "VMs_in-low_price";
                } else if (CloudController.SIMULATION_TYPE == CloudController.ENERGY_HIGH_COST_VARIATION_SIMULATION) {
                    name = "VMs_in-high_price";
                } else if (CloudController.SIMULATION_TYPE == CloudController.LOW_LATENCY_VARIATION_SIMULATION) {
                    name = "Low_BW";
                } else if (CloudController.SIMULATION_TYPE == CloudController.HIGH_LATENCY_VARIATION_SIMULATION) {
                    name = "High_BW";
                }
                dataCenterResult.add(getMapResult(key, greenEnergy, name));
            }
        }
        return dataCenterResult;
    }

    private Map<String, Object> getMapResult(double key, double value, String name) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("name", name);
        data.put("time", key);
        data.put("val", truncateTwoDecimals(value));
        return data;
    }

    private double truncateTwoDecimals(double number) {
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.valueOf(df.format(number));
    }

    public void writeFile(Map<Double, Map<String, Double>> mapResult) {
        PrintWriter writer = null;
        String fileName;
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd HH_mm_ss");
            Date date = new Date();
            fileName = FILE_PATH + dateFormat.format(date) + FILE_EXTENSION;
            writer = new PrintWriter(fileName, ENCODING);
            String tableHeader = "Time," + "Green Energy," + "Brown Energy," + "Servers Energy,"
                    + "Cooling," + "Heat," + "VmsIn," + "VmsOut," + "DCVms,"
                    + "totalCloudVms," + "migratedVms";
            writer.println(tableHeader);
            for (Map.Entry<Double, Map<String, Double>> entry : mapResult.entrySet()) {
                double time = entry.getKey();
                Map<String, Double> dcResult = entry.getValue();
                writer.println(String.valueOf(time)
                        + "," + dcResult.get("greenEnergy")
                        + "," + dcResult.get("brownEnergy")
                        + "," + dcResult.get("serverEnergy")
                        + "," + dcResult.get("cooling")
                        + "," + dcResult.get("heat")
                        + "," + dcResult.get("VmsIn")
                        + "," + dcResult.get("VmsOut")
                        + "," + dcResult.get("dcVms")
                        + "," + dcResult.get("totalCloudVms")
                        + "," + dcResult.get("migratedVms"));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (writer != null) {
            writer.close();
        }
    }
}
