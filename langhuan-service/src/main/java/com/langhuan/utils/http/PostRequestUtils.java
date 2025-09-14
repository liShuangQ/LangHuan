package com.langhuan.utils.http;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class PostRequestUtils {
    /**
     * 发送POST请求（JSON格式）
     *
     * @param url      请求的URL
     * @param jsonData 请求的JSON数据
     * @return 响应的数据
     * @throws Exception 如果请求过程中发生异常
     */
    public static String sendPostRequest(String url, String jsonData) throws Exception {
        return sendPostRequest(url, jsonData, null);
    }

    /**
     * 发送POST请求（JSON格式）
     *
     * @param url      请求的URL
     * @param jsonData 请求的JSON数据
     * @param headers  请求头
     * @return 响应的数据
     * @throws Exception 如果请求过程中发生异常
     */
    public static String sendPostRequest(String url, String jsonData, Map<String, String> headers) throws Exception {
        log.debug("Sending POST request to URL: " + url);
//        log.debug("Request JSON data: " + jsonData);
//        log.debug("Request headers: " + headers);

        URL obj = new URI(url).toURL();
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        // 添加自定义请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                // 不覆盖Content-Type和Accept
                if (!"Content-Type".equalsIgnoreCase(entry.getKey()) &&
                        !"Accept".equalsIgnoreCase(entry.getKey())) {
                    con.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
        }

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return readResponse(con);
    }

    /**
     * 发送POST请求（form-data格式）
     *
     * @param url      请求的URL
     * @param formData 表单数据
     * @return 响应的数据
     * @throws Exception 如果请求过程中发生异常
     */
    public static String sendPostRequestWithFormData(String url, Map<String, Object> formData) throws Exception {
        return sendPostRequestWithFormData(url, formData, null);
    }

    /**
     * 发送POST请求（form-data格式）
     *
     * @param url      请求的URL
     * @param formData 表单数据（支持String和File类型）
     * @param headers  请求头
     * @return 响应的数据
     * @throws Exception 如果请求过程中发生异常
     */
    public static String sendPostRequestWithFormData(String url, Map<String, Object> formData,
            Map<String, String> headers) throws Exception {
        log.debug("Sending POST request with form-data to URL: " + url);
        log.debug("Form data keys: " + formData.keySet());

        String boundary = "----WebKitFormBoundary" + UUID.randomUUID().toString().replace("-", "");

        URL obj = new URI(url).toURL();
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        con.setDoOutput(true);
        con.setDoInput(true);

        // 添加自定义请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                // 不覆盖Content-Type
                if (!"Content-Type".equalsIgnoreCase(entry.getKey())) {
                    con.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
        }

        try (OutputStream os = con.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8), true)) {

            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                String fieldName = entry.getKey();
                Object fieldValue = entry.getValue();

                if (fieldValue instanceof File) {
                    // 处理文件上传
                    File file = (File) fieldValue;
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"").append(fieldName)
                            .append("\"; filename=\"").append(file.getName()).append("\"").append("\r\n");
                    writer.append("Content-Type: application/octet-stream").append("\r\n");
                    writer.append("\r\n");
                    writer.flush();

                    // 写入文件内容
                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        os.flush();
                    }
                    writer.append("\r\n");
                } else {
                    // 处理普通表单字段
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"")
                            .append("\r\n");
                    writer.append("\r\n");
                    writer.append(String.valueOf(fieldValue)).append("\r\n");
                }
            }

            // 结束边界
            writer.append("--").append(boundary).append("--").append("\r\n");
            writer.flush();
        }

        // 读取响应
        return readResponse(con);
    }

    /**
     * 发送POST请求（application/x-www-form-urlencoded格式）
     *
     * @param url      请求的URL
     * @param formData 表单数据
     * @return 响应的数据
     * @throws Exception 如果请求过程中发生异常
     */
    public static String sendPostRequestWithUrlEncoded(String url, Map<String, String> formData) throws Exception {
        return sendPostRequestWithUrlEncoded(url, formData, null);
    }

    /**
     * 发送POST请求（application/x-www-form-urlencoded格式）
     *
     * @param url      请求的URL
     * @param formData 表单数据
     * @param headers  请求头
     * @return 响应的数据
     * @throws Exception 如果请求过程中发生异常
     */
    public static String sendPostRequestWithUrlEncoded(String url, Map<String, String> formData,
            Map<String, String> headers) throws Exception {
        log.debug("Sending POST request with url-encoded data to URL: " + url);
        log.debug("Form data: " + formData);

        URL obj = new URI(url).toURL();
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        con.setDoOutput(true);

        // 添加自定义请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                // 不覆盖Content-Type
                if (!"Content-Type".equalsIgnoreCase(entry.getKey())) {
                    con.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
        }

        // 构建URL编码的表单数据
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (postData.length() != 0) {
                postData.append('&');
            }
            postData.append(java.net.URLEncoder.encode(entry.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(java.net.URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
        }

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = postData.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return readResponse(con);
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
