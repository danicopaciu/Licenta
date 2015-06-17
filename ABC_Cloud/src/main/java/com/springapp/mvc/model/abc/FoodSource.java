package com.springapp.mvc.model.abc;

import com.springapp.mvc.model.cloud.GreenDataCenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

import java.util.*;

public class FoodSource implements Comparable<FoodSource> {

    private List<Nectar> nectarList;

    private double fitness;

    private double fitnessFactor;

    private double probability;

    private int trialsNumber;

    private int conflictsNumber;

    private Map<Host, List<Vm>> migrationMap;

    private Map<Host, List<Vm>> migrationOutVms;

    private Map<GreenDataCenter, Double> predictedEnergy;

    private Bee employedBee;

    private Bee onlookerBee;

    public FoodSource() {
        this.fitness = 0.0;
        this.probability = 0.0;
        this.trialsNumber = 0;
        this.conflictsNumber = 0;
        this.nectarList = new LinkedList<Nectar>();
        this.migrationMap = new HashMap<Host, List<Vm>>();
        this.migrationOutVms = new HashMap<Host, List<Vm>>();
        this.predictedEnergy = new HashMap<GreenDataCenter, Double>();

    }

    public boolean addNectar(Nectar n) {
        addToMigrationMap(n.getHost(), n.getVm());
        return nectarList.add(n);
    }

    public Bee getEmployedBee() {
        return employedBee;
    }

    public void setEmployedBee(Bee employedBee) {
        this.employedBee = employedBee;
    }

    public void setOnlookerBee(Bee onlookerBee) {
        this.onlookerBee = onlookerBee;
    }

    public List<Nectar> getNectarList() {
        return nectarList;
    }

    public void setNectarList(List<Nectar> nectarList) {
        this.nectarList = nectarList;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public int getTrialsNumber() {
        return trialsNumber;
    }

    public void setTrialsNumber(int trialsNumber) {
        this.trialsNumber = trialsNumber;
    }

    public int getConflictsNumber() {
        return conflictsNumber;
    }

    public double getFitnessFactor() {
        return fitnessFactor;
    }

    public void setFitnessFactor(double fitnessFactor) {
        this.fitnessFactor = fitnessFactor;
    }

    @Override
    public int compareTo(FoodSource o) {
        return this.conflictsNumber - o.getConflictsNumber();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FoodSource) {
            int equalElements = 0;
            for (Nectar index1 : nectarList) {
                FoodSource comparisonObj = (FoodSource) obj;
                for (Nectar index2 : comparisonObj.getNectarList()) {
                    if (index1.equals(index2)) {
                        equalElements++;
                    }
                }
            }
            if (equalElements >= 4) {
                return true;
            }
        }
        return false;
    }

    public void incrementTrialsNumber() {
        trialsNumber++;
    }

    public void addToMigrationMap(Host host, Vm vm) {
        if (migrationMap.containsKey(host)) {
            setMigrationMapEntry(host, vm);
        } else {
            createMigrationMapEntry(host, vm);
        }
    }

    private void createMigrationMapEntry(Host host, Vm vm) {
        List<Vm> vmList = new ArrayList<Vm>();
        vmList.add(vm);
        migrationMap.put(host, vmList);
    }

    private boolean setMigrationMapEntry(Host host, Vm vm) {
        List<Vm> vmList = migrationMap.get(host);
        return !vmList.contains(vm) && vmList.add(vm);
    }

    public void removeFromMigrationMap(Host host, Vm vm) {
        if (migrationMap.containsKey(host)) {
            List<Vm> vmList = migrationMap.get(host);
            if (vmList.contains(vm)) {
                vmList.remove(vm);
            }
        }
    }

    public List<Vm> getVmListForHost(Host host) {
        if (migrationMap.containsKey(host)) {
            return migrationMap.get(host);
        }
        return null;
    }

    public Map<Host, List<Vm>> getMigrationMap() {
        return migrationMap;
    }

    public void setMigrationMap(Map<Host, List<Vm>> migrationMap) {
        this.migrationMap = migrationMap;
    }

    public void addMigrationOutVm(Host host, Vm vm) {
        if (!migrationOutVms.containsKey(host)) {
            migrationOutVms.put(host, new ArrayList<Vm>());
        }
        List<Vm> vmList = migrationOutVms.get(host);
        if (!vmList.contains(vm)) {
            vmList.add(vm);
        }
    }

    public List<Vm> getMigratingOutVmsForHost(Host host) {
        return migrationOutVms.get(host);
    }

    public Map<GreenDataCenter, Double> getPredictedEnergy() {
        return predictedEnergy;
    }

    public void setPredictedEnergy(Map<GreenDataCenter, Double> predictedEnergy) {
        this.predictedEnergy = predictedEnergy;
    }
}
