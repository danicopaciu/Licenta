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
        initialize();
        System.out.println(clock);
        do {
//            System.out.println(epoch);

            if (bestFoodSource != null) {
                prevFitness = bestFoodSource.getFitness();
            }
//            System.out.println(clock + ": Previous fitness function was: " + prevFitness);
//            long start = System.currentTimeMillis();
            sendEmployedBees();
//            long finish = System.currentTimeMillis();
//            System.out.println("Execution time for employed bees: " + (finish-start));
            applyFitness();
            computeProbability();
//            start = System.currentTimeMillis();
            sendOnlookerBees();
//            finish = System.currentTimeMillis();
//            System.out.println("Execution time for onlooker bees: " + (finish-start));
            bestFoodSource = getBestSolution();

//            if (bestFoodSource.getFitness() > 1) {
//                System.out.println();
//            }
//            System.out.println(clock + ": Actual fitness function is: " + bestFoodSource.getFitness());

//            if (epoch >= 500) {
//                System.out.println(counter + " " + bestFoodSource.getConflictsNumber());
//                initialize();
//                epoch = 0;
//            }
            if (prevFitness == bestFoodSource.getFitness()) {
                counter++;
            } else {
                counter = 0;
            }
            sendScoutBees();
            epoch++;
        } while (bestFoodSource.getConflictsNumber() != 0 || counter <= 5);

        bestFoodSource = getBestSolution();
//        Log.printLine(clock + ": final best food source has fitness function: " + bestFoodSource.getFitness());
//        for (Nectar n : bestFoodSource.getNectarList()) {
//            Log.printLine(clock + ": Vm#" + n.getVm().getId() + " will be migrated to host#" + n.getHost().getId()
//                    + " on Datacenter#" + n.getHost().getDatacenter().getId());
//        }
        System.out.println("Number of epochs: " + epoch);
//        System.out.println("Conflicts: " + bestFoodSource.getConflictsNumber());
        return bestFoodSource;
    }

    private FoodSource getBestSolution() {
        FoodSource maxFoodSource = foodSourceList.get(0);
        double diff = Double.POSITIVE_INFINITY;
        for (FoodSource fs : foodSourceList) {
//            if (fs.getFitness() > maxFoodSource.getFitness()) {
//                maxFoodSource = fs;
//            }
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
//        while (index < FOOD_SOURCES_NUMBER) {
//            List<Nectar> nectarList = initializeNectarList();
//            FoodSource foodSource = new FoodSource(nectarList);
//            if (foodSourceSet.add(foodSource)) {
//                Bee employedBee = new Bee(foodSource);
//                foodSource.setEmployedBee(employedBee);
//                clearMigrationLists();
//                index++;
//
//            }
//        }
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

//    private void clearMigrationLists() {
//        for (GreenHost h : getHostList()) {
//            h.clearMigratingInVms();
//        }
//    }

    /**
     * This method initializes the nectar list of a particular food source
     *
     * @return the nectar list of the food source
     */
//    private List<Nectar> initializeNectarList(List<Vm> assignedBeforeVms) {
//        List<Nectar> nectarList = new ArrayList<Nectar>();
//        for (GreenVm vm : vmList) {
//            GreenHost selectedHost = getRandomHost(vm, assignedBeforeVms);
//            Nectar nectar = new Nectar(selectedHost, vm);
//            nectarList.add(nectar);
//
//        }
//        return nectarList;
//    }
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
        } while (greenEnergy == 0 || !host.isMigrationPossible(vm, assignedBeforeVms) || host.getDatacenter() == vm.getHost().getDatacenter());

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
