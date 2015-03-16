package com.springapp.mvc.model.abc;

import com.springapp.mvc.model.cloud.GreenDataCenter;
import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Daniel on 3/14/2015.
 * Bee class
 */
public class Bee {

    private int id;

    private FoodSource foodSource;

    public Bee(int id, FoodSource foodSource) {
        this.id = id;
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
        double actualFitnessFunction = applyFitnessFunction(dataCenterList);
        int actualConflict = computeConflicts();
        if (prevFitnessFunction > actualFitnessFunction && actualConflict <= prevConflicts) {
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

    /**
     * deep copy the previous host
     *
     * @param host is the object that is cloned
     * @return the copy of the host
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private GreenHost savePreviousHost(GreenHost host) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(host);
        oos.flush();
        oos.close();
        bos.close();
        byte[] byteData = bos.toByteArray();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(byteData);
        Object object = new ObjectInputStream(inputStream).readObject();
        if (object instanceof GreenHost) {
            return (GreenHost) new ObjectInputStream(inputStream).readObject();
        }
        return null;
    }

    private int determineNewHostId(double phi, int prevHostId, int nextHostId) {
        int newHostId;
        newHostId = (int) (prevHostId + phi * (prevHostId - nextHostId));
        if (newHostId < 0) {
            newHostId = 0;
        }
        if (newHostId >= Constants.HOST_NUMBER) {
            newHostId = Constants.HOST_NUMBER - 1;
        }
        return newHostId;
    }

    private int determineChangedParameterIndex(int dimension) {
        Random random = new Random();
        return random.nextInt(dimension);
    }

    private double determinePhi() {
        Random random = new Random();
        return random.nextDouble();
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
            double vmEnergy = 0;
            for (Nectar n : nectarList) {
                GreenHost host = n.getHost();
                String currentDataCenterName = host.getDatacenter().getName();
                if (currentDataCenterName.equals(dc.getName())) {
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
}
