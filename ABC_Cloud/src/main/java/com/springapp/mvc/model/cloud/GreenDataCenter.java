package com.springapp.mvc.model.cloud;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 3/12/2015.
 * GreenDataCenter
 */
public class GreenDataCenter extends PowerDatacenter {


    public static final int SUPPLIED_TEMPERATURE = 15; //degrees
    public static final String TIME = "Time";
    public static final String GREEN_ENERGY = "GreenEnergy";
    public static final String BROWN_ENERGY = "BrownEnergy";
    public static final String SERVERS_ENERGY = "ServersEnergy";
    public static final String COOLING = "Cooling";
    public static final String HEAT = "Heat";
    public static final String VMS_IN = "MigratingInVms";
    public static final String VMS_OUT = "MigratingOutVms";
    private double greenEnergyQuantity;
    private double brownEnergyQuantity;
    private Map<String, List<Double>> statistics;

    public GreenDataCenter(String name, DatacenterCharacteristics characteristics,
                           VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
                           double schedulingInterval) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

        this.greenEnergyQuantity = 0;
        this.brownEnergyQuantity = 0;

        statistics = new HashMap<String, List<Double>>();
        statistics.put(TIME, new ArrayList<Double>());
        statistics.put(GREEN_ENERGY, new ArrayList<Double>());
        statistics.put(BROWN_ENERGY, new ArrayList<Double>());
        statistics.put(SERVERS_ENERGY, new ArrayList<Double>());
        statistics.put(COOLING, new ArrayList<Double>());
        statistics.put(HEAT, new ArrayList<Double>());
        statistics.put(VMS_IN, new ArrayList<Double>());
        statistics.put(VMS_OUT, new ArrayList<Double>());

    }

    public double getGreenEnergyQuantity() {
        return greenEnergyQuantity;
    }

    public void setGreenEnergyQuantity(double greenEnergyQuantity) {
        this.greenEnergyQuantity = greenEnergyQuantity;
    }



    @Override
    protected void updateCloudletProcessing() {
        if (getCloudletSubmitted() == -1 || getCloudletSubmitted() == CloudSim.clock()) {
            CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
            schedule(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
            return;
        }
        double currentTime = CloudSim.clock();

        if (currentTime > 86400) {
            System.out.println("Am terminat simularea");
            return;
        }

        // if some time passed since last processing
        if (currentTime > getLastProcessTime()) {

            double minTime = updateCloudetProcessingWithoutSchedulingFutureEventsForce();

            if (!isDisableMigrations()) {
                List<Map<String, Object>> migrationMap = getVmAllocationPolicy().optimizeAllocation(
                        getVmList());

                if (migrationMap != null) {
                    for (Map<String, Object> migrate : migrationMap) {
                        Vm vm = (Vm) migrate.get("vm");
                        PowerHost targetHost = (PowerHost) migrate.get("host");
                        PowerHost oldHost = (PowerHost) vm.getHost();

                        if (oldHost == null) {
                            Log.formatLine(
                                    "%.2f: Migration of VM #%d to Host #%d is started",
                                    currentTime,
                                    vm.getId(),
                                    targetHost.getId());
                        } else {
                            Log.formatLine(
                                    "%.2f: Migration of VM #%d from Host #%d to Host #%d is started",
                                    currentTime,
                                    vm.getId(),
                                    oldHost.getId(),
                                    targetHost.getId());
                        }

                        targetHost.addMigratingInVm(vm);
                        incrementMigrationCount();

                        /** VM migration delay = RAM / bandwidth **/
                        // we use BW / 2 to model BW available for migration purposes, the other
                        // half of BW is for VM communication
                        // around 16 seconds for 1024 MB using 1 Gbit/s network
                        send(
                                getId(),
                                vm.getRam() / ((double) targetHost.getBw() / (2 * 8000)),
                                CloudSimTags.VM_MIGRATE,
                                migrate);
                    }
                }
            }

            // schedules an event to the next time
            if (minTime != Double.MAX_VALUE) {
                CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
                send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);
            }

            setLastProcessTime(currentTime);
        }
    }


    /**
     * Update cloudet processing without scheduling future events.
     *
     * @return the double
     */
    @Override
    protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
        double currentTime = CloudSim.clock();
        double minTime = Double.MAX_VALUE;
        double timeDiff = currentTime - getLastProcessTime();
        double timeFrameDatacenterEnergy = 0.0;

        Log.printLine("\n\n--------------------------------------------------------------\n\n");
        Log.formatLine("New resource usage for the time frame starting at %.2f:", currentTime);

        for (PowerHost host : this.<PowerHost>getHostList()) {
            Log.printLine();

            double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
            if (time < minTime) {
                minTime = time;
            }

            Log.formatLine(
                    "%.2f: [Host #%d] utilization is %.2f%%",
                    currentTime,
                    host.getId(),
                    host.getUtilizationOfCpu() * 100);
        }

        if (timeDiff > 0) {
            Log.formatLine(
                    "\nEnergy consumption for the last time frame from %.2f to %.2f:",
                    getLastProcessTime(),
                    currentTime);

            for (GreenHost host : this.<GreenHost>getHostList()) {
                double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
                double utilizationOfCpu = host.getUtilizationOfCpu();
                double timeFrameHostEnergy = host.getEnergyLinearInterpolation(
                        previousUtilizationOfCpu,
                        utilizationOfCpu,
                        timeDiff);
                host.addEnergyHistory(timeFrameHostEnergy);
                timeFrameDatacenterEnergy += timeFrameHostEnergy;

                Log.printLine();
                Log.formatLine(
                        "%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%",
                        currentTime,
                        host.getId(),
                        getLastProcessTime(),
                        previousUtilizationOfCpu * 100,
                        utilizationOfCpu * 100);
                Log.formatLine(
                        "%.2f: [Host #%d] energy is %.2f W*sec",
                        currentTime,
                        host.getId(),
                        timeFrameHostEnergy);
            }

            Log.formatLine(
                    "\n%.2f: Data center's energy is %.2f W*sec\n",
                    currentTime,
                    timeFrameDatacenterEnergy);
        }

        setPower(timeFrameDatacenterEnergy);
        if (currentTime != getLastProcessTime()) {
            double energy = greenEnergyQuantity - timeFrameDatacenterEnergy;
            if (energy < 0) {
                setBrownEnergyQuantity(timeFrameDatacenterEnergy - getGreenEnergyQuantity());
            } else {
//                greenEnergyQuantity = energy;
                setBrownEnergyQuantity(0);
            }
            if (currentTime >= 600) {
                putTime(currentTime);
                putGreenEnergy(getGreenEnergyQuantity());
                putServerEnergy(getPower());
                putBrownEnergy(getBrownEnergyQuantity());
                double heat = 3.5 * getPower();
                putHeat(heat);
                double cop = computeCOP();
                double cooling = getPower() / cop;
                putCooling(cooling);
            }
        }
        checkCloudletCompletion();

        /** Remove completed VMs **/
        for (PowerHost host : this.<PowerHost>getHostList()) {
            for (Vm vm : host.getCompletedVms()) {
                getVmAllocationPolicy().deallocateHostForVm(vm);
                getVmList().remove(vm);
                Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
            }
        }

        Log.printLine();

        setLastProcessTime(currentTime);
        return minTime;
    }

    public boolean addToMigratingInVms(int d) {
        return statistics.get(VMS_IN).add((double) d);
    }

    public boolean addToMigratingOutVms(int d) {
        return statistics.get(VMS_OUT).add((double) d);
    }

    private double computeCOP() {
        return 0.0068 * GreenDataCenter.SUPPLIED_TEMPERATURE *
                GreenDataCenter.SUPPLIED_TEMPERATURE +
                0.0008 * GreenDataCenter.SUPPLIED_TEMPERATURE + 0.458;
    }

    public double getBrownEnergyQuantity() {
        return brownEnergyQuantity;
    }

    public void setBrownEnergyQuantity(double brownEnergyQuantity) {
        this.brownEnergyQuantity = brownEnergyQuantity;
    }

    public void putTime(Double time) {
        statistics.get(TIME).add(time);
    }

    public void putGreenEnergy(Double greenEnergy) {
        statistics.get(GREEN_ENERGY).add(greenEnergy);
    }

    public void putBrownEnergy(Double brownEnergy) {
        statistics.get(BROWN_ENERGY).add(brownEnergy);
    }

    public void putServerEnergy(Double serverEnergy) {
        statistics.get(SERVERS_ENERGY).add(serverEnergy);
    }

    public void putCooling(Double cooling) {
        statistics.get(COOLING).add(cooling);
    }

    public void putHeat(Double heat) {
        statistics.get(HEAT).add(heat);
    }

    public Map<String, List<Double>> getStatistics() {
        return statistics;
    }
}
