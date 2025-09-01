package com.langhuan.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MyMessageWindowChatMemory implements ChatMemory {

	private static final int DEFAULT_MAX_MESSAGES = 20;

	private final MyJdbcChatMemoryRepository chatMemoryRepository;

	private final int maxMessages;

	private MyMessageWindowChatMemory(MyJdbcChatMemoryRepository chatMemoryRepository, int maxMessages) {
		Assert.notNull(chatMemoryRepository, "chatMemoryRepository cannot be null");
		Assert.isTrue(maxMessages > 0, "maxMessages must be greater than 0");
		this.chatMemoryRepository = chatMemoryRepository;
		this.maxMessages = maxMessages;
	}

	@Override
	public void add(String conversationId, List<Message> messages) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		Assert.notNull(messages, "messages cannot be null");
		Assert.noNullElements(messages, "messages cannot contain null elements");

		List<Message> memoryMessages = this.chatMemoryRepository.findByConversationId(conversationId);
		List<Message> processedMessages = process(memoryMessages, messages);
		// this.chatMemoryRepository.saveAll(conversationId, processedMessages);
		this.chatMemoryRepository.add(conversationId, processedMessages);
	}

	@Override
	public List<Message> get(String conversationId) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		return this.chatMemoryRepository.findByConversationId(conversationId);
	}

	@Override
	public void clear(String conversationId) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		this.chatMemoryRepository.deleteByConversationId(conversationId);
	}

	private List<Message> process(List<Message> memoryMessages, List<Message> newMessages) {
		List<Message> processedMessages = new ArrayList<>();

		Set<Message> memoryMessagesSet = new HashSet<>(memoryMessages);
		boolean hasNewSystemMessage = newMessages.stream()
				.filter(SystemMessage.class::isInstance)
				.anyMatch(message -> !memoryMessagesSet.contains(message));

		memoryMessages.stream()
				.filter(message -> !(hasNewSystemMessage && message instanceof SystemMessage))
				.forEach(processedMessages::add);

		processedMessages.addAll(newMessages);

		if (processedMessages.size() <= this.maxMessages) {
			return processedMessages;
		}

		int messagesToRemove = processedMessages.size() - this.maxMessages;

		List<Message> trimmedMessages = new ArrayList<>();
		int removed = 0;
		for (Message message : processedMessages) {
			if (message instanceof SystemMessage || removed >= messagesToRemove) {
				trimmedMessages.add(message);
			} else {
				removed++;
			}
		}

		return trimmedMessages;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private MyJdbcChatMemoryRepository chatMemoryRepository;

		private int maxMessages = DEFAULT_MAX_MESSAGES;

		private Builder() {
		}

		public Builder chatMemoryRepository(MyJdbcChatMemoryRepository chatMemoryRepository) {
			this.chatMemoryRepository = chatMemoryRepository;
			return this;
		}

		public Builder maxMessages(int maxMessages) {
			this.maxMessages = maxMessages;
			return this;
		}

		public MyMessageWindowChatMemory build() {
			if (this.chatMemoryRepository == null) {
				log.error("自定义MyMessageWindowChatMemory，必须指定chatMemoryRepository仓库。");
				throw new IllegalArgumentException("chatMemoryRepository cannot be null");
				// this.chatMemoryRepository = new InMemoryChatMemoryRepository();
			}
			return new MyMessageWindowChatMemory(this.chatMemoryRepository, this.maxMessages);
		}

	}

}
