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

    public int data_center_nr = 5;
    public int vm_nr = 1;
    public Map<Integer, Integer> DC_to_server = new HashMap<Integer, Integer>();

           public Resources(){

               DC_to_server.put(1,3);
               DC_to_server.put(2,4);
               DC_to_server.put(3,5);
               DC_to_server.put(4,2);
               DC_to_server.put(5,1);



           }

}
