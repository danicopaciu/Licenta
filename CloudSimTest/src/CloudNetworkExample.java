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
 * Created by Daniel on 3/1/2015.
 * * A simple example showing how to create
 * two data enters with one host each and
 * run cloudlets of two users with network
 * topology on them.
 */
public class CloudNetworkExample {

    private static List<Vm> vmList1;
    private static List<Vm> vmList2;

    private static List<Cloudlet> cloudletList1;
    private static List<Cloudlet> cloudletList2;

    public static void main(String[] args) {
        Log.printLine("Starting example...");

        int userNum = 2;
        Calendar calendar = Calendar.getInstance();
        boolean traceFlag = false;

        CloudSim.init(userNum, calendar, traceFlag);

        Datacenter datacenter0 = createDataCenter("Datacenter0");
        Datacenter datacenter1 = createDataCenter("Datacenter1");

        DatacenterBroker broker1 = createBroker(1);
        int broker1Id = broker1.getId();
        DatacenterBroker broker2 = createBroker(2);
        int broker2Id = broker2.getId();

        vmList1 = new ArrayList<Vm>();
        vmList2 = new ArrayList<Vm>();

        int vmid = 0;
        long size = 10000; //image size (MB)
        int mips = 250;
        int ram = 512; //vm memory (MB)
        long bw = 1000;
        int pesNumber = 1; //number of cpus
        String vmm = "Xen"; //VMM name

        Vm vm1 = new Vm(vmid, broker1Id, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
        Vm vm2 = new Vm(vmid, broker2Id, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());

        vmList1.add(vm1);
        vmList2.add(vm2);

        broker1.submitVmList(vmList1);
        broker2.submitVmList(vmList2);

        cloudletList1 = new ArrayList<Cloudlet>();
        cloudletList2 = new ArrayList<Cloudlet>();

        int id = 0;
        long length = 40000;
        long fileSize = 300;
        long outputSize = 300;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        Cloudlet cloudlet1 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
        cloudlet1.setUserId(broker1Id);

        Cloudlet cloudlet2 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
        cloudlet2.setUserId(broker2Id);

        cloudletList1.add(cloudlet1);
        cloudletList2.add(cloudlet2);

        broker1.submitCloudletList(cloudletList1);
        broker2.submitCloudletList(cloudletList2);

        NetworkTopology.buildNetworkTopology("topology.brite");

        int briteNode = 0;
        NetworkTopology.mapNode(datacenter0.getId(), briteNode);

        briteNode = 2;
        NetworkTopology.mapNode(datacenter1.getId(), briteNode);

        briteNode = 3;
        NetworkTopology.mapNode(broker1.getId(), briteNode);

        briteNode = 4;
        NetworkTopology.mapNode(broker2.getId(), briteNode);

        CloudSim.startSimulation();

        List<Cloudlet> newList1 = broker1.getCloudletReceivedList();
        List<Cloudlet> newList2 = broker2.getCloudletReceivedList();

        CloudSim.stopSimulation();

        Log.print("=============> User "+broker1Id+"    ");
        printCloudletList(newList1);

        Log.print("=============> User "+broker2Id+"    ");
        printCloudletList(newList2);

        Log.printLine("NetworkExample3 finished!");




    }

    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
                "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
                Log.print("SUCCESS");

                DecimalFormat dft = new DecimalFormat("###.##");
                Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
                        indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())+
                        indent + indent + dft.format(cloudlet.getFinishTime()));
            }
        }

    }

    private static DatacenterBroker createBroker(int id) {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker"+id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    private static Datacenter createDataCenter(String name) {

        List<Host> hostList = new ArrayList<Host>();

        List<Pe> peList = new ArrayList<Pe>();

        int mips = 1000;

        peList.add(new Pe(0, new PeProvisionerSimple(mips)));

        int hostId=0;
        int ram = 2048; //host memory (MB)
        long storage = 1000000; //host storage
        int bw = 10000;


        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList,
                        new VmSchedulerSpaceShared(peList)
                )
        ); // This is our machine

        String arch = "x86";      // system architecture
        String os = "Linux";          // operating system
        String vmm = "Xen";
        double time_zone = 10.0;         // time zone this resource located
        double cost = 3.0;              // the cost of using processing in this resource
        double costPerMem = 0.05;               // the cost of using memory in this resource
        double costPerStorage = 0.001;  // the cost of using storage in this resource
        double costPerBw = 0.0;                 // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();    //we are not adding SAN devices by now

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);


        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }
}
