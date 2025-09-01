package com.langhuan.config;

import java.lang.reflect.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepositoryDialect;
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

import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of {@link ChatMemoryRepository} for JDBC.
 *
 * @author Jonathan Leijendekker
 * @author Thomas Vitale
 * @author Linar Abzaltdinov
 * @author Mark Pollack
 * @author Yanming Zhou
 * @since 1.0.0
 */
@Slf4j
public final class MyJdbcChatMemoryRepository implements ChatMemoryRepository {

	private final JdbcTemplate jdbcTemplate;

	private final TransactionTemplate transactionTemplate;

	private final JdbcChatMemoryRepositoryDialect dialect;

	private MyJdbcChatMemoryRepository(JdbcTemplate jdbcTemplate, JdbcChatMemoryRepositoryDialect dialect,
			PlatformTransactionManager txManager) {
		Assert.notNull(jdbcTemplate, "jdbcTemplate cannot be null");
		Assert.notNull(dialect, "dialect cannot be null");
		this.jdbcTemplate = jdbcTemplate;
		this.dialect = dialect;
		this.transactionTemplate = new TransactionTemplate(
				txManager != null ? txManager : new DataSourceTransactionManager(jdbcTemplate.getDataSource()));
	}

	@Override
	public List<String> findConversationIds() {
		return this.jdbcTemplate.queryForList(dialect.getSelectConversationIdsSql(), String.class);
	}

	@Override
	public List<Message> findByConversationId(String conversationId) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		return this.jdbcTemplate.query(this.dialect.getSelectMessagesSql(), new MessageRowMapper(), conversationId);
	}

	/**
	 * 新增，对应MyMessageWindowChatMemory的add方法
	 * @param conversationId
	 * @param messages
	 */
	public void add(String conversationId, List<Message> messages) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		Assert.notNull(messages, "messages cannot be null");
		Assert.noNullElements(messages, "messages cannot contain null elements");

		// 只添加最新的对话，不用之前的全部删除在添加的方式
		this.transactionTemplate.execute(status -> {
			this.jdbcTemplate.batchUpdate(this.dialect.getInsertMessageSql(),
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
			this.jdbcTemplate.batchUpdate(this.dialect.getInsertMessageSql(),
					new AddBatchPreparedStatement(conversationId, messages));
			return null;
		});

	}

	@Override
	public void deleteByConversationId(String conversationId) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty");
		this.jdbcTemplate.update(this.dialect.getDeleteMessagesSql(), conversationId);
	}

	public static Builder builder() {
		return new Builder();
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

	public static final class Builder {

		private JdbcTemplate jdbcTemplate;

		private JdbcChatMemoryRepositoryDialect dialect;

		private DataSource dataSource;

		private PlatformTransactionManager platformTransactionManager;

		private static final Logger logger = LoggerFactory.getLogger(Builder.class);

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
			if (this.dataSource != null) {
				return new JdbcTemplate(this.dataSource);
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
				try {
					return JdbcChatMemoryRepositoryDialect.from(dataSource);
				} catch (Exception ex) {
					throw new IllegalStateException("Could not detect dialect from datasource", ex);
				}
			} else {
				warnIfDialectMismatch(dataSource, this.dialect);
				return this.dialect;
			}
		}

		/**
		 * Logs a warning if the explicitly set dialect differs from the dialect
		 * detected
		 * from the DataSource.
		 */
		private void warnIfDialectMismatch(DataSource dataSource, JdbcChatMemoryRepositoryDialect explicitDialect) {
			try {
				JdbcChatMemoryRepositoryDialect detected = JdbcChatMemoryRepositoryDialect.from(dataSource);
				if (!detected.getClass().equals(explicitDialect.getClass())) {
					logger.warn("Explicitly set dialect {} will be used instead of detected dialect {} from datasource",
							explicitDialect.getClass().getSimpleName(), detected.getClass().getSimpleName());
				}
			} catch (Exception ex) {
				logger.debug("Could not detect dialect from datasource", ex);
			}
		}

	}

}
