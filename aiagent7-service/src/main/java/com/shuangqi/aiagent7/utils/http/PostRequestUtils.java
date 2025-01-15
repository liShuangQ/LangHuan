package com.shuangqi.aiagent7.utils.http;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Slf4j
public class PostRequestUtils {
    /**
     * 发送POST请求
     *
     * @param url      请求的URL
     * @param jsonData 请求的JSON数据
     * @return 响应的数据
     * @throws Exception 如果请求过程中发生异常
     */
    public static String sendPostRequest(String url, String jsonData) throws Exception {
        log.debug("Sending POST request to URL: " + url);
        log.debug("Request JSON data: " + jsonData);
        return "请求-成功" + url + jsonData;
//        URL obj = new URL(url);
//        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//        con.setRequestMethod("POST");
//        con.setRequestProperty("Content-Type", "application/json; utf-8");
//        con.setRequestProperty("Accept", "application/json");
//        con.setDoOutput(true);
//
//        try (OutputStream os = con.getOutputStream()) {
//            byte[] input = jsonData.getBytes("utf-8");
//            os.write(input, 0, input.length);
//        }
//
//        try (BufferedReader br = new BufferedReader(
//                new InputStreamReader(con.getInputStream(), "utf-8"))) {
//            StringBuilder response = new StringBuilder();
//            String responseLine = null;
//            while ((responseLine = br.readLine()) != null) {
//                response.append(responseLine.trim());
//            }
//            return response.toString();
//        }
    }

    /**
     * 发送POST请求
     *
     * @param url      请求的URL
     * @param jsonData 请求的JSON数据
     * @param headers  请求头
     * @return 响应的数据
     * @throws Exception 如果请求过程中发生异常
     */
    public static String sendPostRequest(String url, String jsonData, Map<String, String> headers) throws Exception {
        log.debug("Sending POST request to URL: " + url);
        log.debug("Request JSON data: " + jsonData);
        log.debug("Request headers: " + headers);
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        // 添加自定义请求头
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonData.getBytes("utf-8");
            os.write(input, 0, input.length);
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
