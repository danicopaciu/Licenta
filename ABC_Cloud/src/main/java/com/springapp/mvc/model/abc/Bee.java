package com.springapp.mvc.model.abc;

import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Daniel on 3/14/2015.
 */
public abstract class Bee {

    private int id;

    private FoodSource foodSource;

    public Bee(int id, FoodSource foodSource) {
        this.foodSource = foodSource;
    }

    protected void searchInNeighborhood(FoodSource neighbourFoodSource, int dimension,
                                        List<GreenDataCenter> dataCenterList) throws NotImplementedException {
        applyFitnessFunction(dataCenterList);
        double prevFitnessFunction = foodSource.getFitness();
        int j = determineParameterIndex(dimension);
        double phi = determinePhi();
        Nectar changedParameter = foodSource.getNectarList().get(j);
        GreenHost auxHost = savePreviousHost(changedParameter.getHost());
        int newHostId = determineNewHostId(phi, changedParameter.getHost().getId(),
                neighbourFoodSource.getNectarList().get(j).getHost().getId());
        GreenHost newHost = getHostList(dataCenterList).get(newHostId);
        changedParameter.setHost(newHost);
        applyFitnessFunction(dataCenterList);
        double actualFitnessFunction = foodSource.getFitness();
        if(prevFitnessFunction > actualFitnessFunction){
            changedParameter.setHost(auxHost);
            applyFitnessFunction(dataCenterList);
            foodSource.incrementTrialsNumber();
        }else{
            foodSource.setTrialsNumber(0);
        }
    }

    private GreenHost savePreviousHost(GreenHost host) {
        return new GreenHost(
                host.getId(),
                host.getRamProvisioner(),
                host.getBwProvisioner(),
                host.getStorage(),
                host.getPeList(),
                host.getVmScheduler(),
                host.getPowerModel());
    }

    private int determineNewHostId(double phi, int prevHostId, int nextHostId){
        int newHostId;
        newHostId = (int) (prevHostId + phi * (prevHostId - nextHostId));
        if(newHostId < 0){
            newHostId = 0;
        }
        if(newHostId >= Constants.HOST_NUMBER){
            newHostId = Constants.HOST_NUMBER - 1;
        }
        return newHostId;
    }

    private int determineParameterIndex(int dimension) {
        Random random = new Random();
        return random.nextInt() % dimension;
    }

    private double determinePhi(){
        Random random = new Random();
        return random.nextDouble();
    }

    public FoodSource getFoodSource() {
        return foodSource;
    }

    public void setFoodSource(FoodSource foodSource) {
        this.foodSource = foodSource;
    }

    private List<GreenHost> getHostList(List<GreenDataCenter>dataCenterList){
        List<GreenHost> hostList = new ArrayList<GreenHost>();
        for(Datacenter d : dataCenterList){
            List<Host> auxHostList = d.getHostList();
            for(Host h : auxHostList){
                if(h instanceof GreenHost){
                    hostList.add((GreenHost)h);
                }
            }
        }
        return hostList;
    }

    public double applyFitnessFunction(List<GreenDataCenter> dataCenterList){
        List<Nectar> nectarList = foodSource.getNectarList();
        double result = 0;
        for(GreenDataCenter dc : dataCenterList){
            double vmEnergy = 0;
            for(Nectar n : nectarList){
                GreenHost host = n.getHost();
                if(host.getDataCenterName().equals(dc.getName())){
                    GreenVm vm = n.getVm();
                    double necessaryEnergy = vm.getNecessaryEnergy();
                    vmEnergy += necessaryEnergy;
                }
            }
            double energy = vmEnergy / dc.getGreenEnergyQuantity();
            result += energy;
        }
        foodSource.setFitness(result);
        return result;
    }

    public void computeConflicts(){
        List<Nectar> nectarList = foodSource.getNectarList();
        int conflicts = 0;
        for(Nectar n : nectarList){
            GreenHost host = n.getHost();
            GreenVm vm = n.getVm();
            if(!host.isSuitableForVm(vm)){
                conflicts++;
            }
        }
        foodSource.setConflictsNumber(conflicts);
    }
}
