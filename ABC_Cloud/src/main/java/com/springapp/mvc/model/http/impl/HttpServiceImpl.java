package com.springapp.mvc.model.http.impl;


import com.springapp.mvc.model.http.HttpService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class HttpServiceImpl implements HttpService {


    @Override
    public String makeHttpCall(String host, List<String> attributeList,
                               Map<String, String> queryParams, String httpMethod, String responseType, String body) throws IOException {
        URL url = new URL(buildURL(host, attributeList, queryParams));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(httpMethod);
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

    private String buildURL(String host, List<String> attributeList,
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
                sb.append("?");
                int index = 0;
                for (Map.Entry<String, String> queryParam : queryParams
                        .entrySet()) {
                    if (index != 0) {
                        sb.append("&");
                    }
                    sb.append(queryParam.getKey())
                            .append("=")
                            .append(queryParam.getValue());
                    index++;
                }
            }
        }
    }

    private void appendAttributes(List<String> attributeList, StringBuilder sb) {
        if (attributeList != null) {
            for (String attribute : attributeList) {
                sb.append("/");
                sb.append(attribute);
            }
        }
    }

    public void makePostRequest(String input) {
        try {
            URL url = new URL("http://localhost:8080/packetTracker/user/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStream os = conn.getOutputStream();
            os.write(input.getBytes());
            os.flush();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_CREATED) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                System.out.println(output);
            }

            conn.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
