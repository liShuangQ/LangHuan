package com.langhuan.config;

import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepositoryDialect;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyPostgresChatMemoryRepositoryDialect implements JdbcChatMemoryRepositoryDialect {

	@Override
	public String getSelectMessagesSql() {
		return "SELECT content, type, timestamp FROM SPRING_AI_CHAT_MEMORY WHERE conversation_id = ? ORDER BY \"timestamp\"";
	}

	@Override
	public String getInsertMessageSql() {
		return "INSERT INTO SPRING_AI_CHAT_MEMORY (conversation_id, content, type, \"timestamp\") VALUES (?, ?, ?, ?)";
	}

	@Override
	public String getSelectConversationIdsSql() {
		return "SELECT DISTINCT conversation_id FROM SPRING_AI_CHAT_MEMORY";
	}

	@Override
	public String getDeleteMessagesSql() {
		return "DELETE FROM SPRING_AI_CHAT_MEMORY WHERE conversation_id = ?";
	}

}