package com.langhuan.utils.chatMemory

import org.springframework.ai.chat.memory.ChatMemory
import org.springframework.ai.chat.messages.Message
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.util.Assert
import org.slf4j.LoggerFactory

class MyMessageWindowChatMemory(
	private val chatMemoryRepository: MyJdbcChatMemoryRepository,
	private val maxMessages: Int
) : ChatMemory {

	companion object {
		private val log = LoggerFactory.getLogger(MyMessageWindowChatMemory::class.java)
		private const val DEFAULT_MAX_MESSAGES = 20
	}

	init {
		Assert.notNull(chatMemoryRepository, "chatMemoryRepository cannot be null")
		Assert.isTrue(maxMessages > 0, "maxMessages must be greater than 0")
	}

	override fun add(conversationId: String, messages: List<Message>) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty")
		Assert.notNull(messages, "messages cannot be null")
		Assert.noNullElements(messages, "messages cannot contain null elements")

		val memoryMessages = chatMemoryRepository.findByConversationId(conversationId)
		val processedMessages = process(memoryMessages, messages)
		// this.chatMemoryRepository.saveAll(conversationId, processedMessages)
		chatMemoryRepository.add(conversationId, processedMessages)
	}

	override fun get(conversationId: String): List<Message> {
		Assert.hasText(conversationId, "conversationId cannot be null or empty")
		return chatMemoryRepository.findByConversationId(conversationId)
	}

	fun myGet(conversationId: String): List<Map<String, Any>> {
		Assert.hasText(conversationId, "conversationId cannot be null or empty")
		return chatMemoryRepository.myFindByConversationId(conversationId)
	}

	override fun clear(conversationId: String) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty")
		chatMemoryRepository.deleteByConversationId(conversationId)
	}

	private fun process(memoryMessages: List<Message>, newMessages: List<Message>): List<Message> {
		val processedMessages = mutableListOf<Message>()

		val memoryMessagesSet = memoryMessages.toSet()
		val hasNewSystemMessage = newMessages
			.filterIsInstance<SystemMessage>()
			.any { message -> !memoryMessagesSet.contains(message) }

		memoryMessages
			.filter { message -> !(hasNewSystemMessage && message is SystemMessage) }
			.forEach { processedMessages.add(it) }

		processedMessages.addAll(newMessages)

		if (processedMessages.size <= maxMessages) {
			return processedMessages
		}

		val messagesToRemove = processedMessages.size - maxMessages

		val trimmedMessages = mutableListOf<Message>()
		var removed = 0
		for (message in processedMessages) {
			if (message is SystemMessage || removed >= messagesToRemove) {
				trimmedMessages.add(message)
			} else {
				removed++
			}
		}

		return trimmedMessages
	}
}
