package com.shuangqi.aiagent7.advisors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
    private static final String DEFAULT_USER_TEXT_ADVISE = """
            {re2_input_query}
            Read the question again: {re2_input_query}
            """;

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private AdvisedRequest before(AdvisedRequest advisedRequest) {

        String inputQuery = advisedRequest.userText(); //original user query

        Map<String, Object> params = new HashMap<>(advisedRequest.userParams());
        params.put("re2_input_query", inputQuery);

        log.debug("re2_input_query:" + inputQuery);

        return AdvisedRequest.from(advisedRequest)
                .withUserText(DEFAULT_USER_TEXT_ADVISE)
                .withUserParams(params)
                .build();
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(before(advisedRequest));
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(before(advisedRequest));
    }
}