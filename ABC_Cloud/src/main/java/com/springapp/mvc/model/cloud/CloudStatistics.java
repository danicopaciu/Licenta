package com.springapp.mvc.model.cloud;

import com.springapp.mvc.model.abc.FoodSource;
import com.springapp.mvc.model.abc.Nectar;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

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
        this.results = new HashMap<Double, Map<Datacenter, List<Double>>>();
    }

    public void addSolutionResult(Double time, List<GreenDataCenter> datacenters, FoodSource solution) {
        Map<Datacenter, List<Double>> stepResults = new HashMap<Datacenter, List<Double>>();
        Map<Datacenter, int[]> result = new HashMap<Datacenter, int[]>();
        for (GreenDataCenter dc : datacenters) {
            int[] vms = new int[2];
            vms[0] = vms[1] = 0;
            result.put(dc, vms);
        }
        analyzeSolution(solution, result);
        totalCloudVms = 0;
        for (GreenDataCenter dc : datacenters) {
            int[] vms = result.get(dc);
            totalCloudVms += getOverallVms(dc);
            addPartialResult(stepResults, dc, GreenDataCenter.VMS_IN, vms[0]);
            addPartialResult(stepResults, dc, GreenDataCenter.VMS_OUT, vms[1]);
            addPartialResult(stepResults, dc, GreenDataCenter.TOTAL_VMS, dc.getTotalVms() - vms[1] + vms[0]);
            addPartialResult(stepResults, dc, GreenDataCenter.GREEN_ENERGY, dc.getGreenEnergyQuantity());
            addPartialResult(stepResults, dc, GreenDataCenter.BROWN_ENERGY, dc.getBrownEnergyQuantity());
            addPartialResult(stepResults, dc, GreenDataCenter.SERVERS_ENERGY, dc.getPower());
            addPartialResult(stepResults, dc, GreenDataCenter.HEAT, dc.getHeatGained());
            addPartialResult(stepResults, dc, GreenDataCenter.COOLING, dc.getCoolingEnergy());
        }
        results.put(time, stepResults);
    }

    private void analyzeSolution(FoodSource solution, Map<Datacenter, int[]> result) {
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
        values.add(type, value);
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
        data.put("migratedVms", (double) getMigratedVms());

        return data;
    }
}
