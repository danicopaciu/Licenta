package com.springapp.mvc.model.abc;

import com.springapp.mvc.model.abc.initializer.RandomSolutionInitializer;
import com.springapp.mvc.model.abc.initializer.SolutionInitializer;
import com.springapp.mvc.model.cloud.FederationOfDataCenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ArtificialBeeColony {

    public static final int TRIALS_LIMIT = 50;
    private final int FOOD_SOURCES_NUMBER;
    private final int DIMENSION;
    private final int ITERATION_LIMIT;
    private List<Vm> vmList;
    private List<FoodSource> foodSourceList;
    private FederationOfDataCenter fed;

    public ArtificialBeeColony(FederationOfDataCenter fed, List<Vm> vmList) {
        this.fed = fed;
        this.vmList = vmList;
        DIMENSION = vmList.size();
        FOOD_SOURCES_NUMBER = 20;
        if (vmList.size() > 0 && vmList.get(0).getHost().getDatacenter() != fed.getDataCenterList().get(0)) {
            ITERATION_LIMIT = vmList.size() * 3;
        } else {
            ITERATION_LIMIT = 0;
        }
    }

    public FoodSource runAlgorithm() {
        int counter = 0;
        double prevFitness = 0;
        List<Host> hostList = fed.getHostList();
        SolutionInitializer solutionInitializer = new RandomSolutionInitializer();
        foodSourceList = solutionInitializer.getSolutions(vmList, hostList, FOOD_SOURCES_NUMBER);
        computeFitnessFunction();
        double diff;
        FoodSource bestSolution;
        do {
            sendEmployedBees();
            applyFitness();
            computeProbability();
            sendOnlookerBees();
            bestSolution = getBestSolution();
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
            diff = Math.abs(1 - bestSolution.getFitness());
        } while (diff >= 0.2);

        bestSolution = getBestSolution();
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

    private void sendEmployedBees() {
        int index = 0;
        for (FoodSource fs : foodSourceList) {

            int neighbourBeeIndex;

            do {
                neighbourBeeIndex = getRandomNeighbourIndex(FOOD_SOURCES_NUMBER - 1, index);
            } while (neighbourBeeIndex == index);

            Bee currentBee = fs.getEmployedBee();
            FoodSource neighbourFoodSource = foodSourceList.get(neighbourBeeIndex);
            currentBee.searchInNeighborhood(neighbourFoodSource, DIMENSION, fed);
            index++;
        }
    }

    private void computeFitnessFunction() {
        for (FoodSource fs : foodSourceList) {
            Bee bee = fs.getEmployedBee();
            try {
                bee.applyFitnessFunction(fed.getGreenDatacenter());
            } catch (Exception e) {
                System.out.println(bee);
                e.printStackTrace();
                System.exit(0);
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
                int neighbourBeeIndex =
                        getRandomNeighbourIndex(FOOD_SOURCES_NUMBER - 1, i);
                Bee onlookerBee = new Bee(currentFoodSource);
                currentFoodSource.setOnlookerBee(onlookerBee);
                FoodSource neighbourFoodSource = foodSourceList.get(neighbourBeeIndex);
                onlookerBee.searchInNeighborhood(neighbourFoodSource, DIMENSION, fed);
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
                    RandomSolutionInitializer solutionInitializer = new RandomSolutionInitializer();
                    FoodSource newFoodSource = solutionInitializer.getFoodSource(vmList, fed.getHostList());
                    foodSource.setNectarList(newFoodSource.getNectarList());
                    foodSource.setMigrationMap(newFoodSource.getMigrationMap());
                    Bee bee = foodSource.getEmployedBee();
                    bee.applyFitnessFunction(fed.getGreenDatacenter());
                }
            }
        }
    }

    private int getRandomNeighbourIndex(int low, int high) {
        return new Random().nextInt(FOOD_SOURCES_NUMBER);
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
