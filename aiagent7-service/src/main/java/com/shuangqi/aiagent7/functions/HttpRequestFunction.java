package com.shuangqi.aiagent7.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.shuangqi.aiagent7.utils.http.PostRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.context.annotation.Description;

import java.util.function.BiFunction;

@Slf4j
@Description("根据接口地址和请求参数请求，返回结果")
public class HttpRequestFunction implements BiFunction<HttpRequestFunction.Request, ToolContext, HttpRequestFunction.Response> {

    @Override
    public Response apply(Request request, ToolContext context) {
        log.debug("HttpRequestFunction request.url: {}", request.url);
        log.debug("HttpRequestFunction request.jsonData: {}", request.jsonData);
        try {
            return new Response(PostRequestUtils.sendPostRequest(request.url, request.jsonData));
        } catch (Exception e) {
            return new Response("请求失败");
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("匹配接口url和接口请求参数")
    public record Request(
            @JsonProperty(required = true, value = "url") @JsonPropertyDescription("接口url") String url,
            @JsonProperty(required = true, value = "jsonData") @JsonPropertyDescription("请求参数") String jsonData
    ) {
    }

    public record Response(String outData) {
    }

}
