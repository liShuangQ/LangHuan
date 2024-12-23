package com.shuangqi.aiagent7.functions;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.context.annotation.Description;

import java.util.function.BiFunction;

@Slf4j
@Description("地市有多少个姓名相同的人")
public class SearchLocationNameFunction implements BiFunction<SearchLocationNameFunction.Request, ToolContext, SearchLocationNameFunction.Response> {


    @Override
    public Response apply(Request request, ToolContext context) {
        log.debug("request.location: {}, request.name: {}", request.location(), request.name());
        log.debug("context: {}", context.getContext());
        return new Response("有10个。");
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonClassDescription("匹配地市和姓名信息")
    public record Request(
            @JsonProperty(required = true, value = "location") @JsonPropertyDescription("地市信息") String location,
            @JsonProperty(required = true, value = "name") @JsonPropertyDescription("姓名(名称)信息") String name
    ) {
    }


    public record Response(String num) {
    }
}