package com.springapp.mvc.model.json;

/**
 * Created by Daniel on 12/13/2014.
 */
public interface JsonParser {

    public Object fromJson(String json, Class c);

    public String toJson(Object object);
}
