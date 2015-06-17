package com.springapp.mvc.controller;

public class Resources {

    public final static double SCHEDULING_INTERVAL = 300;
    public final static double SIMULATION_LIMIT = 24 * 60 * 60;
    public final static int CLOUDLET_LENGTH = 2500 * (int) SIMULATION_LIMIT;
    public final static int DATACENTER_NUMBER = 5;
    public static final double ENERGY_PRICE = 0.0662 / 1000;      // 6.62 /cents/kwH
    public static final double HEAT_PRICE = 2.28 / 1000000;       // 2.28 /milionBTU
}
