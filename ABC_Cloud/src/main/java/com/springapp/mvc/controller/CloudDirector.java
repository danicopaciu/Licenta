package com.springapp.mvc.controller;

import com.springapp.mvc.model.cloud.FederationOfDataCenter;

import java.io.FileNotFoundException;

public class CloudDirector {

    public FederationOfDataCenter constructFederationOfDataCenters(CloudBuilder cloudBuilder) throws FileNotFoundException {
        FederationOfDataCenter federationOfDataCenter = cloudBuilder.createFederationOfDataCenters();
        federationOfDataCenter.setHostList(cloudBuilder.createHosts());
        federationOfDataCenter.setDataCenterList(cloudBuilder.createDataCenter());
        federationOfDataCenter.setBroker(cloudBuilder.createBroker());
        federationOfDataCenter.setVmList(cloudBuilder.createVMs());
        federationOfDataCenter.setCloudletList(cloudBuilder.createCloudletss());
        return federationOfDataCenter;
    }
}
