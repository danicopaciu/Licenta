package com.springapp.mvc.model.statistics;

import com.springapp.mvc.model.abc.FoodSource;
import com.springapp.mvc.model.abc.Nectar;
import com.springapp.mvc.model.cloud.GreenDataCenter;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

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

    private static final String FILE_PATH = "D:\\GithubRepositories\\Licenta\\ABC_Cloud\\results_";
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
            Map<String, List<Double>> statistics = dc.getStatistics();
            List<Double> timeList = statistics.get(GreenDataCenter.TIME);
            List<Double> greenEnergyList = statistics.get(GreenDataCenter.GREEN_ENERGY);
            List<Double> brownEnergyList = statistics.get(GreenDataCenter.BROWN_ENERGY);
            List<Double> serverEnergyList = statistics.get(GreenDataCenter.SERVERS_ENERGY);
            List<Double> heatList = statistics.get(GreenDataCenter.HEAT);
            List<Double> coolingList = statistics.get(GreenDataCenter.COOLING);
            writer.println(dc.getName());
            writer.println("Time,Green Energy,Brown Energy,Server Energy,Heat Recovered,Cooling Energy");
            for (int i = 0; i < timeList.size(); i++) {
                StringBuilder sb = new StringBuilder();
                double time = timeList.get(i);
                double greenEnergy = greenEnergyList.get(i);
                double brownEnergy = brownEnergyList.get(i);
                double serverEnergy = serverEnergyList.get(i);
                double heat = heatList.get(i);
                double cooling = coolingList.get(i);
                sb.append(time);
                sb.append(",");
                sb.append(greenEnergy);
                sb.append(",");
                sb.append(brownEnergy);
                sb.append(",");
                sb.append(serverEnergy);
                sb.append(",");
                sb.append(heat);
                sb.append(",");
                sb.append(cooling);
                sb.append(",");
                writer.println(sb.toString());
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
                String fileName = FILE_PATH + dateFormat.format(date) + FILE_EXTENSION;
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
                ((GreenDataCenter) dc).addToMigratingInVms(vms[0]);
                ((GreenDataCenter) dc).addToMigratingOutVms(vms[1]);
            }
        }
    }


}
