package com.springapp.mvc.model.cloud;

import com.springapp.mvc.model.abc.FoodSource;
import com.springapp.mvc.model.abc.Nectar;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by Daniel on 5/17/2015.
 * CloudStatistics
 */
public class CloudStatistics {

    private Map<Double, Map<Datacenter, List<Double>>> results;
    private int totalCloudVms;
    private int migratedVms;

    public CloudStatistics() {
        this.results = new TreeMap<Double, Map<Datacenter, List<Double>>>();
    }

    public void addSolutionResult(Double time, List<GreenDataCenter> datacenters, FoodSource solution) {
        Map<Datacenter, List<Double>> stepResults = new HashMap<Datacenter, List<Double>>();

        Map<Datacenter, int[]> result = analyzeSolution(solution, datacenters);
        totalCloudVms = 0;
        for (GreenDataCenter dc : datacenters) {
            int[] vms = result.get(dc);
            totalCloudVms += getOverallVms(dc);
            addPartialResult(stepResults, dc, GreenDataCenter.VMS_IN, vms[0]);
            addPartialResult(stepResults, dc, GreenDataCenter.VMS_OUT, vms[1]);
            int totalVms = dc.getTotalVms() - vms[1] + vms[0];
            dc.setTotalVms(totalVms);
            addPartialResult(stepResults, dc, GreenDataCenter.TOTAL_VMS, dc.getTotalVms());
            addPartialResult(stepResults, dc, GreenDataCenter.GREEN_ENERGY, dc.getGreenEnergyQuantity());
            addPartialResult(stepResults, dc, GreenDataCenter.BROWN_ENERGY, dc.getBrownEnergyQuantity());
            addPartialResult(stepResults, dc, GreenDataCenter.SERVERS_ENERGY, dc.getPower());
            addPartialResult(stepResults, dc, GreenDataCenter.HEAT, dc.getHeatGained());
            addPartialResult(stepResults, dc, GreenDataCenter.COOLING, dc.getCoolingEnergy());
        }
        results.put(time, stepResults);
    }

    private Map<Datacenter, int[]> analyzeSolution(FoodSource solution, List<GreenDataCenter> datacenters) {
        Map<Datacenter, int[]> result = new HashMap<Datacenter, int[]>();
        for (GreenDataCenter dc : datacenters) {
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
            stepResults.put(dc, new ArrayList<Double>(Collections.nCopies(8, 0.0)));
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
                dataCenterResult.put(key, data);
            }
        }
        return dataCenterResult;
    }

    public List<Map<String, Object>> getGraphResultsForDatacenter(Datacenter dc) {
        List<Map<String, Object>> dataCenterResult = new ArrayList<Map<String, Object>>();
        for (Map.Entry<Double, Map<Datacenter, List<Double>>> entry : results.entrySet()) {
            double key = entry.getKey();
            Map<Datacenter, List<Double>> values = entry.getValue();
            List<Double> valueList = values.get(dc);
            if (valueList != null) {
                double greenEnergy = valueList.get(GreenDataCenter.GREEN_ENERGY);
                dataCenterResult.add(getMapResult(key, greenEnergy, "GreenEnergy"));
                double serverEnergy = valueList.get(GreenDataCenter.SERVERS_ENERGY);
                dataCenterResult.add(getMapResult(key, serverEnergy, "ServerEnergy"));
            }
        }
        return dataCenterResult;
    }

    private Map<String, Object> getMapResult(double key, double value, String name) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("name", name);
        data.put("time", key);
        data.put("energy", truncateTwoDecimals(value));
        return data;
    }

    private double truncateTwoDecimals(double number) {
        DecimalFormat df = new DecimalFormat("#.##");
        return Double.valueOf(df.format(number));
    }
}
