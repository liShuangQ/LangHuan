package com.langhuan.functionTools;

import com.langhuan.utils.http.GetRequestUtils;
import com.langhuan.utils.http.PostRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

@Slf4j
public class RestRequestTools {

    @Tool(description = "get方法请求rest接口", returnDirect = true)
    public static String sendGetRequest(@ToolParam(description = "接口请求地址url") String url) throws Exception {
        log.info("调用工具：" + "sendGetRequest");
        String result = "";
        try {
            result = GetRequestUtils.sendGetRequest(url);
        } catch (Exception e) {
            log.error("sendGetRequest error:{}", e);
            throw e;
        }
        return result;
    }

    @Tool(description = "post方法请求rest接口", returnDirect = true)
    public static String sendPostRequest(
            @ToolParam(description = "接口请求地址url") String url,
            @ToolParam(description = "接口请求携带数据体") String jsonData
    ) throws Exception {
        log.info("调用工具：" + "sendPostRequest");
        String result = "";
        try {
            result = PostRequestUtils.sendPostRequest(url, jsonData);
        } catch (Exception e) {
            log.error("sendPostRequest error:{}", e);
            throw e;
        }
        return result;
    }
}
