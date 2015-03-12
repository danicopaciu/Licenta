package com.springapp.mvc.model;

import org.cloudbus.cloudsim.power.PowerDatacenter;

import java.util.List;

/**
 * Created by Daniel on 3/12/2015.
 */
public class FederationOfDatacenters {

    private List<PowerDatacenter> datacenterList;

    public FederationOfDatacenters(List<PowerDatacenter> datacenterList) {
        this.datacenterList = datacenterList;
    }
}
