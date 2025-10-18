package com.langhuan.advisors

import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClientMessageAggregator
import org.springframework.ai.chat.client.ChatClientRequest
import org.springframework.ai.chat.client.ChatClientResponse
import org.springframework.ai.chat.client.advisor.api.CallAdvisor
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain
import reactor.core.publisher.Flux

class MySimpleLoggerAdvisor : CallAdvisor, StreamAdvisor {

    override fun getName(): String {
        return this::class.java.simpleName
    }

    override fun getOrder(): Int {
        return 0
    }

    override fun adviseStream(
        chatClientRequest: ChatClientRequest,
        streamAdvisorChain: StreamAdvisorChain
    ): Flux<ChatClientResponse> {
        logRequest(chatClientRequest)
        val chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest)
        return ChatClientMessageAggregator().aggregateChatClientResponse(chatClientResponses, this::logResponse)
    }

    override fun adviseCall(
        chatClientRequest: ChatClientRequest,
        callAdvisorChain: CallAdvisorChain
    ): ChatClientResponse {
        logRequest(chatClientRequest)
        val chatClientResponse = callAdvisorChain.nextCall(chatClientRequest)
        logResponse(chatClientResponse)
        return chatClientResponse
    }

    private fun logRequest(request: ChatClientRequest) {
        log.debug("request: {}", request)
    }

    private fun logResponse(chatClientResponse: ChatClientResponse) {
        log.debug("response: {}", chatClientResponse.chatResponse())
    }

    companion object {
        private val log = LoggerFactory.getLogger(MySimpleLoggerAdvisor::class.java)
    }
}
