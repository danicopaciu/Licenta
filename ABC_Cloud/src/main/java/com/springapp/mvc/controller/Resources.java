package com.springapp.mvc.controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Bogdan
 * Date: 15/03/15
 * Time: 12:54
 * To change this template use File | Settings | File Templates.
 */
public class Resources {

    public final static double SCHEDULING_INTERVAL = 300;
    public final static double SIMULATION_LIMIT = 24 * 60 * 60;
    public final static int CLOUDLET_LENGTH	= 2500 * (int) SIMULATION_LIMIT;
    public final static int DATACENTER_NUMBER = 5;
    public final static int VM_NUMBER = 1200;
    public final static int HOST_NUMBER_PER_DATACENTER = 200;
    public static final int HOST_NUMBER = DATACENTER_NUMBER * HOST_NUMBER_PER_DATACENTER;
    public static final double energyPrice = 0.0662 / 1000;      // 6.62 /cents/kwH
    public static final double heatPrice = 2.28 / 1000000;       // 2.28 /milionBTU

    public Map<Integer, Integer> DC_to_server = new HashMap<Integer, Integer>();

           public Resources(){

               DC_to_server.put(1,3);
               DC_to_server.put(2,4);
               DC_to_server.put(3,5);
               DC_to_server.put(4,2);
//               DC_to_server.put(5,1);



           }

}
