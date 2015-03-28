package com.springapp.mvc.model.abc;

import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;

import java.util.*;

/**
 * Created by Daniel on 3/14/2015.
 * Artificial Bee Colony class implements the algorithm
 */
public class ArtificialBeeColony {

    private static final int FOOD_SOURCES_NUMBER = 20;
    private final int DIMENSION;
    private Set<FoodSource> foodSourceSet;
    private List<GreenDataCenter> dataCenterList;
    private List<GreenVm> vmList;
    private List<EmployedBee> employedBeeList;
    private List<OnlookerBee> onlookerBeeList;
    private List<ScoutBee> scoutBeeList;

    public ArtificialBeeColony(List<GreenDataCenter> dataCenterList, List<GreenVm> vmList) {
        this.dataCenterList = dataCenterList;
        this.vmList = vmList;
        DIMENSION = vmList.size();
    }

    public boolean runAlgorithm() {
        boolean done = false;
        int epoch = 0;
        initialize();
        FoodSource bestFoodSource = getBestSolution();
        while (!done) {
            if (epoch < Constants.EPOCH_LIMIT) {
//                if (bestFoodSource.getConflictsNumber() == 0) {
//                    done = true;
//                }
                sendEmployedBees();
                sendOnlookerBees();
                bestFoodSource = getBestSolution();
                sendScoutBees();
                epoch++;
            } else {
                done = true;
            }
        }
        if (epoch == Constants.EPOCH_LIMIT) {
            done = false;
        }
        return done;
    }

    private FoodSource getBestSolution() {
        List<FoodSource> foodSourceList = new ArrayList<FoodSource>();
        foodSourceList.addAll(foodSourceSet);
        FoodSource minFoodSource = foodSourceList.get(0);
        for (FoodSource fs : foodSourceList) {
            if (fs.getFitness() < minFoodSource.getFitness()) {
                minFoodSource = fs;
            }
        }
        return minFoodSource;
    }

    /**
     * initializes the random food sources
     */
    private void initialize() {
        foodSourceSet = new HashSet<FoodSource>();
        employedBeeList = new ArrayList<EmployedBee>();
        int index = 0;
        while (index < FOOD_SOURCES_NUMBER) {
            List<Nectar> nectarList = initializeNectarList();
            FoodSource foodSource = new FoodSource(nectarList);
            if (foodSourceSet.add(foodSource)) {
                EmployedBee employedBee = new EmployedBee(index, foodSource);
                foodSource.setBee(employedBee);
                employedBeeList.add(employedBee);
                index++;
            }
        }
    }

    /**
     * This method initializes the nectar list of a particular food source
     *
     * @return the nectar list of the food source
     */
    private List<Nectar> initializeNectarList() {
        List<Nectar> nectarList = new ArrayList<Nectar>();
        for (GreenVm vm : vmList) {
            GreenHost selectedHost = getRandomHost();
            Nectar nectar = new Nectar(selectedHost, vm);
            nectar.setLatency();
            nectarList.add(nectar);
        }
        return nectarList;
    }

    private GreenHost getRandomHost() {
        Random random = new Random();
        int selectedHostIndex = random.nextInt(getHostList().size());
        return getHostList().get(selectedHostIndex);
    }

    private void sendEmployedBees() {
        List<FoodSource> list = new ArrayList<FoodSource>();
        list.addAll(foodSourceSet);
        for (FoodSource fs : list) {
            Bee bee = fs.getBee();
            bee.computeConflicts();
            bee.computeLatency();
            bee.applyFitnessFunction(dataCenterList);
        }
        int index = 0;
        for (FoodSource fs : list) {
            int neighbourBeeIndex =
                    getRandomNeighbourIndex(FOOD_SOURCES_NUMBER - 1, index);
            Bee currentBee = employedBeeList.get(index);
            Bee neighbourBee = employedBeeList.get(neighbourBeeIndex);
            currentBee.searchInNeighborhood(neighbourBee.getFoodSource(), DIMENSION, dataCenterList);
//            currentBee.computeProbability(list);
            index++;
        }
    }

    /*
     * de verificat in modelul initial
     */
    private void sendOnlookerBees() {
        if (onlookerBeeList == null) {
            onlookerBeeList = new ArrayList<OnlookerBee>();
        }
        List<FoodSource> list = new ArrayList<FoodSource>();
        list.addAll(foodSourceSet);
        for (FoodSource fs : list) {
            OnlookerBee bee;
            if (fs.getOnlookerBee() == null) {
                bee = new OnlookerBee(0, fs);
                fs.setOnlookerBee(bee);
                onlookerBeeList.add(bee);
            } else {
                bee = fs.getOnlookerBee();
            }
            bee.computeProbability(list);
        }
        int index = 0;
        int i = 0;
        Object[] foodSourceArray = foodSourceSet.toArray();
        for (int j = 0; j < foodSourceArray.length; j++) {
            list.add((FoodSource) foodSourceArray[j]);
        }
        while (index < FOOD_SOURCES_NUMBER) {
            Random random = new Random();
            double randomDouble = random.nextDouble();
            FoodSource currentFoodSource = list.get(index);
            if (randomDouble < currentFoodSource.getProbability()) {
                int neighbourBeeIndex =
                        getRandomNeighbourIndex(FOOD_SOURCES_NUMBER - 1, i);
                OnlookerBee onlookerBee = new OnlookerBee(index, currentFoodSource);
                onlookerBeeList.add(onlookerBee);
                FoodSource neighbourFoodSource = list.get(neighbourBeeIndex);
                onlookerBee.searchInNeighborhood(neighbourFoodSource, DIMENSION, dataCenterList);
                index++;
            }
            i++;
            if (i == FOOD_SOURCES_NUMBER) {
                i = 0;
            }
        }
    }

    private void sendScoutBees() {
        List<FoodSource> list = new ArrayList<FoodSource>();
        list.addAll(foodSourceSet);
        for (int i = 0; i < list.size(); i++) {
            FoodSource foodSource = list.get(i);
            if (foodSource.getTrialsNumber() >= Constants.TRIALS_LIMIT) {
                List<Nectar> nectarList = initializeNectarList();
                FoodSource newFoodSource = new FoodSource(nectarList);
                Bee bee = foodSource.getBee();
                newFoodSource.setBee(bee);
                foodSource = newFoodSource;
                bee.applyFitnessFunction(dataCenterList);
                bee.computeConflicts();
            }
        }
    }

    private int getRandomNeighbourIndex(int low, int high) {
        return (int) Math.round((high - low) * new Random().nextDouble() + low);
    }

    private List<GreenHost> getHostList() {
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
}
