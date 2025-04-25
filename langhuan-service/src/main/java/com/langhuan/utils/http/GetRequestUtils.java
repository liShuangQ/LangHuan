package com.langhuan.utils.http;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

@Slf4j
public class GetRequestUtils {
    /**
     * 发送GET请求
     *
     * @param url 请求的URL
     * @return 响应的数据
     * @throws Exception 如果请求过程中发生异常
     */
    public static String sendGetRequest(String url) throws Exception {
        log.debug("Sending GET request to URL: " + url);
        URL obj = new URI(url).toURL();
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    /**
     * 发送GET请求
     *
     * @param url     请求的URL
     * @param headers 请求头
     * @return 响应的数据
     * @throws Exception 如果请求过程中发生异常
     */
    public static String sendGetRequest(String url, Map<String, String> headers) throws Exception {
        log.debug("Sending GET request to URL: " + url);
        log.debug("Request headers: " + headers);
        URL obj = new URI(url).toURL();
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");

        // 添加自定义请求头
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String responseLine = null;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }
}
