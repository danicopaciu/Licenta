package com.springapp.mvc.examples.org.cloudbus.cloudsim.examples.power.planetlab;

import java.io.IOException;

/**
 * A simulation of a heterogeneous power aware data center that only applied DVFS, but no dynamic
 * optimization of the VM allocation. The adjustment of the hosts' power consumption according to
 * their CPU utilization is happening in the PowerDatacenter class.
 * <p/>
 * This example uses a real PlanetLab workload: 20110303.
 * <p/>
 * The remaining configuration parameters are in the Constants and PlanetLabConstants classes.
 * <p/>
 * If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:
 * <p/>
 * Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012
 *
 * @author Anton Beloglazov
 * @since Jan 5, 2012
 */
public class Dvfs {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws java.io.IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {
        boolean enableOutput = true;
        boolean outputToFile = false;
        String inputFolder = Dvfs.class.getClassLoader().getResource("workload/planetlab").getPath();
        String outputFolder = "output";
        String workload = "20110303"; // PlanetLab workload
        String vmAllocationPolicy = "dvfs"; // DVFS policy without VM migrations
        String vmSelectionPolicy = "";
        String parameter = "";

        new PlanetLabRunner(
                enableOutput,
                outputToFile,
                inputFolder,
                outputFolder,
                workload,
                vmAllocationPolicy,
                vmSelectionPolicy,
                parameter);
    }

}