package com.langhuan.utils.chatMemory;

import java.lang.reflect.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class MyJdbcChatMemoryRepository implements ChatMemoryRepository {

	private final JdbcTemplate jdbcTemplate;

	private final TransactionTemplate transactionTemplate;

	private MyJdbcChatMemoryRepository(JdbcTemplate jdbcTemplate, PlatformTransactionManager txManager) {
		Assert.notNull(jdbcTemplate, "jdbcTemplate cannot be null");
		this.jdbcTemplate = jdbcTemplate;
		this.transactionTemplate = new TransactionTemplate(
				txManager != null ? txManager : new DataSourceTransactionManager(jdbcTemplate.getDataSource()));
	}
	
	private String getSelectMessagesSql() {
		return "SELECT content, type, timestamp FROM SPRING_AI_CHAT_MEMORY WHERE conversation_id = ? ORDER BY \"timestamp\"";
	}

	private String getInsertMessageSql() {
		return "INSERT INTO SPRING_AI_CHAT_MEMORY (conversation_id, content, type, \"timestamp\") VALUES (?, ?, ?, ?)";
	}

	private String getSelectConversationIdsSql() {
		return "SELECT DISTINCT conversation_id FROM SPRING_AI_CHAT_MEMORY";
	}

	private String getDeleteMessagesSql() {
		return "DELETE FROM SPRING_AI_CHAT_MEMORY WHERE conversation_id = ?";
	}

	/**
	 * 静态工厂方法，用于创建MyJdbcChatMemoryRepository实例
	 *
	 * @param jdbcTemplate JdbcTemplate实例
	 * @param txManager 事务管理器
	 * @return MyJdbcChatMemoryRepository实例
	 */
	public static MyJdbcChatMemoryRepository create(JdbcTemplate jdbcTemplate,
			PlatformTransactionManager txManager) {
		return new MyJdbcChatMemoryRepository(jdbcTemplate, txManager);
	}

	@Override
	public List<String> findConversationIds() {
		return this.jdbcTemplate.queryForList(getSelectConversationIdsSql(), String.class);
	}

	@Override
	public List<Message> findByConversationId(String conversationId) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		return this.jdbcTemplate.query(this.getSelectMessagesSql(), new MessageRowMapper(), conversationId);
	}

	public List<Map<String, Object>> myFindByConversationId(String conversationId) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		return this.jdbcTemplate.query(
				this.getSelectMessagesSql(),
				new MyMessageRowMapper(), conversationId);
	}

	/**
	 * 新增，对应MyMessageWindowChatMemory的add方法
	 * 
	 * @param conversationId
	 * @param messages
	 */
	public void add(String conversationId, List<Message> messages) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		Assert.notNull(messages, "messages cannot be null");
		Assert.noNullElements(messages, "messages cannot contain null elements");

		// 只添加最新的对话，不用之前的全部删除在添加的方式
		this.transactionTemplate.execute(status -> {
			this.jdbcTemplate.batchUpdate(this.getInsertMessageSql(),
					new AddBatchPreparedStatement(conversationId, List.of(messages.getLast())));
			return null;
		});
	}

	@Override
	public void saveAll(String conversationId, List<Message> messages) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		Assert.notNull(messages, "messages cannot be null");
		Assert.noNullElements(messages, "messages cannot contain null elements");

		this.transactionTemplate.execute(status -> {
			deleteByConversationId(conversationId);
			this.jdbcTemplate.batchUpdate(this.getInsertMessageSql(),
					new AddBatchPreparedStatement(conversationId, messages));
			return null;
		});

	}

	@Override
	public void deleteByConversationId(String conversationId) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		this.jdbcTemplate.update(this.getDeleteMessagesSql(), conversationId);
	}

	private record AddBatchPreparedStatement(String conversationId, List<Message> messages,
			AtomicLong instantSeq) implements BatchPreparedStatementSetter {

		private AddBatchPreparedStatement(String conversationId, List<Message> messages) {
			this(conversationId, messages, new AtomicLong(Instant.now().toEpochMilli()));
		}

		@Override
		public void setValues(PreparedStatement ps, int i) throws SQLException {
			var message = this.messages.get(i);

			ps.setString(1, this.conversationId);
			ps.setString(2, message.getText());
			ps.setString(3, message.getMessageType().name());
			ps.setTimestamp(4, new Timestamp(this.instantSeq.getAndIncrement()));
		}

		@Override
		public int getBatchSize() {
			return this.messages.size();
		}
	}

	private static class MessageRowMapper implements RowMapper<Message> {

		@Override
		@Nullable
		public Message mapRow(ResultSet rs, int i) throws SQLException {
			var content = rs.getString(1);
			var type = MessageType.valueOf(rs.getString(2));

			return switch (type) {
				case USER -> new UserMessage(content);
				case ASSISTANT -> new AssistantMessage(content);
				case SYSTEM -> new SystemMessage(content);
				// The content is always stored empty for ToolResponseMessages.
				// If we want to capture the actual content, we need to extend
				// AddBatchPreparedStatement to support it.
				case TOOL -> new ToolResponseMessage(List.of());
			};
		}

	}

	/**
	 * 见 JdbcChatMemoryRepository 提供的MessageRowMapper。
	 * 自定义回复参数，这时候已经改变了返回格式，注意类型为SYSTEM和TOOL的类型需要特殊处理（当前因为没需求，数据库不存储这种类型，所以不处理）
	 */
	private static class MyMessageRowMapper implements RowMapper<Map<String, Object>> {

		@Override
		@Nullable
		public Map<String, Object> mapRow(ResultSet rs, int i) throws SQLException {

			var content = rs.getString(1);
			var time = rs.getString(3);
			return Map.of(
					"messageType", rs.getString(2),
					"media", Array.newInstance(Object.class, 0),
					"text", content,
					"time", time // 将时间属性添加到结果中
			);
		}

	}
}
