package com.langhuan.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepositoryDialect;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

/**
 * 自定义的 JdbcChatMemoryRepository 实现，用于存储和检索聊天消息。
 * 弃用！！！ 转为更简单的直接在内存服务查询sql获取
 */
public final class MyJdbcChatMemoryRepository implements ChatMemoryRepository {

	private final JdbcTemplate jdbcTemplate;

	private final TransactionTemplate transactionTemplate;

	private final JdbcChatMemoryRepositoryDialect dialect;

	public MyJdbcChatMemoryRepository(JdbcTemplate jdbcTemplate, JdbcChatMemoryRepositoryDialect dialect,
			PlatformTransactionManager txManager) {
		Assert.notNull(jdbcTemplate, "jdbcTemplate cannot be null");
		Assert.notNull(dialect, "dialect cannot be null");
		this.jdbcTemplate = jdbcTemplate;
		this.dialect = dialect;
		this.transactionTemplate = new TransactionTemplate(
				txManager != null ? txManager : new DataSourceTransactionManager(jdbcTemplate.getDataSource()));
	}

	public MyJdbcChatMemoryRepository(DataSource dataSource, JdbcChatMemoryRepositoryDialect dialect) {
		this(new JdbcTemplate(dataSource), dialect, new DataSourceTransactionManager(dataSource));
	}

	@Override
	public List<String> findConversationIds() {
		return this.jdbcTemplate.queryForList(dialect.getSelectConversationIdsSql(), String.class);
	}

	@Override
	public List<Message> findByConversationId(String conversationId) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		return this.jdbcTemplate.query(this.dialect.getSelectMessagesSql(), new CustomMessageRowMapper(), conversationId);
	}

	@Override
	public void saveAll(String conversationId, List<Message> messages) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		Assert.notNull(messages, "messages cannot be null");
		Assert.noNullElements(messages, "messages cannot contain null elements");

		this.transactionTemplate.execute(status -> {
			deleteByConversationId(conversationId);
			// Note: You might also want to customize the saveAll implementation
			// For now, we're focusing on the findByConversationId method
			return null;
		});
	}

	@Override
	public void deleteByConversationId(String conversationId) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		this.jdbcTemplate.update(this.dialect.getDeleteMessagesSql(), conversationId);
	}

	/**
	 * 自定义的 MessageRowMapper 实现，可以根据需要修改消息映射逻辑
	 */
	private static class CustomMessageRowMapper implements RowMapper<Message> {

		@Override
		@Nullable
		public Message mapRow(ResultSet rs, int i) throws SQLException {
			var content = rs.getString(1);
			var type = MessageType.valueOf(rs.getString(2));

			return switch (type) {
				case USER -> new UserMessage(content);
				case ASSISTANT -> new AssistantMessage(content);
				case SYSTEM -> new SystemMessage(content);
				case TOOL -> new ToolResponseMessage(List.of());
			};
		}
	}

	// 如果需要，可以添加 Builder 模式来简化创建过程
	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {

		private JdbcTemplate jdbcTemplate;

		private JdbcChatMemoryRepositoryDialect dialect;

		private DataSource dataSource;

		private PlatformTransactionManager platformTransactionManager;

		private Builder() {
		}

		public Builder jdbcTemplate(JdbcTemplate jdbcTemplate) {
			this.jdbcTemplate = jdbcTemplate;
			return this;
		}

		public Builder dialect(JdbcChatMemoryRepositoryDialect dialect) {
			this.dialect = dialect;
			return this;
		}

		public Builder dataSource(DataSource dataSource) {
			this.dataSource = dataSource;
			return this;
		}

		public Builder transactionManager(PlatformTransactionManager txManager) {
			this.platformTransactionManager = txManager;
			return this;
		}

		public MyJdbcChatMemoryRepository build() {
			DataSource effectiveDataSource = resolveDataSource();
			JdbcChatMemoryRepositoryDialect effectiveDialect = resolveDialect(effectiveDataSource);
			return new MyJdbcChatMemoryRepository(resolveJdbcTemplate(), effectiveDialect,
					this.platformTransactionManager);
		}

		private JdbcTemplate resolveJdbcTemplate() {
			if (this.jdbcTemplate != null) {
				return this.jdbcTemplate;
			}
			DataSource dataSource = resolveDataSource();
			if (dataSource != null) {
				return new JdbcTemplate(dataSource);
			}
			throw new IllegalArgumentException("DataSource must be set (either via dataSource() or jdbcTemplate())");
		}

		private DataSource resolveDataSource() {
			if (this.dataSource != null) {
				return this.dataSource;
			}
			if (this.jdbcTemplate != null && this.jdbcTemplate.getDataSource() != null) {
				return this.jdbcTemplate.getDataSource();
			}
			throw new IllegalArgumentException("DataSource must be set (either via dataSource() or jdbcTemplate())");
		}

		private JdbcChatMemoryRepositoryDialect resolveDialect(DataSource dataSource) {
			if (this.dialect == null) {
				throw new IllegalArgumentException("Dialect must be set");
			}
			return this.dialect;
		}
	}
}
