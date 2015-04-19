package com.springapp.mvc.model.abc;

import com.springapp.mvc.controller.Resources;
import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

import java.util.*;

/**
 * Created by Daniel on 3/14/2015.
 * Bee class
 */
public class Bee {


    private FoodSource foodSource;


    public Bee(FoodSource foodSource) {
        this.foodSource = foodSource;
    }

    protected void searchInNeighborhood(FoodSource neighbourFoodSource, int dimension,
                                        List<GreenDataCenter> dataCenterList) {

        double prevFitnessFunction = foodSource.getFitness();
        int prevConflicts = foodSource.getConflictsNumber();

        int changedParameterIndex = determineChangedParameterIndex(dimension);

        double phi = determinePhi();

        Nectar changedParameter = getChangedParameter(changedParameterIndex);

        GreenHost prevHost = (GreenHost) changedParameter.getHost();
        foodSource.removeFromMigrationMap(prevHost, changedParameter.getVm());
//        prevHost.removeMigratingVm(changedParameter.getVm());

        int prevHostId = prevHost.getId();
        int nextHostId = getNextHostId(neighbourFoodSource, changedParameterIndex);
        int newHostId = determineNewHostId(phi, prevHostId, nextHostId);

        GreenHost newHost = getNewHost(dataCenterList, newHostId);
        changedParameter.setHost(newHost);

        int actualConflict = computeConflicts();
        double actualFitnessFunction = applyFitnessFunction(dataCenterList);
//        if(prevFitnessFunction > actualFitnessFunction){
//            System.out.println();
//        }

        if (prevConflicts < actualConflict ||
                (prevConflicts == actualConflict &&
                        prevFitnessFunction > actualFitnessFunction)) {
            foodSource.removeFromMigrationMap(newHost, changedParameter.getVm());
//            newHost.removeMigratingVm(changedParameter.getVm());
            changedParameter.setHost(prevHost);
            computeConflicts();
            applyFitnessFunction(dataCenterList);
            foodSource.incrementTrialsNumber();
        } else {
            foodSource.setTrialsNumber(0);
        }

    }

    private Nectar getChangedParameter(int changedParameterIndex) {
        List<Nectar> prevNectarList = foodSource.getNectarList();
        return prevNectarList.get(changedParameterIndex);
    }

    private GreenHost getNewHost(List<GreenDataCenter> dataCenterList, int newHostId) {
        List<GreenHost> hostList = getHostList(dataCenterList);
        return hostList.get(newHostId);
    }

    private int getNextHostId(FoodSource neighbourFoodSource, int changedParameterIndex) {
        List<Nectar> nextNectarList = neighbourFoodSource.getNectarList();
        Nectar nectar = nextNectarList.get(changedParameterIndex);
        GreenHost nextHost = (GreenHost) nectar.getHost();
        return nextHost.getId();
    }

    private int determineNewHostId(double phi, int prevHostId, int nextHostId) {
        int newHostId;
        newHostId = (int) (prevHostId + phi * (prevHostId - nextHostId));
        if (newHostId < 0) {
            newHostId = 0;
        }
        if (newHostId >= Resources.HOST_NUMBER) {
            newHostId = Resources.HOST_NUMBER - 1;
        }
        return newHostId;
    }

    private int determineChangedParameterIndex(int dimension) {
        Random random = new Random();
        return random.nextInt(dimension);
    }

    private double determinePhi() {
        Random random = new Random();
        return (random.nextDouble() - 0.5) * 2;
    }

    public FoodSource getFoodSource() {
        return foodSource;
    }

    private List<GreenHost> getHostList(List<GreenDataCenter> dataCenterList) {
        List<GreenHost> hostList = new ArrayList<GreenHost>();
        for (Datacenter d : dataCenterList) {
            List<Host> auxHostList = d.getHostList();
            for (Host h : auxHostList) {
                if (h instanceof GreenHost) {
                    hostList.add((GreenHost) h);
                }
            }
        }
        return hostList;
    }

    public double applyFitnessFunction(List<GreenDataCenter> dataCenterList) {
        Map<GreenHost, Double> consumedEnergyMap =
                getGreenHostConsumedEnergyMap(dataCenterList);
        double dataCenterFitness =
                getDataCenterFitness(dataCenterList, consumedEnergyMap);
        int dataCenterNumber = dataCenterList.size();
        double result = dataCenterFitness / dataCenterNumber;
        foodSource.setFitness(result);
        return result;
    }

    private double getDataCenterFitness(List<GreenDataCenter> dataCenterList,
                                        Map<GreenHost, Double> consumedEnergyMap) {
        double dataCenterFitness = 0;
        for (GreenDataCenter dc : dataCenterList) {
            double hostsEnergy = getHostsEnergy(consumedEnergyMap, dc);
            double greenEnergy = dc.getGreenEnergyQuantity();
            if (greenEnergy != 0) {
                double heat = getGainedHeat(hostsEnergy);
                double cooling = getCoolingEnergy(hostsEnergy);
                double penalty = computePenalty(dc);
                dataCenterFitness += ((hostsEnergy + cooling) / (greenEnergy + heat)) - penalty;
            }
        }
        return dataCenterFitness;
    }

    private double getCoolingEnergy(double hostsEnergy) {
        double cop = computeCOP();
        if (cop != 0) {
            return hostsEnergy / cop;
        }
        return 0;
    }

    private double getGainedHeat(double hostsEnergy) {
        return hostsEnergy * 3.5;
    }

    private double getHostsEnergy(Map<GreenHost,
            Double> consumedEnergyMap, Datacenter dc) {
        double hostsEnergy = 0;
        List<GreenHost> dcHostList = dc.getHostList();
        for (GreenHost host : dcHostList) {
            hostsEnergy += consumedEnergyMap.get(host);
        }
        return hostsEnergy;
    }

    private double computeCOP() {
        return 0.0068 * GreenDataCenter.SUPPLIED_TEMPERATURE *
                GreenDataCenter.SUPPLIED_TEMPERATURE +
                0.0008 * GreenDataCenter.SUPPLIED_TEMPERATURE + 0.458;
    }

    private Map<GreenHost, Double> getGreenHostConsumedEnergyMap(List<GreenDataCenter> dataCenterList) {
        List<GreenHost> hostList = getHostList(dataCenterList);
        Map<GreenHost, Double> consumedEnergyMap = new HashMap<GreenHost, Double>();
        for (GreenHost host : hostList) {
            double hostConsumedEnergy = 0;
            if (!consumedEnergyMap.containsKey(host)) {
                hostConsumedEnergy = host.getMeanPower();
                List<Vm> assignedVms = foodSource.getVmListForHost(host);
                if (assignedVms != null) {
                    for (Vm vm : foodSource.getVmListForHost(host)) {
                        GreenVm greenVm = (GreenVm) vm;
                        hostConsumedEnergy += greenVm.getNecessaryEnergy();
                    }
                }
            }
            consumedEnergyMap.put(host, hostConsumedEnergy);
        }
        return consumedEnergyMap;
    }

    private double computePenalty(Datacenter dataCenter) {
        double penalty = 0;
        double ram;
        int vmNumber = 0;
        List<GreenHost> hostList = dataCenter.getHostList();
        for (GreenHost host : hostList) {
            ram = 0;
            List<Vm> assignedVms = foodSource.getVmListForHost(host);
            if (assignedVms != null) {
                for (Vm vm : foodSource.getVmListForHost(host)) {
                    ram += vm.getRam();
                    vmNumber++;
                }
            }
            penalty += ram / host.getAvailableBandwidth();
        }
//        penalty /= vmNumber;
        return penalty / Resources.SCHEDULING_INTERVAL;
    }

    public int computeConflicts() {
        List<Nectar> nectarList = foodSource.getNectarList();
        int conflicts = 0;
        for (Nectar n : nectarList) {
            GreenHost host = (GreenHost) n.getHost();
            GreenVm vm = (GreenVm) n.getVm();
            List<Vm> assignedVms = foodSource.getVmListForHost(host);
            if (!host.isMigrationPossible(vm, assignedVms)) {
                conflicts++;
            } else {
                foodSource.addToMigrationMap(host, vm);
            }

        }
        foodSource.setConflictsNumber(conflicts);
        return conflicts;
    }

    public double computeProbability(List<FoodSource> foodSourceList) {
        double maxFitness = foodSourceList.get(0).getFitnessFactor();
        for (FoodSource fs : foodSourceList) {
            double currentFitness = fs.getFitnessFactor();
            if (currentFitness > maxFitness) {
                maxFitness = currentFitness;
            }
        }
        double probability = (0.9 * (foodSource.getFitnessFactor() / maxFitness)) + 0.1;
        foodSource.setProbability(probability);
        return probability;
    }

}
