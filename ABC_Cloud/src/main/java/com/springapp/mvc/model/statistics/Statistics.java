package com.springapp.mvc.model.statistics;

import com.springapp.mvc.model.abc.FoodSource;
import com.springapp.mvc.model.cloud.GreenDataCenter;
import org.cloudbus.cloudsim.Datacenter;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniel on 4/15/2015.
 * class for interpreting the results
 */
public class Statistics {

    private static final String FILE_NAME = "results.txt";
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
            writer.println("========================================================================================================");
            writer.println("Time \t | Green Energy \t | Brown Energy \t | Server Energy \t | Heat Recovered \t | Cooling Energy");
            writer.println("========================================================================================================");
            for (int i = 0; i < timeList.size(); i++) {
                StringBuilder sb = new StringBuilder();
                double time = timeList.get(i);
                double greenEnergy = greenEnergyList.get(i);
                double brownEnergy = brownEnergyList.get(i);
                double serverEnergy = serverEnergyList.get(i);
                double heat = heatList.get(i);
                double cooling = coolingList.get(i);
                sb.append(time);
                sb.append("\t");
                sb.append(greenEnergy);
                sb.append("\t");
                sb.append(brownEnergy);
                sb.append("\t");
                sb.append(serverEnergy);
                sb.append("\t");
                sb.append(heat);
                sb.append("\t");
                sb.append(cooling);
                sb.append("\t");
                writer.println(sb.toString());
            }

        }
        writer.close();
    }

    private static void initWriter() {
        if (writer == null) {
            try {
                writer = new PrintWriter(FILE_NAME, ENCODING);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }


}
