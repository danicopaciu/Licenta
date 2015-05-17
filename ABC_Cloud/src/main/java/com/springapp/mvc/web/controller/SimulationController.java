package com.springapp.mvc.web.controller;

import com.springapp.mvc.controller.CloudController;
import com.springapp.mvc.model.json.impl.JsonParserImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
                                  ModelMap map) {

        int hours = getHours(simPeriod);
        int period = getPeriod(hours);
        cloudController = new CloudController();
        cloudController.start(vmNumber, hostNumber, period);
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

    @RequestMapping(value = "ajaxCall", method = RequestMethod.GET)
    public @ResponseBody String getTime() {

        Map<Integer, Map<String, Double>> migrationResults = cloudController.getResults();

        String json = new JsonParserImpl().toJson(migrationResults);


        return json;
    }

}
