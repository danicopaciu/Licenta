package com.springapp.mvc.model.abc;

import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import com.springapp.mvc.model.csv.Log;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.*;

/**
 * Created by Daniel on 3/14/2015.
 * Artificial Bee Colony class implements the algorithm
 */
public class ArtificialBeeColony {

    public static final int EPOCH_LIMIT = 100;
    public static final int TRIALS_LIMIT = 20;
    private final int FOOD_SOURCES_NUMBER = 20;
    private final int DIMENSION;
    private List<FoodSource> foodSourceList;
    private List<GreenDataCenter> dataCenterList;
    private List<GreenVm> vmList;
    private List<Bee> employedBeeList;
    private List<Bee> onlookerBeeList;

    public ArtificialBeeColony(List<GreenDataCenter> dataCenterList, List<GreenVm> vmList) {
        this.dataCenterList = dataCenterList;
        this.vmList = vmList;
        DIMENSION = vmList.size();
    }

    public FoodSource runAlgorithm() {
        double clock = CloudSim.clock();
        Log.printLine(clock + ": ABC algorithm starting");
        boolean done = false;
        int epoch = 0;
        initialize();
        Log.printLine(clock + ": random initialization");
        FoodSource bestFoodSource = null;
        while (!done) {
            if (epoch < EPOCH_LIMIT) {
                Log.printLine(clock + ": epoch: " + epoch);
                Log.printLine(clock + ": employed bees phase is starting...");
                sendEmployedBees();
                Log.printLine(clock + ": onlooker bees phase is starting...");
                sendOnlookerBees();
                bestFoodSource = getBestSolution();
                Log.printLine(clock + ": temporary best food source has fitness function: " + bestFoodSource.getFitness());
                Log.printLine(clock + ": scout bees phase is starting...");
                sendScoutBees();
                epoch++;
            } else {
                done = true;
                bestFoodSource = getBestSolution();
                Log.printLine(clock + ": final best food source has fitness function: " + bestFoodSource.getFitness());
                for (Nectar n : bestFoodSource.getNectarList()) {
                    Log.printLine(clock + ": Vm#" + n.getVm().getId() + " will be migrated to host#" + n.getHost().getId()
                            + " on Datacenter#" + n.getHost().getDatacenter().getId());
                }
            }
        }

        return bestFoodSource;
    }

    private FoodSource getBestSolution() {
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
        foodSourceList = new ArrayList<FoodSource>();
        employedBeeList = new ArrayList<Bee>();
        int index = 0;
        Set<FoodSource> foodSourceSet = new HashSet<FoodSource>();
        while (index < FOOD_SOURCES_NUMBER) {
            List<Nectar> nectarList = initializeNectarList();
            FoodSource foodSource = new FoodSource(nectarList);
            if (foodSourceSet.add(foodSource)) {
                Bee employedBee = new Bee(foodSource);
                foodSource.setEmployedBee(employedBee);
                employedBeeList.add(employedBee);
                index++;
            }
        }
        if (foodSourceSet.size() != 0) {
            foodSourceList.addAll(foodSourceSet);
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
        List<GreenHost> hosts = getHostList();
        int selectedHostIndex = random.nextInt(hosts.size());
        return hosts.get(selectedHostIndex);
    }

    private void sendEmployedBees() {
        for (FoodSource fs : foodSourceList) {
            Bee bee = fs.getEmployedBee();
            bee.computeConflicts();
            bee.applyFitnessFunction(dataCenterList);
        }
        int index = 0;
        for (FoodSource fs : foodSourceList) {
            int neighbourBeeIndex =
                    getRandomNeighbourIndex(FOOD_SOURCES_NUMBER - 1, index);
            Bee currentBee = fs.getEmployedBee();
            Bee neighbourBee = employedBeeList.get(neighbourBeeIndex);
            currentBee.searchInNeighborhood(neighbourBee.getFoodSource(), DIMENSION, dataCenterList);
            currentBee.computeProbability(foodSourceList);
            index++;
        }
    }

    private void sendOnlookerBees() {
        if (onlookerBeeList == null) {
            onlookerBeeList = new ArrayList<Bee>();
        }
        int index = 0;
        int i = 0;
        while (index < FOOD_SOURCES_NUMBER) {
            Random random = new Random();
            double randomDouble = random.nextDouble();
            FoodSource currentFoodSource = foodSourceList.get(i);
            randomDouble /= 10;
            if (randomDouble < currentFoodSource.getProbability()) {
                int neighbourBeeIndex =
                        getRandomNeighbourIndex(FOOD_SOURCES_NUMBER - 1, i);
                Bee onlookerBee = new Bee(currentFoodSource);
                onlookerBeeList.add(onlookerBee);
                FoodSource neighbourFoodSource = foodSourceList.get(neighbourBeeIndex);
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
        int counter = 0;
        for (FoodSource foodSource : foodSourceList) {
            if (foodSource.getTrialsNumber() >= TRIALS_LIMIT) {
                counter++;
                List<Nectar> nectarList = initializeNectarList();
                foodSource.setNectarList(nectarList);
            }
        }
        Log.printLine(CloudSim.clock() + " : " + counter + " solutions have been abandoned");
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
