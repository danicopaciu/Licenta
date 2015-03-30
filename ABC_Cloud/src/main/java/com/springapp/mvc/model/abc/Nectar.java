package com.springapp.mvc.model.abc;

import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import org.cloudbus.cloudsim.Host;

/**
 * Created by Daniel on 3/14/2015.
 */
public class Nectar {

    private GreenHost host;

    private GreenVm vm;

    private double latency;

    public Nectar(GreenHost host, GreenVm vm) {
        this.host = host;
        this.vm = vm;
        this.latency = 0;
    }

    public double getLatency() {
        return latency;
    }

    public void setLatency(double latency) {
        this.latency = latency;
    }

    public GreenHost getHost() {
        return host;
    }

    public void setHost(GreenHost host) {
        this.host = host;
    }

    public GreenVm getVm() {
        return vm;
    }

    public void setVm(GreenVm vm) {
        this.vm = vm;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Nectar) {
            Nectar comparisonObj = (Nectar) obj;
            if (host.getId() == comparisonObj.getHost().getId() &&
                    vm.getId() == comparisonObj.getVm().getId()) {
                return true;
            }
        }
        return false;
    }

    public double setLatency() {
        Host currentHost = vm.getHost();
        if (currentHost != null && currentHost instanceof GreenHost) {
            GreenHost greenHost = (GreenHost) currentHost;
            double sourceBw = greenHost.getAvailableBandwidth();
            double destinationBw = host.getAvailableBandwidth();
            double availableBw = Math.min(sourceBw, destinationBw);
            double ram = vm.getRam();
            latency = ram / availableBw;
        }
        return latency;
    }
}
