package com.springapp.mvc.model.http;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface HttpService {

    String HTTP_GET = "GET";
    String HTTP_POST = "POST";

    public String makeHttpCall(String host, List<String> attributeList,
                               Map<String, String> queryParams, String httpMethod, String responseType, String body) throws IOException;
}
