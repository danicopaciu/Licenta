package com.springapp.mvc.model.abc;

import com.springapp.mvc.controller.CloudController;
import com.springapp.mvc.controller.Resources;
import com.springapp.mvc.model.abc.fitness.FitnessFactory;
import com.springapp.mvc.model.abc.fitness.FitnessFunction;
import com.springapp.mvc.model.abc.fitness.LatencyPenalty;
import com.springapp.mvc.model.abc.fitness.Penalty;
import com.springapp.mvc.model.cloud.FederationOfDataCenter;
import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Bee {


    private FoodSource foodSource;

    public Bee(FoodSource foodSource) {
        this.foodSource = foodSource;
    }

    protected void searchInNeighborhood(FoodSource neighbourFoodSource, int dimension,
                                        FederationOfDataCenter fed) {

        double prevFitnessFunction = foodSource.getFitness();
        int changedParameterIndex = determineChangedParameterIndex(dimension);
        Nectar changedParameter = getChangedParameter(changedParameterIndex);
        GreenHost prevHost = (GreenHost) changedParameter.getHost();
        foodSource.removeFromMigrationMap(prevHost, changedParameter.getVm());
        Host nextHost = getNextHost(neighbourFoodSource, changedParameterIndex);
        GreenHost newHost;
        do {
            newHost = determineNewHost(changedParameter, nextHost, fed);
        } while (newHost.getDatacenter() == changedParameter.getVm().getHost().getDatacenter());

        if (newHost.isMigrationPossible(changedParameter.getVm(), foodSource.getVmListForHost(newHost))) {
            changedParameter.setHost(newHost);
            foodSource.addToMigrationMap(newHost, changedParameter.getVm());

            applyFitnessFunction(fed.getGreenDatacenter());
            double actualFitnessFunction = foodSource.getFitness();
            double prevDiff = Math.abs(1 - prevFitnessFunction);
            double currDiff = Math.abs(1 - actualFitnessFunction);

            if (prevDiff < currDiff) {
                foodSource.removeFromMigrationMap(newHost, changedParameter.getVm());
                foodSource.addToMigrationMap(prevHost, changedParameter.getVm());
                changedParameter.setHost(prevHost);
                foodSource.setFitness(prevFitnessFunction);
                foodSource.incrementTrialsNumber();
            } else {
                foodSource.setTrialsNumber(0);
            }
        }
    }

    private Nectar getChangedParameter(int changedParameterIndex) {
        List<Nectar> prevNectarList = foodSource.getNectarList();
        return prevNectarList.get(changedParameterIndex);
    }

    private Host getNextHost(FoodSource neighbourFoodSource, int changedParameterIndex) {
        List<Nectar> nextNectarList = neighbourFoodSource.getNectarList();
        Nectar nectar = nextNectarList.get(changedParameterIndex);
        return nectar.getHost();
    }

    private GreenHost determineNewHost(Nectar nectar,
                                       Host neighborHost, FederationOfDataCenter fed) {
        List<Datacenter> dataCenterList = fed.getDataCenterList();
        Host prevHost = nectar.getHost();
        Datacenter prevDc = prevHost.getDatacenter();
        int prevDcId = prevDc.getId() - 3;
        int neighbourDcId = neighborHost.getDatacenter().getId() - 3;
        double phi = determinePhi();
        int newDcId;
        if (foodSource.getFitness() < 1) {
            newDcId = (int) ((prevDcId + phi * (prevDcId - neighbourDcId)) / Resources.DATACENTER_NUMBER);
        } else {
            newDcId = (int) (prevDcId + phi * (prevDcId - neighbourDcId));
        }
        if (newDcId < 0) {
            newDcId = 0;
        }
        if (newDcId >= Resources.DATACENTER_NUMBER) {
            newDcId = Resources.DATACENTER_NUMBER - 1;
        }
        Datacenter newDc = dataCenterList.get(newDcId);
        List<Host> hosts = newDc.getHostList();
        int minHostId = hosts.get(0).getId();
        int maxHostId = hosts.get(hosts.size() - 1).getId();
        int newHostId = new Random().nextInt(maxHostId - minHostId + 1) + minHostId;

        List<Host> hostList = fed.getHostList();
        Host newHost = hostList.get(newHostId);
        int hostPerDataCenter = hostList.size() / Resources.DATACENTER_NUMBER;
        boolean isChanged = false;
        Vm vm = nectar.getVm();
        if (newHost.getDatacenter() == vm.getHost().getDatacenter()) {
            if (newHost.getDatacenter() == dataCenterList.get(0)) {
                newHostId += hostPerDataCenter;
                isChanged = true;
            } else if (newHost.getDatacenter() == dataCenterList.get(dataCenterList.size() - 1)) {
                newHostId -= hostPerDataCenter;
                isChanged = true;
            } else {
                Random random = new Random();
                if (random.nextInt() % 2 == 0) {
                    newHostId += hostPerDataCenter;
                } else {
                    newHostId -= hostPerDataCenter;
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

    public double applyFitnessFunction(GreenDataCenter dataCenter) {
        foodSource.setFitness(0);
        Map<GreenHost, Double> consumedEnergyMap =
                getGreenHostConsumedEnergyMap(dataCenter);
        getDataCenterFitness(dataCenter, consumedEnergyMap);
        return foodSource.getFitness();
    }

    private double getDataCenterFitness(GreenDataCenter dc,
                                        Map<GreenHost, Double> consumedEnergyMap) {

        double greenEnergy = dc.getGreenEnergyQuantity();
        double hostsEnergy = getHostsEnergy(consumedEnergyMap, dc);
        double prevError = dc.getError();
        if (prevError != 0) {
            hostsEnergy *= prevError;
        }
        double heat = getGainedHeat(hostsEnergy);
        double cooling = getCoolingEnergy(hostsEnergy);
        Penalty p = new LatencyPenalty();
        double penalty = p.getPenalty(foodSource, dc);
        FitnessFunction fitnessFunction = FitnessFactory.getFitnessFunction(CloudController.SIMULATION_TYPE);
        if (fitnessFunction != null) {
            double newResult = fitnessFunction.computeFitness(greenEnergy, hostsEnergy, heat, cooling, penalty);
            foodSource.setFitness(newResult);
            foodSource.getPredictedEnergy().put(dc, hostsEnergy);
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
            Double> consumedEnergyMap, GreenDataCenter dc) {
        double hostsEnergy = 0;
        List<GreenHost> dcHostList = dc.getHostList();
        for (GreenHost host : dcHostList) {
            hostsEnergy += consumedEnergyMap.get(host);
        }
        return hostsEnergy + dc.getPower();
    }

    private double computeCOP() {
        return 0.0068 * GreenDataCenter.SUPPLIED_TEMPERATURE *
                GreenDataCenter.SUPPLIED_TEMPERATURE +
                0.0008 * GreenDataCenter.SUPPLIED_TEMPERATURE + 0.458;
    }

    private Map<GreenHost, Double> getGreenHostConsumedEnergyMap(GreenDataCenter dataCenter) {
        List<GreenHost> hostList = dataCenter.getHostList();
        Map<GreenHost, Double> consumedEnergyMap = new HashMap<GreenHost, Double>();
        for (GreenHost host : hostList) {
            double hostConsumedEnergy = 0;
            if (!consumedEnergyMap.containsKey(host)) {
                double vmEnergy = 0;
                List<Vm> assignedVms = foodSource.getVmListForHost(host);
                if (assignedVms != null) {
                    for (Vm vm : foodSource.getVmListForHost(host)) {
                        GreenVm greenVm = (GreenVm) vm;
                        vmEnergy += greenVm.getNecessaryEnergy();
                    }
                    hostConsumedEnergy += vmEnergy;
                }
                vmEnergy = 0;
                List<Vm> migratingOutVms = foodSource.getMigratingOutVmsForHost(host);
                if (migratingOutVms != null) {
                    for (Vm vm : foodSource.getMigratingOutVmsForHost(host)) {
                        GreenVm greenVm = (GreenVm) vm;
                        vmEnergy += greenVm.getNecessaryEnergy();
                    }
                    hostConsumedEnergy -= vmEnergy;
                }
            }


            consumedEnergyMap.put(host, hostConsumedEnergy);
        }
        return consumedEnergyMap;
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
