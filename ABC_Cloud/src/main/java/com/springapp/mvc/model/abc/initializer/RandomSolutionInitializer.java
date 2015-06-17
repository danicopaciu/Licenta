package com.springapp.mvc.model.abc.initializer;

import com.springapp.mvc.model.abc.Bee;
import com.springapp.mvc.model.abc.FoodSource;
import com.springapp.mvc.model.abc.Nectar;
import com.springapp.mvc.model.cloud.GreenHost;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

import java.util.*;


public class RandomSolutionInitializer implements SolutionInitializer {

    @Override
    public List<FoodSource> getSolutions(List<Vm> migratedVms, List<Host> hostList, int solutionNumber) {
        int index = 0;
        Set<FoodSource> foodSourceSet = new HashSet<FoodSource>();
        while (index < solutionNumber) {
            FoodSource foodSource = getFoodSource(migratedVms, hostList);
            foodSourceSet.add(foodSource);
            index++;
        }
        List<FoodSource> foodSourceList = new ArrayList<FoodSource>();
        if (foodSourceSet.size() != 0) {
            foodSourceList.addAll(foodSourceSet);
        }
        return foodSourceList;
    }

    public FoodSource getFoodSource(List<Vm> migratedVm, List<Host> hostList) {
        FoodSource foodSource = new FoodSource();
        for (Vm vm : migratedVm) {
            Host host = getRandomHost(vm, hostList, foodSource);
            setNectar(foodSource, vm, host);
            setBee(foodSource);
            foodSource.addMigrationOutVm(vm.getHost(), vm);
        }
        return foodSource;
    }

    private void setNectar(FoodSource foodSource, Vm vm, Host host) {
        Nectar nectar = new Nectar(host, vm);
        foodSource.addNectar(nectar);
    }

    private void setBee(FoodSource foodSource) {
        Bee employedBee = new Bee(foodSource);
        foodSource.setEmployedBee(employedBee);
    }

    private GreenHost getRandomHost(Vm vm, List<Host> hostList, FoodSource foodSource) {
        Random random = new Random();
        GreenHost host;
        List<Vm> assignedBeforeVms;
        do {
            int selectedHostIndex = random.nextInt(hostList.size());
            host = (GreenHost) hostList.get(selectedHostIndex);
            assignedBeforeVms = foodSource.getVmListForHost(host);
        } while (!host.isMigrationPossible(vm, assignedBeforeVms) ||
                host.getDatacenter() == vm.getHost().getDatacenter());

        return host;
    }
}
