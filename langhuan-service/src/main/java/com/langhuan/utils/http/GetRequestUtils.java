package com.langhuan.utils.http;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * GET请求工具类
 * 提供发送GET请求的各种方法，支持请求头和URL参数
 *
 * @author LangHuan
 */
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
        return sendGetRequest(url, null, null);
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
        return sendGetRequest(url, null, headers);
    }

    /**
     * 发送GET请求
     *
     * @param url    请求的URL
     * @param params URL参数
     * @return 响应的数据
     * @throws Exception 如果请求过程中发生异常
     */
    public static String sendGetRequestWithParams(String url, Map<String, String> params) throws Exception {
        return sendGetRequest(url, params, null);
    }

    /**
     * 发送GET请求
     *
     * @param url     请求的URL
     * @param params  URL参数
     * @param headers 请求头
     * @return 响应的数据
     * @throws Exception 如果请求过程中发生异常
     */
    public static String sendGetRequest(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
        log.debug("Sending GET request to URL: " + url);
        log.debug("Request params: " + params);
        log.debug("Request headers: " + headers);

        // 构建完整的URL（包含参数）
        String fullUrl = buildUrlWithParams(url, params);
        log.debug("Full URL with params: " + fullUrl);

        URL obj = new URI(fullUrl).toURL();
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("User-Agent", "Java-HttpURLConnection/1.0");

        // 添加自定义请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        return readResponse(con);
    }

    /**
     * 构建带参数的URL
     *
     * @param url    基础URL
     * @param params URL参数
     * @return 完整的URL
     * @throws Exception 如果URL编码过程中发生异常
     */
    private static String buildUrlWithParams(String url, Map<String, String> params) throws Exception {
        if (params == null || params.isEmpty()) {
            return url;
        }

        StringBuilder urlBuilder = new StringBuilder(url);
        
        // 检查URL是否已经包含参数
        if (url.contains("?")) {
            urlBuilder.append("&");
        } else {
            urlBuilder.append("?");
        }

        // 添加参数
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                urlBuilder.append("&");
            }
            urlBuilder.append(java.net.URLEncoder.encode(entry.getKey(), "UTF-8"))
                     .append("=")
                     .append(java.net.URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
            first = false;
        }

        return urlBuilder.toString();
    }

    /**
     * 读取HTTP响应
     *
     * @param con HTTP连接
     * @return 响应内容
     * @throws Exception 如果读取过程中发生异常
     */
    private static String readResponse(HttpURLConnection con) throws Exception {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        } catch (Exception e) {
            log.error("Error reading response: " + e.getMessage());
            // 尝试读取错误流
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder errorResponse = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    errorResponse.append(responseLine.trim());
                }
                log.error("Error response: " + errorResponse.toString());
            } catch (Exception ignored) {
                // 忽略读取错误流时的异常
            }
            throw e;
        }
    }
}
