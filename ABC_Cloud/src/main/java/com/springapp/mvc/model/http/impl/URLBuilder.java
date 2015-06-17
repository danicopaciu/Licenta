package com.springapp.mvc.model.http.impl;

import java.util.List;
import java.util.Map;


public class URLBuilder {

    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";
    private static final String EQUAL = "&";
    private static final String SLASH = "/";


    public String buildURL(String host, List<String> attributeList,
                           Map<String, String> queryParams) {
        StringBuilder sb = new StringBuilder();
        if (host != null) {
            sb.append(host);
            appendAttributes(attributeList, sb);
            appendQueryParams(queryParams, sb);
        }
        return sb.toString();
    }

    private void appendQueryParams(Map<String, String> queryParams, StringBuilder sb) {
        if (queryParams != null) {
            if (!queryParams.isEmpty()) {
                sb.append(QUESTION_MARK);
                int index = 0;
                for (Map.Entry<String, String> queryParam : queryParams
                        .entrySet()) {
                    if (index != 0) {
                        sb.append(AMPERSAND);
                    }
                    sb.append(queryParam.getKey())
                            .append(EQUAL)
                            .append(queryParam.getValue());
                    index++;
                }
            }
        }
    }

    private void appendAttributes(List<String> attributeList, StringBuilder sb) {
        if (attributeList != null) {
            for (String attribute : attributeList) {
                sb.append(SLASH);
                sb.append(attribute);
            }
        }
    }
}
