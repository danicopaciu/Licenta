package com.springapp.mvc.model.abc;

import com.springapp.mvc.controller.Resources;
import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

        int prevHostId = prevHost.getId();
        int nextHostId = getNextHostId(neighbourFoodSource, changedParameterIndex);
        int newHostId = determineNewHostId(phi, prevHostId, nextHostId);

        GreenHost newHost = getNewHost(dataCenterList, newHostId);

        changedParameter.setHost(newHost);

        int actualConflict = computeConflicts();
        double actualFitnessFunction = applyFitnessFunction(dataCenterList);
        if (prevConflicts < actualConflict && prevFitnessFunction > actualFitnessFunction) {
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
        List<Nectar> nectarList = foodSource.getNectarList();
        double result = 0;
        for (GreenDataCenter dc : dataCenterList) {
            double maxLatency = 0;
            double vmEnergy = 0;
            for (Nectar n : nectarList) {
                GreenHost host = n.getHost();
                String currentDataCenterName = host.getDatacenter().getName();
                if (currentDataCenterName != null && currentDataCenterName.equals(dc.getName())) {
                    GreenVm vm = n.getVm();
                    double necessaryEnergy = vm.getNecessaryEnergy();
                    vmEnergy += necessaryEnergy;
                    if (n.getLatency() > maxLatency) {
                        maxLatency = n.getLatency();
                    }
                }
            }
            double energy;
            if (dc.getGreenEnergyQuantity() <= 0) {
                energy = 0;
            } else {
                energy = vmEnergy / dc.getGreenEnergyQuantity();
            }
            energy -= energy * maxLatency;
            if (energy < 0) {
                energy = 0;
            }
            result += energy;
        }

        foodSource.setFitness(result);
        return result;
    }

    public int computeConflicts() {
        List<Nectar> nectarList = foodSource.getNectarList();
        int conflicts = 0;
        for (Nectar n : nectarList) {
            GreenHost host = n.getHost();
            GreenVm vm = n.getVm();
            if (!host.isSuitableForVm(vm)) {
                conflicts++;
            }
        }
        foodSource.setConflictsNumber(conflicts);
        return conflicts;
    }

    public double computeProbability(List<FoodSource> foodSourceList) {
        double fitnessSum = 0;
        for (FoodSource fs : foodSourceList) {
            double fitnessValue = fs.getFitness();
            fitnessSum += fitnessValue;
        }
        FoodSource thisFoodSource = getFoodSource();
        double thisFitnessValue = thisFoodSource.getFitness();
        double thisProbability = thisFitnessValue / fitnessSum;
        thisFoodSource.setProbability(thisProbability);
        return thisProbability;
    }

}
