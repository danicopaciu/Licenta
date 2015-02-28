import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Daniel on 2/27/2015.
 * Second example for CloudSim
 */
public class CloudExample {

    public void run(){

        Log.printLine("Example started");

        int num_user = 2;
        Calendar calendar = Calendar.getInstance();
        boolean traceFlag = false;

        CloudSim.init(num_user, calendar, traceFlag);

        Datacenter datacenter0 = createDatacenter("Datacenter0");
        Datacenter datacenter1 = createDatacenter("Datacenter1");

        DatacenterBroker broker1 = createBroker(1);
        int brokerId1 = broker1.getId();

        DatacenterBroker broker2 = createBroker(2);
        int brokerId2 = broker2.getId();

        List<Vm> vmList1 = new ArrayList<Vm>();
        List<Vm> vmList2 = new ArrayList<Vm>();

        int vmId = 0;
        int mips = 250;
        long size = 10000;
        int ram = 512;
        long bw = 1000;
        int pesNumber = 1; //numver of cpus
        String vmm = "Xen";

        //belongs to user1
        Vm vm1 = new Vm(vmId, brokerId1, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
        //belongs to user2
        Vm vm2 = new Vm(vmId, brokerId1, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

        vmList1.add(vm1);
        vmList2.add(vm2);

        broker1.submitVmList(vmList1);
        broker2.submitVmList(vmList2);

        List<Cloudlet> cloudletList1 = new ArrayList<Cloudlet>();
        List<Cloudlet> cloudletList2 = new ArrayList<Cloudlet>();

        int id = 0;
        int length = 40000;
        int fileSize = 300;
        int outputSize = 300;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet cloudlet1 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
        cloudlet1.setUserId(brokerId1);
        Cloudlet cloudlet2 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
        cloudlet2.setUserId(brokerId2);

        cloudletList1.add(cloudlet1);
        cloudletList2.add(cloudlet2);

        broker1.submitCloudletList(cloudletList1);
        broker2.submitCloudletList(cloudletList2);

        CloudSim.startSimulation();

        List<Cloudlet> newList1 = broker1.getCloudletReceivedList();
        List<Cloudlet> newList2 = broker2.getCloudletReceivedList();

        CloudSim.stopSimulation();

        Log.print("=============> User "+brokerId1+"    ");
        printCloudletList(newList1);

        Log.print("=============> User "+brokerId2+"    ");
        printCloudletList(newList2);

        Log.printLine("CloudSimExample5 finished!");



    }

    private Datacenter createDatacenter(String s) {
        List<Host> hostList = new ArrayList<Host>();
        List<Pe> peList = new ArrayList<Pe>();

        int mips = 1000;

        peList.add(new Pe(0, new PeProvisionerSimple(mips)));

        int hostId = 0;
        int ram = 2048;
        long storage = 100000;
        int bw = 10000;

        Host host = new Host(hostId, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerSpaceShared(peList));
        hostList.add(host);

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double timezone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;
        LinkedList<Storage> storageList = new LinkedList<Storage>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, timezone, cost, costPerMem, costPerStorage,costPerBw);
        Datacenter datacenter;
        try {
            datacenter = new Datacenter(s, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return datacenter;
    }

    private DatacenterBroker createBroker(int i) {
        DatacenterBroker broker;
        try {
            broker = new DatacenterBroker("Broker" + i);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    private void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
                Log.print("SUCCESS");

                Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())+
                        indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }
    }

    public static void main(String[] args) {
        CloudExample cloudExample = new CloudExample();
        cloudExample.run();
    }
}
