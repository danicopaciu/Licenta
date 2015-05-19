package com.springapp.mvc.web.controller;

import com.springapp.mvc.controller.CloudController;
import com.springapp.mvc.model.json.JsonParser;
import com.springapp.mvc.model.json.impl.JsonParserImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
 * Created by Daniel on 5/14/2015.
 * Simulation Controller class
 */
@Controller
@RequestMapping("/")
public class SimulationController {

    private CloudController cloudController;

    @RequestMapping(value = "simulation", method = RequestMethod.GET)
    public String renderSimulationPage(ModelMap map) {
        return "simulation";
    }

    @RequestMapping(value = "startSimulation", method = RequestMethod.POST)
    public String startSimulation(@RequestParam("vmNumber") int vmNumber,
                                  @RequestParam("hostNumber") int hostNumber,
                                  @RequestParam("simulationPeriod") String simPeriod,
                                  @RequestParam("simulationType") String simType,
                                  Map<String, Object> map) {

        int hours = getHours(simPeriod);
        int period = getPeriod(hours);
        cloudController = new CloudController();
        cloudController.start(vmNumber, hostNumber, period);
        Map<Double, Map<String, Double>> simulationResult = cloudController.getOverallResults(0);
        JsonParser parser = new JsonParserImpl();
        List<List<Double>> graphList = cloudController.getGraphResults(0);
        String json = parser.toJson(graphList);
        map.put("result", simulationResult);
        map.put("json", json);
        return "simulation";
    }

    private int getPeriod(int hours) {
        int period;
        int minutes = hours * 60;
        int seconds = minutes * 60;
        period = seconds / 300;
        period *= 300;
        return period;
    }

    private int getHours(String period) {
        String[] data = period.split(" ");
        return Integer.parseInt(data[0]);
    }

    @RequestMapping(value = "getStatistics/{dataCenterId}", method = RequestMethod.GET)
    public
    @ResponseBody
    String getStatistics(@PathVariable("dataCenterId") int dataCenterId) {

        Map<String, Double> migrationResults = cloudController.getPartialResults(dataCenterId);
        return new JsonParserImpl().toJson(migrationResults);
    }

    @RequestMapping(value = "simulationProgress", method = RequestMethod.GET)
    public
    @ResponseBody
    int getCurrentProgress() {
        return (int) cloudController.getSimulationProgress();
    }

}
