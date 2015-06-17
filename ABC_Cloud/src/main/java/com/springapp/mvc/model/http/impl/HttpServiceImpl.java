package com.springapp.mvc.model.http.impl;


import com.springapp.mvc.model.http.HttpService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpServiceImpl implements HttpService {


    @Override
    public String makeHttpCall(String host, List<String> attributeList,
                               Map<String, String> queryParams, String httpMethod,
                               String responseType, String body) throws IOException {

        HttpURLConnection connection = getHttpURLConnection(host, attributeList, queryParams, httpMethod);
        if (httpMethod.equals(HTTP_POST)) {
            connection.setRequestProperty("Content-Type", responseType);
            connection.setDoOutput(true);
            if (body != null) {
                OutputStream os = connection.getOutputStream();
                os.write(body.getBytes());
                os.flush();
            }

        } else if (httpMethod.equals(HTTP_GET)) {
            connection.setRequestProperty("Accept", responseType);
        }
        if (checkHttpResponse(connection, httpMethod)) {
            return getHttpResponse(connection);
        }
        return null;
    }

    private HttpURLConnection getHttpURLConnection(String host, List<String> attributeList,
                                                   Map<String, String> queryParams, String httpMethod) throws IOException {
        URLBuilder urlBuilder = new URLBuilder();
        URL url = new URL(urlBuilder.buildURL(host, attributeList, queryParams));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(httpMethod);
        return connection;
    }

    private boolean checkHttpResponse(HttpURLConnection connection, String httpMethod) throws IOException {
        if (httpMethod.equals(HTTP_GET)) {
            return checkHttpResponseCode(connection, HttpURLConnection.HTTP_OK);
        } else if (httpMethod.equals(HTTP_POST)) {
            return checkHttpResponseCode(connection, HttpURLConnection.HTTP_OK);
        }
        return false;
    }

    private String getHttpResponse(HttpURLConnection connection) throws IOException {
        InputStream inputStream = connection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String responseString = buildResponse(bufferedReader);
        connection.disconnect();
        return responseString;
    }

    private String buildResponse(BufferedReader bufferedReader) throws IOException {
        String output;
        StringBuilder stringBuilder = new StringBuilder();
        while ((output = bufferedReader.readLine()) != null) {
            stringBuilder.append(output);
        }
        return stringBuilder.toString();
    }

    private boolean checkHttpResponseCode(HttpURLConnection connection, int codeResponse) throws IOException {
        if (connection.getResponseCode() != codeResponse) {
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            throw new RuntimeException("Failed : HTTP error code: " + responseCode);
        }
        return true;
    }

}
