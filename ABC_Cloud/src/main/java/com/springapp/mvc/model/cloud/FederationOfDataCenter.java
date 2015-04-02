package com.springapp.mvc.model.cloud;

import com.springapp.mvc.controller.Resources;
import com.springapp.mvc.model.abc.ArtificialBeeColony;
import com.springapp.mvc.model.abc.FoodSource;
import com.springapp.mvc.model.abc.Nectar;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by Daniel on 3/12/2015.
 */
public class FederationOfDataCenter extends SimEntity {

    public static final int PERIODIC_EVENT = 67567;
    public static final int DC_NUMBER = 67568;
    public static final int POWER_DATACENTER = 67569;
    public static double allocatedDC;
    private List<GreenDataCenter> dataCenterList;
    private List<GreenHost> hostList;
    private List<GreenVm> vmList;
    private List<Cloudlet> cloudletList;
    private List<Double> windList;
    private DatacenterBroker broker;
    private PrintWriter fileWriter;

    public FederationOfDataCenter(String name) {
        super(name);
        try {
            fileWriter = new PrintWriter("energies.txt", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
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
                    processPeriodicEvent(ev);
                    break;
                case DC_NUMBER:
                    allocatedDC = getAllocatedDC();
                    send(getId(), 200, POWER_DATACENTER, new Object());
                    send(getId(), 500 + allocatedDC, PERIODIC_EVENT, new Object());
                    break;
                case POWER_DATACENTER:
                    computeDataCenterPower();
                    break;
                default:
                    Log.printLine("Warning: " + CloudSim.clock() + ":" + this.getName() + ": Unknown event ignored. Tag:" + tag);
            }
        }

    }

    private void computeDataCenterPower() {
        double clock = CloudSim.clock();
        if (clock < 86100 + allocatedDC) {
            computeGreenPower(windList.get((int) (clock - getAllocatedDC()) / 300) + 1);
            send(getId(), 300, POWER_DATACENTER, new Object());
        }
    }

    private void processPeriodicEvent(SimEvent ev) {
        //your code here
        double clock = CloudSim.clock();
        System.out.print(clock + " " + getAllocatedDC() + " || ");
        migrateVMs();
        float delay = 300; //contains the delay to the next periodic event
        boolean generatePeriodicEvent = true; //true if new internal events have to be generated
        if (clock >= 86100 + allocatedDC) {
            generatePeriodicEvent = false;
            fileWriter.close();
            com.springapp.mvc.model.csv.Log.close();
        }


        if (generatePeriodicEvent) send(getId(), delay, PERIODIC_EVENT, new Object());
    }

    public double getAllocatedDC() {
        double counter = 0;
        for (GreenDataCenter dc : getDataCenterList()) {
            if (!dc.getVmList().isEmpty()) {
                counter++;
            }
        }
        return counter / 10;
    }

    @Override
    public void shutdownEntity() {

    }

    public List<Double> getWindList() {
        return windList;
    }

    public void setWindList(List<Double> windList) {
        this.windList = windList;
    }

    public DatacenterBroker getBroker() {
        return broker;
    }

    public void setBroker(DatacenterBroker broker) {
        this.broker = broker;
    }

    public List<GreenDataCenter> getDataCenterList() {
        return dataCenterList;
    }

    public void setDataCenterList(List<GreenDataCenter> dataCenterList) {
        this.dataCenterList = dataCenterList;
    }

    public List<GreenHost> getHostList() {
        return hostList;
    }

    public void setHostList(List<GreenHost> hostList) {
        this.hostList = hostList;
    }

    public List<GreenVm> getVmList() {
        return vmList;
    }

    public void setVmList(List<GreenVm> vmList) {
        this.vmList = vmList;
    }

    public List<Cloudlet> getCloudletList() {
        return cloudletList;
    }

    public void setCloudletList(List<Cloudlet> cloudletList) {
        this.cloudletList = cloudletList;
    }

    public void computeGreenPower(double windSpeed) {
        final int vIn = 3; //starting speed of energy production m/s
        final int vOut = 25; // finishing speed of energy production m/s
        final int pr = 225000; //windmill power w
        final int vr = 13; // speed for optimal production m/s
        double energy;

        fileWriter.println(CloudSim.clock());

        for (GreenDataCenter dc : dataCenterList) {
            if (windSpeed < vIn || windSpeed > vOut) {
                energy = 0;
            } else if (windSpeed > vr && windSpeed < vOut) {
                energy = pr;
            } else {
                energy = (pr * (windSpeed - vIn)) / (vr - vIn);
            }
            fileWriter.print(dc.getId() + " ");
            fileWriter.print(dc.getGreenEnergyQuantity() + " ");
            dc.setGreenEnergyQuantity(dc.getGreenEnergyQuantity() + Resources.SCHEDULING_INTERVAL * energy);
            fileWriter.println(dc.getGreenEnergyQuantity() + " ");
        }
    }

    private void migrateVMs() {

        List<Vm> greenVmList = new ArrayList<Vm>();
        for (GreenDataCenter dc : dataCenterList) {
            List<GreenHost> greenHosts = dc.getHostList();
            for (GreenHost h : greenHosts) {
                greenVmList.addAll(h.getVmList());
            }
        }
        Random rand = new Random();
        int vmNr = rand.nextInt(greenVmList.size() - 2) + 1;
        Set<GreenVm> migratingSet = new HashSet<GreenVm>();

        while (migratingSet.size() < vmNr) {

            int index = rand.nextInt(greenVmList.size() - 1);
            GreenVm vm = (GreenVm) greenVmList.get(index);
            if (vm.getHost() != null) {
                migratingSet.add(vm);
            }

        }
        List<GreenVm> migratingList = new ArrayList<GreenVm>();
        if (migratingSet.size() != 0) {
            migratingList.addAll(migratingSet);
        }
        List<GreenDataCenter> DCList = getDataCenterList();

        ArtificialBeeColony abc = new ArtificialBeeColony(DCList, migratingList);
        FoodSource result = abc.runAlgorithm();

        for (Nectar n : result.getNectarList()) {
            GreenHost host = n.getHost();
            GreenVm vm = n.getVm();
            GreenDataCenter dataCenter = (GreenDataCenter) vm.getHost().getDatacenter();
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("vm", vm);
            data.put("host", host);
            send(dataCenter.getId(), 0, CloudSimTags.VM_MIGRATE, data);
        }
    }


}
