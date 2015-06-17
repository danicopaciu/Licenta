package com.springapp.mvc.model.json;

public interface JsonParser {

    Object fromJson(String json, Class c);

    String toJson(Object object);
}
