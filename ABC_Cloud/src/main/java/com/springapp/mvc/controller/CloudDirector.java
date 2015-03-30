package com.springapp.mvc.controller;

import com.springapp.mvc.model.cloud.FederationOfDataCenter;

import java.io.FileNotFoundException;

/**
 * Created by Daniel on 3/21/2015.
 */
public class CloudDirector {

    public FederationOfDataCenter constructFederationOfDataCenters(CloudBuilder cloudBuilder) throws FileNotFoundException {
        cloudBuilder.createHosts();
        cloudBuilder.createDataCenter();
        cloudBuilder.createBroker();
        cloudBuilder.createVMs();
        cloudBuilder.createCloudletss();
        FederationOfDataCenter federationOfDataCenter = cloudBuilder.createFederationOfDataCenters();
        federationOfDataCenter.setHostList(cloudBuilder.getHostList());
        federationOfDataCenter.setDataCenterList(cloudBuilder.getDataCenterList());
        federationOfDataCenter.setBroker(cloudBuilder.getBroker());
        federationOfDataCenter.setVmList(cloudBuilder.getVmList());
        federationOfDataCenter.setCloudletList(cloudBuilder.getCloudletList());
        return federationOfDataCenter;
    }
}
