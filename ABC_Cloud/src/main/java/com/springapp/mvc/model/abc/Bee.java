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


        Nectar changedParameter = getChangedParameter(changedParameterIndex);

        GreenHost prevHost = (GreenHost) changedParameter.getHost();
        foodSource.removeFromMigrationMap(prevHost, changedParameter.getVm());
        int nextHostId = getNextHostId(neighbourFoodSource, changedParameterIndex);
        GreenHost newHost;
        do {
            newHost = determineNewHost(changedParameter, nextHostId, dataCenterList);
        } while (newHost.getDatacenter() == changedParameter.getVm().getHost().getDatacenter());

        changedParameter.setHost(newHost);

        int actualConflict = computeConflicts();
        applyFitnessFunction(dataCenterList);
        double actualFitnessFunction = foodSource.getFitness();

        if (prevConflicts < actualConflict ||
                prevConflicts == actualConflict) {
            double prevDiff = Math.abs(1 - prevFitnessFunction);
            double currDiff = Math.abs(1 - actualFitnessFunction);
            if (prevDiff < currDiff) {
                foodSource.removeFromMigrationMap(newHost, changedParameter.getVm());
                changedParameter.setHost(prevHost);
                computeConflicts();
                applyFitnessFunction(dataCenterList);
                foodSource.incrementTrialsNumber();
            }
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

    private GreenHost determineNewHost(Nectar nectar,
                                       int nextHostId, List<GreenDataCenter> dataCenterList) {
        int newHostId;
        Host prevHost = nectar.getHost();
        int prevHostId = prevHost.getId();
        double phi = determinePhi();
        newHostId = (int) (prevHostId + phi * (prevHostId - nextHostId));
        if (newHostId < 0) {
            newHostId = 0;
        }
        if (newHostId >= Resources.HOST_NUMBER) {
            newHostId = Resources.HOST_NUMBER - 1;
        }
        List<GreenHost> hostList = getHostList(dataCenterList);
        Host newHost = hostList.get(newHostId);

        boolean isChanged = false;
        Vm vm = nectar.getVm();
        if (newHost.getDatacenter() == vm.getHost().getDatacenter()) {
            if (newHost.getDatacenter() == dataCenterList.get(0)) {
                newHostId += Resources.HOST_NUMBER_PER_DATACENTER;
                isChanged = true;
            } else if (newHost.getDatacenter() == dataCenterList.get(dataCenterList.size() - 1)) {
                newHostId -= Resources.HOST_NUMBER_PER_DATACENTER;
                isChanged = true;
            } else {
                Random random = new Random();
                if (random.nextInt() % 2 == 0) {
                    newHostId += Resources.HOST_NUMBER_PER_DATACENTER;
                } else {
                    newHostId -= Resources.HOST_NUMBER_PER_DATACENTER;
                }
                isChanged = true;
            }

        }
        if (isChanged) {
            newHost = hostList.get(newHostId);
        }
        return (GreenHost) newHost;
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
        foodSource.setFitness(0);
        Map<GreenHost, Double> consumedEnergyMap =
                getGreenHostConsumedEnergyMap(dataCenterList);
        getDataCenterFitness(dataCenterList, consumedEnergyMap);
        return foodSource.getFitness();
    }

    private double getDataCenterFitness(List<GreenDataCenter> dataCenterList,
                                        Map<GreenHost, Double> consumedEnergyMap) {
        int greenDataCenters = 0;
        for (GreenDataCenter dc : dataCenterList) {
            double greenEnergy = dc.getGreenEnergyQuantity();
            if (greenEnergy >= 0.5) {
                greenDataCenters++;
                double hostsEnergy = getHostsEnergy(consumedEnergyMap, dc);
                double coolingFactor = 0;
                double heatFactor = 0;
                double heat = getGainedHeat(hostsEnergy);
                double cooling = getCoolingEnergy(hostsEnergy);
                double penalty = computePenalty(dc);
                double result = ((hostsEnergy + coolingFactor * cooling) /
                        (greenEnergy + heatFactor * heat)) - penalty;
                foodSource.setFitness(foodSource.getFitness() + result);
            }
        }
        if (foodSource.getFitness() != 0) {
            foodSource.setFitness(foodSource.getFitness() / greenDataCenters);
        }
        return foodSource.getFitness();
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
                    double vmEnergy = 0;
                    for (Vm vm : foodSource.getVmListForHost(host)) {
                        GreenVm greenVm = (GreenVm) vm;
                        vmEnergy += greenVm.getNecessaryEnergy();
                    }
                    hostConsumedEnergy += vmEnergy;
                }
            }

            consumedEnergyMap.put(host, hostConsumedEnergy);
        }
        return consumedEnergyMap;
    }

    private double computePenalty(Datacenter dataCenter) {
        double penalty = 0;
        double ram;
        List<GreenHost> hostList = dataCenter.getHostList();
        for (GreenHost host : hostList) {
            ram = 0;
            List<Vm> assignedVms = foodSource.getVmListForHost(host);
            if (assignedVms != null) {
                for (Vm vm : assignedVms) {
                    ram += vm.getRam();
                }
            }
            penalty += ram / host.getAvailableBandwidth();
        }
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
