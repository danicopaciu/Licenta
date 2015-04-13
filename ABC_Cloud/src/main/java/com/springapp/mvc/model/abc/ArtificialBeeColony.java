package com.springapp.mvc.model.abc;

import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import com.springapp.mvc.model.csv.Log;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.*;

/**
 * Created by Daniel on 3/14/2015.
 * Artificial Bee Colony class implements the algorithm
 */
public class ArtificialBeeColony {

    public static final int TRIALS_LIMIT = 50;
    private final int FOOD_SOURCES_NUMBER = 5;
    private final int DIMENSION;

    private List<GreenDataCenter> dataCenterList;
    private List<GreenVm> vmList;
    private List<FoodSource> foodSourceList;


    public ArtificialBeeColony(List<GreenDataCenter> dataCenterList, List<GreenVm> vmList) {
        this.dataCenterList = dataCenterList;
        this.vmList = vmList;
        DIMENSION = vmList.size();
    }

    public FoodSource runAlgorithm() {
        double clock = CloudSim.clock();
        int epoch = 0;
        double counter = 0;
        double prevFitness = 0;
        FoodSource bestFoodSource = null;
        Log.printLine(clock + ": ABC algorithm starting");
        Log.printLine(clock + ": random initialization");
        initialize();

        do {
            System.out.println(epoch);

            if (bestFoodSource != null) {
                prevFitness = bestFoodSource.getFitness();
            }
            System.out.println(clock + ": Previous fitness function was: " + prevFitness);
            sendEmployedBees();
            applyFitness();
            computeProbability();
            sendOnlookerBees();
            bestFoodSource = getBestSolution();
            if (bestFoodSource.getFitness() > 1) {
                System.out.println();
            }
            System.out.println(clock + ": Actual fitness function is: " + bestFoodSource.getFitness());

            if (epoch >= 500) {
                initialize();
                epoch = 0;
            }
            if (prevFitness == bestFoodSource.getFitness()) {
                counter++;
            } else {
                counter = 0;
            }
            sendScoutBees();
            epoch++;
        } while (bestFoodSource.getConflictsNumber() != 0 || counter <= 50);

        bestFoodSource = getBestSolution();
        Log.printLine(clock + ": final best food source has fitness function: " + bestFoodSource.getFitness());
        for (Nectar n : bestFoodSource.getNectarList()) {
            Log.printLine(clock + ": Vm#" + n.getVm().getId() + " will be migrated to host#" + n.getHost().getId()
                    + " on Datacenter#" + n.getHost().getDatacenter().getId());
        }
        System.out.println("Number of epochs: " + epoch);
        System.out.println("Conflicts: " + bestFoodSource.getConflictsNumber());
        return bestFoodSource;
    }

    private FoodSource getBestSolution() {
        FoodSource maxFoodSource = foodSourceList.get(0);
        for (FoodSource fs : foodSourceList) {
            if (fs.getFitness() > maxFoodSource.getFitness()) {
                maxFoodSource = fs;
            }
        }
        return maxFoodSource;
    }

    /**
     * initializes the random food sources
     */
    private void initialize() {
        foodSourceList = new ArrayList<FoodSource>();
        int index = 0;
        Set<FoodSource> foodSourceSet = new HashSet<FoodSource>();
        while (index < FOOD_SOURCES_NUMBER) {
            List<Nectar> nectarList = initializeNectarList();
            FoodSource foodSource = new FoodSource(nectarList);
            if (foodSourceSet.add(foodSource)) {
                Bee employedBee = new Bee(foodSource);
                foodSource.setEmployedBee(employedBee);
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
            GreenHost selectedHost = getRandomHost(vm);
            Nectar nectar = new Nectar(selectedHost, vm);
            nectar.setLatency();
            nectarList.add(nectar);
        }
        return nectarList;
    }

    private GreenHost getRandomHost(Vm vm) {
        Random random = new Random();
        List<GreenHost> hosts = getHostList();
        GreenHost host;
        double greenEnergy;
        do {
            int selectedHostIndex = random.nextInt(hosts.size());
            host = hosts.get(selectedHostIndex);
            GreenDataCenter dataCenter = (GreenDataCenter) host.getDatacenter();
            greenEnergy = dataCenter.getGreenEnergyQuantity();
        } while (greenEnergy == 0 || !host.isMigrationPossible(vm));

        return host;
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
            FoodSource neighbourFoodSource = foodSourceList.get(neighbourBeeIndex);
            Bee neighbourBee = neighbourFoodSource.getEmployedBee();
            currentBee.searchInNeighborhood(neighbourBee.getFoodSource(), DIMENSION, dataCenterList);
            index++;
        }
    }

    private void sendOnlookerBees() {
        int index = 0;
        int i = 0;
        while (index < FOOD_SOURCES_NUMBER) {
            Random random = new Random();
            double randomDouble = random.nextDouble();
            FoodSource currentFoodSource = foodSourceList.get(i);
            double currentProbability = currentFoodSource.getProbability();
            if (randomDouble < currentProbability) {
                int neighbourBeeIndex =
                        getRandomNeighbourIndex(FOOD_SOURCES_NUMBER - 1, i);
                Bee onlookerBee = new Bee(currentFoodSource);
                currentFoodSource.setOnlookerBee(onlookerBee);
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
        for (FoodSource foodSource : foodSourceList) {
            if (foodSource.getTrialsNumber() >= TRIALS_LIMIT) {
                List<Nectar> nectarList = initializeNectarList();
                foodSource.setNectarList(nectarList);
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

    private void computeProbability() {
        for (FoodSource fs : foodSourceList) {
            Bee employedBee = fs.getEmployedBee();
            if (employedBee != null) {
                employedBee.computeProbability(foodSourceList);
            }
        }
    }

    private void applyFitness() {
        double minFitness = foodSourceList.get(0).getFitness();
        for (FoodSource fs : foodSourceList) {
            double currentFitness = fs.getFitness();
            if (currentFitness < minFitness) {
                minFitness = currentFitness;
            }
        }
        double maxFitness = foodSourceList.get(0).getFitness();
        for (FoodSource fs : foodSourceList) {
            double currentFitness = fs.getFitness();
            if (currentFitness > maxFitness) {
                maxFitness = currentFitness;
            }
        }

        for (FoodSource fs : foodSourceList) {
            double newFitness;
            if (maxFitness == minFitness) {
                newFitness = 100;
            } else {
                newFitness = ((fs.getFitness() - minFitness) / (maxFitness - minFitness)) * 100;
            }
            fs.setFitnessFactor(newFitness);
        }

    }
}
