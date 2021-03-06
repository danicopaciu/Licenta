package com.springapp.mvc.model.abc;

import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Vm;

public class Nectar {

    private Host host;

    private Vm vm;

    public Nectar(Host host, Vm vm) {
        this.host = host;
        this.vm = vm;
    }


    public Host getHost() {
        return host;
    }

    public void setHost(GreenHost host) {
        this.host = host;
    }

    public Vm getVm() {
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
}
