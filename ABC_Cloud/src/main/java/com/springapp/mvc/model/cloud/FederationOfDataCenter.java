package com.springapp.mvc.model.cloud;

import com.springapp.mvc.controller.Resources;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Daniel on 3/12/2015.
 */
public class FederationOfDataCenter extends SimEntity {

    public static final int PERIODIC_EVENT = 67567;
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

    public void migrateVMs(GreenVm vm, GreenHost host) {
        //  System.out.println("Vm " + vm.getId() + " is on host " + vm.getHost().getId());
            GreenDataCenter dataCenter = (GreenDataCenter) host.getDatacenter();
            HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("vm", vm);
            data.put("host", vm);
            send(dataCenter.getId(), CloudSim.clock(), CloudSimTags.VM_MIGRATE, data);
            System.out.println("Vm " + vm.getId() + " is on host " + vm.getHost().getId());
    }

    @Override
    public void startEntity() {
        send(getId(), 600, PERIODIC_EVENT, new Object());
    }

    @Override
    public void processEvent(SimEvent ev) {
        if (ev == null){
            Log.printLine("Warning: " + CloudSim.clock() + ": " + this.getName() + ": Null event ignored.");
        } else {
            int tag = ev.getTag();
            switch(tag){
                case PERIODIC_EVENT: processPeriodicEvent(ev); break;
                default: Log.printLine("Warning: "+CloudSim.clock()+":"+this.getName()+": Unknown event ignored. Tag:" +tag);
            }
        }

    }

    private void processPeriodicEvent(SimEvent ev) {
        //your code here
        double clock = CloudSim.clock();
        System.out.print(clock + " " + getAllocatedDC() + " || ");

        computeGreenPower(windList.get((int) (clock - getAllocatedDC()) / 300) + 1);

        float delay = 300; //contains the delay to the next periodic event
        boolean generatePeriodicEvent = true; //true if new internal events have to be generated
        if (clock >= 86400) {
            generatePeriodicEvent = false;
            fileWriter.close();
        }


        if (generatePeriodicEvent) send(getId(),delay,PERIODIC_EVENT, null);
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
        final int vin = 3;
        final int vout = 25;
        final int pr = 225000;
        final int vr = 13;
        double energy;

        fileWriter.println(CloudSim.clock());

        for (GreenDataCenter dc : dataCenterList) {
            if (windSpeed < vin || windSpeed > vout) {
                energy = 0;
            } else if (windSpeed > vr && windSpeed < vout) {
                energy = pr;
            } else {
                energy = (pr * (windSpeed - vin)) / (vr - vin);
            }
            fileWriter.print(dc.getId() + " ");
            fileWriter.print(dc.getGreenEnergyQuantity() + " ");
            dc.setGreenEnergyQuantity(dc.getGreenEnergyQuantity() + Resources.SCHEDULING_INTERVAL * energy);
            fileWriter.println(dc.getGreenEnergyQuantity() + " ");
        }
    }

    public void createEvent(int entityId, double delay, int cloudSimTag, Object data) {
          send(entityId, delay, cloudSimTag, data);
    }

}
