package com.springapp.mvc.model.cloud;

import com.springapp.mvc.controller.CloudController;
import com.springapp.mvc.controller.Resources;
import com.springapp.mvc.model.abc.ArtificialBeeColony;
import com.springapp.mvc.model.abc.FoodSource;
import com.springapp.mvc.model.abc.Nectar;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.*;

public class FederationOfDataCenter extends SimEntity {

    public static final int PERIODIC_EVENT = 67567;
    public static final int DC_NUMBER = 67568;
    public static final int POWER_DATACENTER = 67569;
    public static final int DELAY = 300;
    private static final int V_IN = 3; //starting speed of energy production m/s
    private static final int V_OUT = 25; // finishing speed of energy production m/s
    private static final int PR = 225000; //windmill power w
    private static final int VR = 13; // speed for optimal production m/s
    public static double dataCenterAllocationDelay;

    private Map<Integer, Map<String, Double>> migrationResults;
    private CloudStatistics cloudStatistics;

    private List<Datacenter> dataCenterList;
    private List<Host> hostList;
    private List<Vm> vmList;
    private List<Cloudlet> cloudletList;
    private Map<String, List<Double>> windSpeedMap;
    private DatacenterBroker broker;
    private int simulationPeriod;


    public FederationOfDataCenter(String name) {
        super(name);

        migrationResults = new HashMap<Integer, Map<String, Double>>();
        cloudStatistics = new CloudStatistics();

    }

    @Override
    public void startEntity() {
        send(getId(), 100, DC_NUMBER, null);
    }

    @Override
    public void processEvent(SimEvent ev) {
        if (ev == null) {
            Log.printLine("Warning: " + CloudSim.clock() + ": " + this.getName() + ": Null event ignored.");
        } else {
            int tag = ev.getTag();
            switch (tag) {
                case PERIODIC_EVENT:
                    processPeriodicEvent();
                    break;
                case DC_NUMBER:
                    processDataCenterAllocationDelay();
                    break;
                case POWER_DATACENTER:
                    computeDataCenterPower();
                    break;
                default:
                    Log.printLine("Warning: " + CloudSim.clock() + ":" + this.getName() + ": Unknown event ignored. Tag:" + tag);
            }
        }

    }

    @Override
    public void shutdownEntity() {

    }

    private void processDataCenterAllocationDelay() {
        dataCenterAllocationDelay = computeDataCenterAllocationDelay();
        double nextTimeStamp = dataCenterAllocationDelay + 200;
        send(getId(), nextTimeStamp, POWER_DATACENTER, new Object());
    }

    private void computeDataCenterPower() {
        double clock = CloudSim.clock();
        if (clock <= simulationPeriod + dataCenterAllocationDelay) {
            computeGreenPower(clock);
            if (clock >= 600 + dataCenterAllocationDelay) {
                sendNow(getId(), PERIODIC_EVENT, new Object());
            } else {
                send(getId(), DELAY, POWER_DATACENTER, new Object());
            }
        }
    }

    private void processPeriodicEvent() {
        double clock = CloudSim.clock();
        runMigrationAlgorithm();
        boolean generatePeriodicEvent = true; //true if new internal events have to be generated
        if (clock >= simulationPeriod + dataCenterAllocationDelay) {
            generatePeriodicEvent = false;
        }
        if (generatePeriodicEvent) send(getId(), DELAY, POWER_DATACENTER, new Object());
    }

    public double computeDataCenterAllocationDelay() {
        double counter = 0;
        for (Datacenter dc : getDataCenterList()) {
            if (!dc.getVmList().isEmpty()) {
                counter++;
            }
        }
        return counter / 10;
    }


    public void computeGreenPower(double clock) {
        double energy;
        for (Datacenter dc : dataCenterList) {
            List<Double> windSpeedList = windSpeedMap.get(dc.getName());
            double windSpeed = windSpeedList.get((int) (clock - dataCenterAllocationDelay) / 300);
            energy = getWindEnergy(windSpeed);
            if (dc instanceof GreenDataCenter) {
                GreenDataCenter greenDc = (GreenDataCenter) dc;
                if (dc.getName().equals("DataCenter_0")) {
                    greenDc.setGreenEnergyQuantity(Resources.SCHEDULING_INTERVAL * energy + 0.1);
                } else {
                    greenDc.setGreenEnergyQuantity(0.1);
                }
            }
        }
    }

    private double getWindEnergy(double windSpeed) {
        double energy;
        if (windSpeed < V_IN || windSpeed > V_OUT) {
            energy = 0.1;
        } else if (windSpeed > VR && windSpeed < V_OUT) {
            energy = PR;
        } else {
            energy = (PR * (windSpeed - V_IN)) / (VR - V_IN);
        }
        return energy;
    }

    private void runMigrationAlgorithm() {

        List<Vm> greenVmList = getGreenVmList();
        Set<Vm> migratingSet = getMigrationVms(greenVmList);
        List<Vm> migratingList = new ArrayList<Vm>();
        if (migratingSet.size() != 0) {
            migratingList.addAll(migratingSet);
        }
        ArtificialBeeColony abc = new ArtificialBeeColony(this, migratingList);
        FoodSource result = null;
        System.out.println(CloudSim.clock());
        if (!migratingList.isEmpty()) {
            result = abc.runAlgorithm();
            scheduleMigrations(result);
        }
        cloudStatistics.setMigratedVms(migratingList.size());
        cloudStatistics.addSolutionResult(CloudSim.clock(), dataCenterList, result);

    }

    private Set<Vm> getMigrationVms(List<Vm> greenVmList) {

        GreenDataCenter greenDataCenter = (GreenDataCenter) dataCenterList.get(0);
        List<Vm> migratingVms = new ArrayList<Vm>();
        int vmNr;
        Random rand = new Random();
        double totalEnergy;
        if (CloudController.SIMULATION_TYPE == CloudController.HEAT_VARIATION_SIMULATION) {
            totalEnergy = greenDataCenter.getGreenEnergyQuantity() + greenDataCenter.getHeatGained();
        } else {
            totalEnergy = greenDataCenter.getGreenEnergyQuantity();
        }
        if (greenDataCenter.getTotalEnergy() <= totalEnergy) {
            for (Vm vm : greenVmList) {
                GreenDataCenter dc = (GreenDataCenter) vm.getHost().getDatacenter();
                if (dc.getGreenEnergyQuantity() < 0.5) {
                    migratingVms.add(vm);
                }
            }
            double energyRatio = (greenDataCenter.getTotalEnergy()) / totalEnergy;
            double dc1_vm_nr = greenVmList.size() - migratingVms.size();
            double ratio_diff = (1 - energyRatio);
            vmNr = (int) (ratio_diff * dc1_vm_nr / energyRatio);

        } else {
            for (Host h : greenDataCenter.getHostList()) {
                migratingVms.addAll(h.<GreenVm>getVmList());
            }
            double energyRatio = totalEnergy / greenDataCenter.getTotalEnergy();
            vmNr = (int) ((1 - energyRatio) * migratingVms.size());
        }

        if (vmNr <= 0) {
            vmNr = 1;
        }

        Set<Vm> migratingSet = new HashSet<Vm>();

        if (vmNr > migratingVms.size()) {
            migratingSet.addAll(migratingVms);

            return migratingSet;
        }

        while (migratingSet.size() < vmNr) {
            int index = rand.nextInt(migratingVms.size());
            GreenVm vm = (GreenVm) migratingVms.get(index);
            Host host = vm.getHost();
            if (host != null) {
                migratingSet.add(vm);
            }

        }
        return migratingSet;
    }

    private List<Vm> getGreenVmList() {
        List<Vm> greenVmList = new ArrayList<Vm>();
        for (Datacenter dc : dataCenterList) {
            List<GreenHost> greenHosts = dc.getHostList();
            for (GreenHost h : greenHosts) {
                greenVmList.addAll(h.getVmList());
            }
        }
        return greenVmList;
    }

    private void scheduleMigrations(FoodSource result) {
        for (Nectar n : result.getNectarList()) {
            Host host = n.getHost();
            Vm vm = n.getVm();
            GreenDataCenter dataCenter = (GreenDataCenter) vm.getHost().getDatacenter();
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("vm", vm);
            data.put("host", host);
            sendNow(dataCenter.getId(), CloudSimTags.VM_MIGRATE, data);
        }
    }


    public DatacenterBroker getBroker() {
        return broker;
    }

    public void setBroker(DatacenterBroker broker) {
        this.broker = broker;
    }

    public List<Datacenter> getDataCenterList() {
        return dataCenterList;
    }

    public void setDataCenterList(List<Datacenter> dataCenterList) {
        this.dataCenterList = dataCenterList;
    }

    public List<Host> getHostList() {
        return hostList;
    }

    public void setHostList(List<Host> hostList) {
        this.hostList = hostList;
    }

    public List<Vm> getVmList() {
        return vmList;
    }

    public void setVmList(List<Vm> vmList) {
        this.vmList = vmList;
    }

    public List<Cloudlet> getCloudletList() {
        return cloudletList;
    }

    public void setCloudletList(List<Cloudlet> cloudletList) {
        this.cloudletList = cloudletList;
    }

    public void setWindSpeedMap(Map<String, List<Double>> windSpeedMap) {
        this.windSpeedMap = windSpeedMap;
    }

    public void setSimulationPeriod(int simulationPeriod) {
        this.simulationPeriod = simulationPeriod;
    }

    public Map<Integer, Map<String, Double>> getMigrationResults() {
        return migrationResults;
    }

    public void setMigrationResults(Map<Integer, Map<String, Double>> migrationResults) {
        this.migrationResults = migrationResults;
    }

    public CloudStatistics getCloudStatistics() {
        return cloudStatistics;
    }

    public Map<String, Double> getResultForDataCenter(Double time, int dataCenterId) {
        GreenDataCenter dataCenter = getGreenDataCenter(dataCenterId);
        return cloudStatistics.getStepResultsForDataCenter(time, dataCenter);
    }

    private GreenDataCenter getGreenDataCenter(int dataCenterId) {
        GreenDataCenter dataCenter = null;
        for (Datacenter dc : dataCenterList) {
            if ((dc.getId() - 3) == dataCenterId) {
                dataCenter = (GreenDataCenter) dc;
                break;
            }
        }
        return dataCenter;
    }

    public Map<Double, Map<String, Double>> getOverallResultsForDataCenter(int dataCenterId) {
        GreenDataCenter dataCenter = getGreenDataCenter(dataCenterId);
        return cloudStatistics.getOverallResultsForDatacenter(dataCenter);
    }

    public List<Map<String, Object>> getGraphResultsForDataCenter(int dataCenterId) {
        GreenDataCenter dataCenter = getGreenDataCenter(dataCenterId);
        if (CloudController.SIMULATION_TYPE == CloudController.CONSUMED_ENERGY_VARIATION_SIMULATION ||
                CloudController.SIMULATION_TYPE == CloudController.HEAT_VARIATION_SIMULATION) {
            return cloudStatistics.getGraphResultsForDatacenter(dataCenter);
        } else if (CloudController.SIMULATION_TYPE == CloudController.ENERGY_HIGH_COST_VARIATION_SIMULATION ||
                CloudController.SIMULATION_TYPE == CloudController.ENERGY_LOW_COST_VARIATION_SIMULATION ||
                CloudController.SIMULATION_TYPE == CloudController.LOW_LATENCY_VARIATION_SIMULATION ||
                CloudController.SIMULATION_TYPE == CloudController.HIGH_LATENCY_VARIATION_SIMULATION) {
            return cloudStatistics.getResultForEnergyCost(dataCenter);
        }
        return null;
    }

    public double getSimulationProgress() {
        double progress = (CloudSim.clock() / simulationPeriod) * 100;
        if (progress > 100) {
            progress = 100;
        }
        return progress;
    }

    public GreenDataCenter getGreenDatacenter() {
        return (GreenDataCenter) dataCenterList.get(0);
    }
}
