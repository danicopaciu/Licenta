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

    public static final int TRIALS_LIMIT = 50;
    private final int FOOD_SOURCES_NUMBER = 20;
    private final int DIMENSION;
    private final int ITERATION_LIMIT;
    private List<GreenDataCenter> dataCenterList;
    private List<GreenVm> vmList;
    private List<FoodSource> foodSourceList;
    private List<GreenHost> hostList;
    private FoodSource bestSolution;


    public ArtificialBeeColony(List<GreenDataCenter> dataCenterList, List<GreenVm> vmList) {
        this.dataCenterList = dataCenterList;
        this.vmList = vmList;
        hostList = getHostList();
        DIMENSION = vmList.size();
        if (vmList.size() > 0 && vmList.get(0).getHost().getDatacenter() != dataCenterList.get(0)) {
            ITERATION_LIMIT = vmList.size() * 2;
        } else {
            ITERATION_LIMIT = 0;
        }
    }

    public FoodSource runAlgorithm() {
        double clock = CloudSim.clock();
        int epoch = 0;
        int counter = 0;
        double prevFitness = 0;
//        System.out.println("Dimension: " + DIMENSION);
        initialize();
        computeFitnessFunction();
        System.out.println(clock);
        double diff;
        do {
//            System.out.println(epoch);
//            System.out.println(clock + ": P: " + prevFitness);
            sendEmployedBees();
            applyFitness();
            computeProbability();
            sendOnlookerBees();
            bestSolution = getBestSolution();

//            System.out.println(clock + ": A: " + bestSolution.getFitness());
            if (evaluateFitness(bestSolution.getFitness()) == evaluateFitness(prevFitness)) {
                counter++;
            } else {
                counter = 0;
                prevFitness = bestSolution.getFitness();
            }
            if (counter >= ITERATION_LIMIT) {
                break;
            }
            sendScoutBees();
            epoch++;
            diff = Math.abs(1 - bestSolution.getFitness());
        } while (diff >= 0.5);

        bestSolution = getBestSolution();
//        System.out.println("Number of epochs: " + epoch);
        return bestSolution;
    }

    private double evaluateFitness(double fitness) {
        return 1 - fitness;
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
            if (foodSource.getEmployedBee() == null) {
                System.out.println();
            }
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
            foodSource.addMigrationOutVm(vm.getHost(), vm);
        }
        return foodSource;
    }

    private GreenHost getRandomHost(Vm vm, FoodSource foodSource) {
        Random random = new Random();
        GreenHost host;
        List<Vm> assignedBeforeVms;
        do {
            int selectedHostIndex = random.nextInt(hostList.size());
            host = hostList.get(selectedHostIndex);
            assignedBeforeVms = foodSource.getVmListForHost(host);
        } while (!host.isMigrationPossible(vm, assignedBeforeVms) ||
                host.getDatacenter() == vm.getHost().getDatacenter());

        return host;
    }

    private void sendEmployedBees() {
        int index = 0;
        for (FoodSource fs : foodSourceList) {

            int neighbourBeeIndex;

            do {
                neighbourBeeIndex = getRandomNeighbourIndex(FOOD_SOURCES_NUMBER - 1, index);
            } while (neighbourBeeIndex == index);

            Bee currentBee = fs.getEmployedBee();
            FoodSource neighbourFoodSource = foodSourceList.get(neighbourBeeIndex);
            currentBee.searchInNeighborhood(neighbourFoodSource, DIMENSION, dataCenterList);
            index++;
        }
    }

    private void computeFitnessFunction() {
        for (FoodSource fs : foodSourceList) {
            Bee bee = fs.getEmployedBee();
            try {
                bee.applyFitnessFunction(dataCenterList);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
//                for (int j = 0; j < 0.2 * DIMENSION; j++) {
//
//                }
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
                if (foodSource.getProbability() > 0.9) {
                    foodSource.setTrialsNumber(0);
                } else {
                    FoodSource newFoodSource = getFoodSource();
                    foodSource.setNectarList(newFoodSource.getNectarList());
                    foodSource.setMigrationMap(newFoodSource.getMigrationMap());
                    Bee bee = foodSource.getEmployedBee();
                    bee.applyFitnessFunction(dataCenterList);
                }
            }
        }
    }

    private int getRandomNeighbourIndex(int low, int high) {
//        return (int) Math.round((high - low) * new Random().nextDouble() + low);
        return new Random().nextInt(FOOD_SOURCES_NUMBER);
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

        List<Double> foodSourceQuality = new ArrayList<Double>();
        int index = 0;
        for (FoodSource fs : foodSourceList) {
            double diff = Math.abs(1 - fs.getFitness());
            foodSourceQuality.add(diff);
            index++;
        }

        double minDiff = Collections.min(foodSourceQuality);
        double maxDiff = Collections.max(foodSourceQuality);

        index = 0;
        for (FoodSource fs : foodSourceList) {
            double newFitness;
            if (minDiff == maxDiff) {
                newFitness = 100;
            } else {
                double diff = foodSourceQuality.get(index);
                double a = ((diff - minDiff) / (maxDiff - minDiff)) * 100;
                newFitness = 100 - a;
            }
            fs.setFitnessFactor(newFitness);
            index++;
        }

    }
}
