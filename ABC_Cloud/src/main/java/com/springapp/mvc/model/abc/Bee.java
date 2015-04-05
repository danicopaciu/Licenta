package com.springapp.mvc.model.abc;

import com.springapp.mvc.controller.Resources;
import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;

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

        GreenHost prevHost = changedParameter.getHost();
        prevHost.removeMigratingVm(changedParameter.getVm());

        int prevHostId = prevHost.getId();
        int nextHostId = getNextHostId(neighbourFoodSource, changedParameterIndex);
        int newHostId = determineNewHostId(phi, prevHostId, nextHostId);

        GreenHost newHost = getNewHost(dataCenterList, newHostId);
        changedParameter.setHost(newHost);

        int actualConflict = computeConflicts();
        double actualFitnessFunction = applyFitnessFunction(dataCenterList);
        if (prevConflicts <= actualConflict && prevFitnessFunction > actualFitnessFunction) {
            changedParameter.setHost(prevHost);
            applyFitnessFunction(dataCenterList);
            computeConflicts();
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
        GreenHost nextHost = nectar.getHost();
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
        Map<GreenHost, Double> consumedEnergyMap = getGreenHostConsumedEnergyMap(dataCenterList);
        double dataCenterFitness = getDataCenterFitness(dataCenterList, consumedEnergyMap);
        double result = dataCenterFitness / dataCenterList.size();
        foodSource.setFitness(result);
        return result;
    }

    private double getDataCenterFitness(List<GreenDataCenter> dataCenterList, Map<GreenHost, Double> consumedEnergyMap) {
        double localFitness;
        double dataCenterFitness = 0;
        for (GreenDataCenter dc : dataCenterList) {
            List<GreenHost> dcHostList = dc.getHostList();
            localFitness = 0;
            for (GreenHost host : dcHostList) {
                localFitness += consumedEnergyMap.get(host);
            }
            double energy = dc.getGreenEnergyQuantity();
            if (energy != 0) {
                dataCenterFitness += localFitness / dc.getGreenEnergyQuantity();
            }
        }
        return dataCenterFitness;
    }

    private Map<GreenHost, Double> getGreenHostConsumedEnergyMap(List<GreenDataCenter> dataCenterList) {
        List<GreenHost> hostList = getHostList(dataCenterList);
        List<Nectar> nectarList = foodSource.getNectarList();
        Map<GreenHost, Double> consumedEnergyMap = new HashMap<GreenHost, Double>();
        for (GreenHost host : hostList) {
            List<GreenVm> vmList = new ArrayList<GreenVm>();
            if (!consumedEnergyMap.containsKey(host)) {
                for (Nectar nectar : nectarList) {
                    int hostId = nectar.getHost().getId();
                    if (hostId == host.getId()) {
                        vmList.add(nectar.getVm());
                    }
                }
            }
            double consumedEnergy = host.getMeanPower();
            double vmConsumedEnergy = 0;
            double ram = 0;
            for (GreenVm vm : vmList) {
                vmConsumedEnergy += vm.getNecessaryEnergy();
                ram += vm.getRam();
            }
            double penalty = ram / host.getAvailableBandwidth();
            double fitness;
            fitness = (consumedEnergy * (1 - penalty) + vmConsumedEnergy);
            consumedEnergyMap.put(host, fitness);
        }
        return consumedEnergyMap;
    }

    public int computeConflicts() {
        List<Nectar> nectarList = foodSource.getNectarList();
        int conflicts = 0;
        for (Nectar n : nectarList) {
            GreenHost host = n.getHost();
            GreenVm vm = n.getVm();
            if (!host.isMigrationPossible(vm)) {
                conflicts++;
            } else {
                host.addMigratingVm(vm);
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
