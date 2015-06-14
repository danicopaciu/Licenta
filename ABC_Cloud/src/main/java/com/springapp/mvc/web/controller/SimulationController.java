package com.springapp.mvc.web.controller;

import com.springapp.mvc.controller.CloudController;
import com.springapp.mvc.model.json.JsonParser;
import com.springapp.mvc.model.json.impl.JsonParserImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
                                  @RequestParam("simulationType") int simType,
                                  Map<String, Object> map) {

        int hours = getHours(simPeriod);
        int period = getPeriod(hours);
        cloudController = new CloudController();
//        startCloudSimulation(vmNumber, hostNumber, period, simType);
//        cloudController.start(vmNumber, hostNumber, period);
        JsonParser parser = new JsonParserImpl();
        List<Map<String, Object>> graphList = startCloudSimulation(vmNumber, hostNumber, period, simType);
        String json = parser.toJson(graphList);
        Map<Double, Map<String, Double>> simulationResult = cloudController.getOverallResults(0);
        map.put("result", simulationResult);
        map.put("json_result", json);
        return "simulation";
    }

    private List<Map<String, Object>> startCloudSimulation(int vmNumber, int hostNumber, int period, int simType) {
        List<Map<String, Object>> result1 = null;
        List<Map<String, Object>> result2;
        switch (simType) {
            case 1:
                CloudController.SIMULATION_TYPE = 1;
                cloudController.start(vmNumber, hostNumber, period);
                result1 = cloudController.getGraphResults(0);
                break;
            case 2:
                CloudController.SIMULATION_TYPE = CloudController.CONSUMED_ENERGY_VARIATION_SIMULATION;
                cloudController.start(vmNumber, hostNumber, period);
                result1 = cloudController.getGraphResults(0);
                CloudController.SIMULATION_TYPE = CloudController.HEAT_VARIATION_SIMULATION;
                cloudController.start(vmNumber, hostNumber, period);
                result2 = cloudController.getGraphResults(0);
                result1.addAll(result2);
                break;
            case 3:
                CloudController.SIMULATION_TYPE = CloudController.LOW_LATENCY_VARIATION_SIMULATION;
                cloudController.start(vmNumber, hostNumber, period);
                result1 = cloudController.getGraphResults(0);
                CloudController.SIMULATION_TYPE = CloudController.HIGH_LATENCY_VARIATION_SIMULATION;
                cloudController.start(vmNumber, hostNumber, period);
                result2 = cloudController.getGraphResults(0);
                result1.addAll(result2);
                break;
            case 4:
                CloudController.SIMULATION_TYPE = CloudController.ENERGY_LOW_COST_VARIATION_SIMULATION;
                cloudController.start(vmNumber, hostNumber, period);
                result1 = cloudController.getGraphResults(0);
                CloudController.SIMULATION_TYPE = CloudController.ENERGY_HIGH_COST_VARIATION_SIMULATION;
                cloudController.start(vmNumber, hostNumber, period);
                result2 = cloudController.getGraphResults(0);
                result1.addAll(result2);
        }
        return result1;
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
