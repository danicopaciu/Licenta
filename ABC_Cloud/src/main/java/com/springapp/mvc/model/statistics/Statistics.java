package com.springapp.mvc.model.statistics;

import com.springapp.mvc.model.abc.FoodSource;
import com.springapp.mvc.model.abc.Nectar;
import com.springapp.mvc.model.cloud.FederationOfDataCenter;
import com.springapp.mvc.model.cloud.GreenDataCenter;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import java.awt.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 4/15/2015.
 * class for interpreting the results
 */
public class Statistics {

    private static final String FILE_PATH = "results.csv";
    private static final String FILE_EXTENSION = ".csv";
    private static final String ENCODING = "utf-8";
    private static FoodSource solution;
    private static Map<Datacenter, List<Double>> datacenterResults;
    private static double clock;
    private static PrintWriter writer;

    public static void init(FoodSource currentSolution,
                            Double currentClock) {
        solution = currentSolution;
        clock = currentClock;
        initWriter();
    }

    public static void printResults(List<GreenDataCenter> dataCenterList) {
        initWriter();
        for (GreenDataCenter dc : dataCenterList) {
            double time = 600 + FederationOfDataCenter.allocatedDC;
            writer.println(dc.getName());
            writer.println("Time,Green Energy,Brown Energy,Server Energy,Cooling Energy, HeatRecovered, DC Energy, VMs In, VMs Out, Total VMs");
            Map<Double, List<Double>> statistics = dc.getStatistics();
            while (time < FederationOfDataCenter.TIME_STOP) {
                double greenEnergy, brownEnergy, serverEnergy, coolingEnergy, heatRecovered, totalEnergy, vmsIn, vmsOut, totalVms;
                List<Double> values;

                    values = statistics.get(time);
                    if ((values == null) || (values.size() == 0)){
                        greenEnergy = 0;
                        brownEnergy = 0;
                        serverEnergy = 0;
                        coolingEnergy = 0;
                        heatRecovered = 0;
                        totalEnergy = 0;
                        vmsIn = 0;
                        vmsOut = 0;
                        totalVms = 0;
                    }else{
                        greenEnergy = values.get(GreenDataCenter.GREEN_ENERGY);
                        brownEnergy = values.get(GreenDataCenter.BROWN_ENERGY);
                        serverEnergy = values.get(GreenDataCenter.SERVERS_ENERGY);
                        coolingEnergy = values.get(GreenDataCenter.COOLING);
                        heatRecovered = values.get(GreenDataCenter.HEAT);
                        totalEnergy = values.get(GreenDataCenter.DATACENTER_ENERGY);
                        vmsIn = values.get(GreenDataCenter.VMS_IN);
                        vmsOut = values.get(GreenDataCenter.VMS_OUT);
                        totalVms = values.get(GreenDataCenter.TOTAL_VMS);
                    }
                    StringBuilder sb = new StringBuilder();
                    sb.append(time);
                    sb.append(",");
                    sb.append(greenEnergy);
                    sb.append(",");
                    sb.append(brownEnergy);
                    sb.append(",");
                    sb.append(serverEnergy);
                    sb.append(",");
                    sb.append(coolingEnergy);
                    sb.append(",");
                    sb.append(heatRecovered);
                    sb.append(",");
                    sb.append(totalEnergy);
                    sb.append(",");
                    sb.append(vmsIn);
                    sb.append(",");
                    sb.append(vmsOut);
                    sb.append(",");
                    sb.append(totalVms);
                    sb.append(",");
                    writer.println(sb.toString());
                    time += 300;

            }

        }
        writer.close();
        openFile();

    }

    private static void openFile() {
        try {
            Desktop dt = Desktop.getDesktop();
            dt.open(new File(FILE_PATH));
            Runtime.getRuntime()
                    .exec("rundll32 url.dll,FileProtocolHandler " + FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initWriter() {
        if (writer == null) {
            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();
                String fileName = FILE_PATH;
                writer = new PrintWriter(fileName, ENCODING);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public static void analizeSolution(List<GreenDataCenter> dataCenters, FoodSource solution) {
        Map<Datacenter, int[]> result = new HashMap<Datacenter, int[]>();
        for (Datacenter dc : dataCenters) {
            int[] vms = new int[2];
            vms[0] = vms[1] = 0;
            result.put(dc, vms);
        }
        for (Nectar n : solution.getNectarList()) {
            Vm vm = n.getVm();
            Host prevHost = vm.getHost();
            Host nextHost = n.getHost();
            Datacenter prevDc = prevHost.getDatacenter();
            Datacenter nextDc = nextHost.getDatacenter();
            int[] out = result.get(prevDc);
            out[1]++;
            int[] in = result.get(nextDc);
            in[0]++;
        }
        for (Datacenter dc : dataCenters) {
            if (dc instanceof GreenDataCenter) {
                int[] vms = result.get(dc);
                Double currentTime = CloudSim.clock();
                GreenDataCenter gdc = (GreenDataCenter) dc;
                gdc.setMigratingInVms(vms[0]);
                gdc.setMigratingOutVms(vms[1]);
                gdc.setTotalVms(gdc.getTotalVms() - vms[1] + vms[0]);
                gdc.putGreenEnergy(currentTime, gdc.getGreenEnergyQuantity());
                gdc.putBrownEnergy(currentTime, gdc.getBrownEnergyQuantity());
                gdc.putServerEnergy(currentTime, gdc.getPower());
                gdc.putCooling(currentTime, gdc.getCoolingEnergy());
                gdc.putHeat(currentTime, gdc.getHeatGained());
                gdc.putTotalEnergy(currentTime, gdc.getTotalEnergy());
                gdc.putVmsIn(currentTime, gdc.getMigratingInVms());
                gdc.putVmsOut(currentTime, gdc.getMigratingOutVms());
                gdc.putTotalVms(currentTime, (double) gdc.getTotalVms());
            }
        }
    }


}
