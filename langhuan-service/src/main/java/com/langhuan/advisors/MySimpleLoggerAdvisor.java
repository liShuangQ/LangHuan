package com.langhuan.advisors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;
import java.util.function.Function;

@Slf4j
public class MySimpleLoggerAdvisor implements CallAdvisor, StreamAdvisor {

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest,
			StreamAdvisorChain streamAdvisorChain) {
		this.logRequest(chatClientRequest);
		Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);
		return (new ChatClientMessageAggregator()).aggregateChatClientResponse(chatClientResponses, this::logResponse);
	}

	@Override
	public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
		this.logRequest(chatClientRequest);
		ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
		this.logResponse(chatClientResponse);
		return chatClientResponse;
	}

	private void logRequest(ChatClientRequest request) {
		log.debug("request: {}", request);
	}

	private void logResponse(ChatClientResponse chatClientResponse) {
		log.debug("response: {}", chatClientResponse.chatResponse());
	}

}