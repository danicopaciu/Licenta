package com.springapp.mvc.model.json.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.springapp.mvc.model.json.JsonParser;


/**
 * Created by Daniel on 12/13/2014.
 */
public class JsonParserImpl implements JsonParser {

    private Gson gson;

    public JsonParserImpl() {
        gson = new Gson();
    }

    @Override
    public Object fromJson(String json, Class c) {

        if (json != null && c != null) {
            return gson.fromJson(json, c);
        }
        return null;
    }

    public double getElement(String json, String memberName) {
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        JsonElement el = jsonObject.get("currently");
        JsonObject jsonObject1 = el.getAsJsonObject();
        return jsonObject1.get(memberName).getAsDouble();
    }

    @Override
    public String toJson(Object object) {
        if (object != null) {
            return gson.toJson(object);
        }
        return null;
    }
}
