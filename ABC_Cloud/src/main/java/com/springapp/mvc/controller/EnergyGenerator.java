package com.springapp.mvc.controller;

import com.springapp.mvc.model.http.HttpService;
import com.springapp.mvc.model.http.impl.HttpServiceImpl;
import com.springapp.mvc.model.json.impl.JsonParserImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniel on 3/27/2015.
 */
public class EnergyGenerator {

    public static void main(String[] args) {
        EnergyGenerator eg = new EnergyGenerator();
        try {
            eg.init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() throws IOException {

        HttpService httpService = new HttpServiceImpl();
        long startTime = 1427500800;
        PrintWriter writer = new PrintWriter("wind_speed_Datacenter_4.txt", "UTF-8");
        for (int i = 0; i < 289; i++) {
            long time = startTime + i * 300;
            List<String> attributeList = new ArrayList<String>();
            attributeList.add("forecast");
            attributeList.add("8c993da7f47dde5d76a75b0abb035a8f");
            attributeList.add("46.766667046,23.5833330," + time);
            String response = httpService.makeHttpCall("https://api.forecast.io", attributeList, null, "GET", "application/json", null);
            JsonParserImpl parser = new JsonParserImpl();
            double wind = parser.getElement(response, "windSpeed");
            writer.println(time + ": " + wind);
        }
        writer.close();
    }
}
