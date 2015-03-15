package com.springapp.mvc.model.abc;

import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;

import java.util.*;

/**
 * Created by Daniel on 3/14/2015.
 */
public class ArtificialBeeColony {

    private Set<FoodSource> foodSourceSet;

    private List<GreenDataCenter> dataCenterList;

    private List<GreenVm> vmList;

    private List<EmployedBee> employedBeeList;

    private List<OnlookerBee> onlookerBeeList;

    private List<ScoutBee> scoutBeeList;

    private static final int FOOD_SOURCES_NUMBER = 20;

    private final int DIMENSION;

    public ArtificialBeeColony(List<GreenDataCenter> dataCenterList, List<GreenVm> vmList) {
        this.dataCenterList = dataCenterList;
        this.vmList = vmList;
        DIMENSION = vmList.size();
    }

    public boolean runAlgorithm(){
        boolean done = false;
        int epoch = 0;
        initialize();
        FoodSource bestFoodSource = getBestSolution();
        while (!done){
            if(epoch < Constants.EPOCH_LIMIT){
                if(bestFoodSource.getConflictsNumber() == 0){
                    done = true;
                }
                sendEmployedBees();
                sendOnlookerBees();
                bestFoodSource = getBestSolution();
                sendScoutBees();
                epoch++;
            }else{
                done = true;
            }
        }
        if(epoch == Constants.EPOCH_LIMIT){
            done = false;
        }
        return done;
    }

    private FoodSource getBestSolution() {
        return Collections.min(foodSourceSet);
    }

    /**
     *
     */
    private void initialize(){
        foodSourceSet = new HashSet<FoodSource>();
        employedBeeList = new ArrayList<EmployedBee>();
        int index = 0;
        while(index < FOOD_SOURCES_NUMBER){
            List<Nectar> nectarList = initializeNectarList();
            FoodSource foodSource = new FoodSource(nectarList);
            if(foodSourceSet.add(foodSource)){
                EmployedBee employedBee = new EmployedBee(index, foodSource);
                foodSource.setBee(employedBee);
                employedBeeList.add(employedBee);
                index++;
                employedBee.computeConflicts();
                employedBee.applyFitnessFunction(dataCenterList);
            }
        }
    }

    /**
     * This method initializes the nectar list of a particular food source
     * @return the nectar list of the food source
     */
    private List<Nectar> initializeNectarList() {
        List<Nectar> nectarList = new ArrayList<Nectar>();
        for(GreenVm vm : vmList){
            GreenHost selectedHost = getRandomHost();
            Nectar nectar = new Nectar(selectedHost, vm);
            nectarList.add(nectar);
        }
        return nectarList;
    }

    private GreenHost getRandomHost() {
        Random random = new Random();
        int selectedHostIndex = random.nextInt(getHostList().size());
        return getHostList().get(selectedHostIndex);
    }

    private void sendEmployedBees(){
        for (int i = 0; i < FOOD_SOURCES_NUMBER; i++){
            int neighbourBeeIndex =
                    getRandomNeighbourIndex(FOOD_SOURCES_NUMBER - 1, i);
            Bee currentBee = employedBeeList.get(i);
            Bee neighbourBee = employedBeeList.get(neighbourBeeIndex);
            currentBee.searchInNeighborhood(neighbourBee.getFoodSource(), DIMENSION, dataCenterList);
        }
    }

    /*
     * de verificat in modelul initial
     */
    private void sendOnlookerBees(){
        int index = 0;
        int i = 0;
        onlookerBeeList = new ArrayList<OnlookerBee>();
        FoodSource[] foodSourceArray = (FoodSource[]) foodSourceSet.toArray();
        while (index < FOOD_SOURCES_NUMBER){
            Random random = new Random();
            double randomDouble = random.nextDouble();
            FoodSource currentFoodSource = foodSourceArray[index];
            if(randomDouble < currentFoodSource.getProbability()){
                int neighbourBeeIndex =
                        getRandomNeighbourIndex(FOOD_SOURCES_NUMBER - 1, i);
                OnlookerBee onlookerBee = new OnlookerBee(index, currentFoodSource);
                onlookerBeeList.add(onlookerBee);
                FoodSource neighbourFoodSource = foodSourceArray[neighbourBeeIndex];
                onlookerBee.searchInNeighborhood(neighbourFoodSource, DIMENSION, dataCenterList);
                index++;
            }
            i++;
            if(i == FOOD_SOURCES_NUMBER){
                i = 0;
            }
        }
    }

    private void sendScoutBees(){

    }

    private int getRandomNeighbourIndex(int low, int high) {
        return (int)Math.round((high - low) * new Random().nextDouble() + low);
    }
    
    private List<GreenHost> getHostList(){
        List<GreenHost> hostList = new ArrayList<GreenHost>();
        for(Datacenter d : dataCenterList){
            List<Host> auxHostList = d.getHostList();
            for(Host h : auxHostList){
                if(h instanceof PowerHostUtilizationHistory){
                    hostList.add((GreenHost)h);
                }
            }
        }
        return hostList;
    }
}
