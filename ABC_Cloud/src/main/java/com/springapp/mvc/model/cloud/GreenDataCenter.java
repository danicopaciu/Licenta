package com.springapp.mvc.model.cloud;

import com.springapp.mvc.controller.Resources;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.core.predicates.PredicateType;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;
import java.util.Map;

public class GreenDataCenter extends PowerDatacenter {

    public static final int SUPPLIED_TEMPERATURE = 15; //degrees
    public static final int GREEN_ENERGY = 0;
    public static final int BROWN_ENERGY = 1;
    public static final int SERVERS_ENERGY = 2;
    public static final int COOLING = 3;
    public static final int HEAT = 4;
    public static final int TOTAL_VMS = 5;
    public static final int VMS_IN = 6;
    public static final int VMS_OUT = 7;
    public static final int DATACENTER_ENERGY = 8;
    public static final int OVERALL_VMS = 9;
    public final int MAX_NUMBER_OF_VMS;


    private double greenEnergyQuantity;
    private double brownEnergyQuantity;
    private double totalEnergy;
    private double coolingEnergy;
    private double heatGained;
    private double migratingInVms;
    private double migratingOutVms;
    private int totalVms;
    private double predictedEnergy;
    private double error;


    public GreenDataCenter(String name, DatacenterCharacteristics characteristics,
                           VmAllocationPolicy vmAllocationPolicy, List<Storage> storageList,
                           double schedulingInterval, int maxNumberVms) throws Exception {
        super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);

        this.greenEnergyQuantity = 0;
        this.brownEnergyQuantity = 0;
        this.totalEnergy = 0;
        this.coolingEnergy = 0;
        this.heatGained = 0;
        this.migratingInVms = 0;
        this.migratingOutVms = 0;
        this.totalVms = 0;
        MAX_NUMBER_OF_VMS = maxNumberVms;

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


            CloudSim.cancelAll(getId(), new PredicateType(CloudSimTags.VM_DATACENTER_EVENT));
            send(getId(), getSchedulingInterval(), CloudSimTags.VM_DATACENTER_EVENT);

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
        Log.formatLine(getName() + ": New resource usage for the time frame starting at %.2f:", currentTime);

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
            setCoolingEnergy(getPower() / computeCOP());
            setHeatGained(getPower() * 3.5);
            setTotalEnergy(getPower() + getCoolingEnergy());
            if (getPredictedEnergy() != 0) {
                setError(getPower() / getPredictedEnergy());
            }

            if (getTotalEnergy() - getGreenEnergyQuantity() > 0) {
                setBrownEnergyQuantity(getTotalEnergy() - getGreenEnergyQuantity());
            } else {
                setBrownEnergyQuantity(0);
            }


        }
        checkCloudletCompletion();

        /** Remove completed VMs **/
        for (PowerHost host : this.<PowerHost>getHostList()) {
            for (Vm vm : host.getCompletedVms()) {
                getVmAllocationPolicy().deallocateHostForVm(vm);
                getVmList().remove(vm);
                totalVms--;
                Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
            }
        }

        Log.printLine();

        setLastProcessTime(currentTime);
        return minTime;
    }

    public double getTotalEnergy() {
        return totalEnergy;
    }

    public void setTotalEnergy(double totalEnergy) {
        this.totalEnergy = totalEnergy;
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


    public int getTotalVms() {
        return totalVms;
    }

    public void setTotalVms(int totalVms) {
        this.totalVms = totalVms;
    }

    protected void processVmCreate(SimEvent ev, boolean ack) {
        Vm vm = (Vm) ev.getData();

        boolean result = false;
        if (totalVms < MAX_NUMBER_OF_VMS) {
            result = getVmAllocationPolicy().allocateHostForVm(vm);
        }

        if (ack) {
            int[] data = new int[3];
            data[0] = getId();
            data[1] = vm.getId();

            if (result) {
                data[2] = CloudSimTags.TRUE;
            } else {
                data[2] = CloudSimTags.FALSE;
            }
            send(vm.getUserId(), CloudSim.getMinTimeBetweenEvents(), CloudSimTags.VM_CREATE_ACK, data);
        }

        if (result) {
            getVmList().add(vm);
            incrementTotalVms();

            if (vm.isBeingInstantiated()) {
                vm.setBeingInstantiated(false);
            }

            vm.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(vm).getVmScheduler()
                    .getAllocatedMipsForVm(vm));
        }
    }

    public void incrementTotalVms() {
        totalVms++;
    }

    public double getCoolingEnergy() {
        return coolingEnergy;
    }

    public void setCoolingEnergy(double coolingEnergy) {
        this.coolingEnergy = coolingEnergy;
    }

    public double getHeatGained() {
        return heatGained;
    }

    public void setHeatGained(double heatGained) {

        this.heatGained = (heatGained * Resources.HEAT_PRICE) / Resources.ENERGY_PRICE;
//        this.heatGained = heatGained;
    }

    public double getMigratingInVms() {
        return migratingInVms;
    }

    public void setMigratingInVms(double migratingInVms) {
        this.migratingInVms = migratingInVms;
    }

    public double getMigratingOutVms() {
        return migratingOutVms;
    }

    public void setMigratingOutVms(double migratingOutVms) {
        this.migratingOutVms = migratingOutVms;
    }

    public void setPower(double power) {
        super.setPower(power);
    }

    public double getPredictedEnergy() {
        return predictedEnergy;
    }

    public void setPredictedEnergy(double predictedEnergy) {
        this.predictedEnergy = predictedEnergy;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }

}
