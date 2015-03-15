package com.springapp.mvc.model.abc;

import com.springapp.mvc.model.cloud.GreenHost;
import com.springapp.mvc.model.cloud.GreenVm;

/**
 * Created by Daniel on 3/14/2015.
 */
public class Nectar {

    private GreenHost host;

    private GreenVm vm;

    public Nectar(GreenHost host, GreenVm vm) {
        this.host = host;
        this.vm = vm;
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
        if(obj instanceof Nectar){
            Nectar comparisonObj = (Nectar) obj;
            if(host.getId() == comparisonObj.getHost().getId() &&
                    vm.getId() == comparisonObj.getVm().getId()){
                return true;
            }
        }
        return false;
    }
}
