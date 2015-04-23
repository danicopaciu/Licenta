package com.springapp.mvc.model.abc;

import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
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

    public static final int TRIALS_LIMIT = 20;
    private final int FOOD_SOURCES_NUMBER = 20;
    private final int DIMENSION;

    private List<GreenDataCenter> dataCenterList;
    private List<GreenVm> vmList;
    private List<FoodSource> foodSourceList;
    private List<GreenHost> hostList;


    public ArtificialBeeColony(List<GreenDataCenter> dataCenterList, List<GreenVm> vmList) {
        this.dataCenterList = dataCenterList;
        this.vmList = vmList;
        hostList = getHostList();
        DIMENSION = vmList.size();
    }

    public FoodSource runAlgorithm() {
        double clock = CloudSim.clock();
        int epoch = 0;
        int counter = 0;
        double prevFitness = 0;
        FoodSource bestFoodSource = null;
        System.out.println("Dimension: " + DIMENSION);
        if (clock == 6000.5) {
            System.out.println();
        }
        initialize();
        System.out.println(clock);
        do {
            if (bestFoodSource != null) {
                prevFitness = bestFoodSource.getFitness();
            }
//            System.out.println(clock + ": Previous fitness function was: " + prevFitness);
            sendEmployedBees();
            applyFitness();
            computeProbability();
            sendOnlookerBees();
            bestFoodSource = getBestSolution();

//            System.out.println(clock + ": Actual fitness function is: " + bestFoodSource.getFitness());
            if (prevFitness == bestFoodSource.getFitness()) {
                counter++;
            } else {
                counter = 0;
            }
            sendScoutBees();
            epoch++;
        } while (bestFoodSource.getConflictsNumber() != 0 || counter <= 20);

        bestFoodSource = getBestSolution();
        System.out.println("Number of epochs: " + epoch);
        return bestFoodSource;
    }

    private FoodSource getBestSolution() {
        FoodSource maxFoodSource = foodSourceList.get(0);
        double diff = Double.POSITIVE_INFINITY;
        for (FoodSource fs : foodSourceList) {
            if (Math.abs(1.0 - fs.getFitness()) < diff) {
                diff = Math.abs(1 - fs.getFitness());
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
            FoodSource foodSource = getFoodSource();
            foodSourceSet.add(foodSource);
            index++;
        }
        if (foodSourceSet.size() != 0) {
            foodSourceList.addAll(foodSourceSet);
        }
    }

    private FoodSource getFoodSource() {
        FoodSource foodSource = new FoodSource();
        for (Vm vm : vmList) {
            Host host = getRandomHost(vm, foodSource);
            Nectar nectar = new Nectar(host, vm);
            foodSource.addNectar(nectar);
            Bee employedBee = new Bee(foodSource);
            foodSource.setEmployedBee(employedBee);
        }
        return foodSource;
    }

    private GreenHost getRandomHost(Vm vm, FoodSource foodSource) {
        Random random = new Random();
        GreenHost host;
        double greenEnergy;
        List<Vm> assignedBeforeVms;
        do {
            int selectedHostIndex = random.nextInt(hostList.size());
            host = hostList.get(selectedHostIndex);
            assignedBeforeVms = foodSource.getVmListForHost(host);
            GreenDataCenter dataCenter = (GreenDataCenter) host.getDatacenter();
            greenEnergy = dataCenter.getGreenEnergyQuantity();
        } while (greenEnergy == 0 ||
                !host.isMigrationPossible(vm, assignedBeforeVms) ||
                host.getDatacenter() == vm.getHost().getDatacenter());

        return host;
    }

    private void sendEmployedBees() {
        for (FoodSource fs : foodSourceList) {
            Bee bee = fs.getEmployedBee();
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
                FoodSource newFoodSource = getFoodSource();
                foodSource.setNectarList(newFoodSource.getNectarList());
                foodSource.setMigrationMap(newFoodSource.getMigrationMap());
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
