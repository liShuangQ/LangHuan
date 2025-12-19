package com.langhuan.utils.chatMemory

import org.springframework.ai.chat.memory.ChatMemoryRepository
import org.springframework.ai.chat.messages.*
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.lang.Nullable
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.util.Assert
import org.slf4j.LoggerFactory
import java.lang.reflect.Array
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.time.Instant
import java.util.Objects
import java.util.concurrent.atomic.AtomicLong

class MyJdbcChatMemoryRepository private constructor(
	private val jdbcTemplate: JdbcTemplate,
	private val transactionTemplate: TransactionTemplate
) : ChatMemoryRepository {

	companion object {
		private val log = LoggerFactory.getLogger(MyJdbcChatMemoryRepository::class.java)
		
		/**
		 * 静态工厂方法，用于创建MyJdbcChatMemoryRepository实例
		 *
		 * @param jdbcTemplate JdbcTemplate实例
		 * @param txManager 事务管理器
		 * @return MyJdbcChatMemoryRepository实例
		 */
		fun create(jdbcTemplate: JdbcTemplate, txManager: PlatformTransactionManager?): MyJdbcChatMemoryRepository {
			Assert.notNull(jdbcTemplate, "jdbcTemplate cannot be null")
			val transactionManager = txManager ?: DataSourceTransactionManager(jdbcTemplate.dataSource!!)
			return MyJdbcChatMemoryRepository(jdbcTemplate, TransactionTemplate(transactionManager))
		}
	}

	init {
		Assert.notNull(jdbcTemplate, "jdbcTemplate cannot be null")
	}

	private fun getSelectMessagesSql(): String {
		return "SELECT content, type, timestamp FROM SPRING_AI_CHAT_MEMORY WHERE conversation_id = ? ORDER BY \"timestamp\""
	}

	private fun getInsertMessageSql(): String {
		return "INSERT INTO SPRING_AI_CHAT_MEMORY (conversation_id, content, type, \"timestamp\") VALUES (?, ?, ?, ?)"
	}

	private fun getSelectConversationIdsSql(): String {
		return "SELECT DISTINCT conversation_id FROM SPRING_AI_CHAT_MEMORY"
	}

	private fun getDeleteMessagesSql(): String {
		return "DELETE FROM SPRING_AI_CHAT_MEMORY WHERE conversation_id = ?"
	}

	override fun findConversationIds(): List<String> {
		return jdbcTemplate.queryForList(getSelectConversationIdsSql(), String::class.java)
	}

	override fun findByConversationId(conversationId: String): List<Message> {
		Assert.hasText(conversationId, "conversationId cannot be null or empty")
		return jdbcTemplate.query(getSelectMessagesSql(), MessageRowMapper(), conversationId)
	}

	fun myFindByConversationId(conversationId: String): List<Map<String, Any>> {
		Assert.hasText(conversationId, "conversationId cannot be null or empty")
		return jdbcTemplate.query(getSelectMessagesSql(), MyMessageRowMapper(), conversationId)
	}

	/**
	 * 新增，对应MyMessageWindowChatMemory的add方法
	 * 
	 * @param conversationId
	 * @param messages
	 */
	fun add(conversationId: String, messages: List<Message>) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty")
		Assert.notNull(messages, "messages cannot be null")
		Assert.noNullElements(messages, "messages cannot contain null elements")

		// 只添加最新的对话，不用之前的全部删除在添加的方式
		transactionTemplate.execute<Void> {
			jdbcTemplate.batchUpdate(getInsertMessageSql(),
				AddBatchPreparedStatement(conversationId, listOf(messages.last())))
			null
		}
	}

	override fun saveAll(conversationId: String, messages: List<Message>) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty")
		Assert.notNull(messages, "messages cannot be null")
		Assert.noNullElements(messages, "messages cannot contain null elements")

		transactionTemplate.execute<Void> {
			deleteByConversationId(conversationId)
			jdbcTemplate.batchUpdate(getInsertMessageSql(),
				AddBatchPreparedStatement(conversationId, messages))
			null
		}
	}

	override fun deleteByConversationId(conversationId: String) {
		Assert.hasText(conversationId, "conversationId cannot be null or empty")
		jdbcTemplate.update(getDeleteMessagesSql(), conversationId)
	}

	private class AddBatchPreparedStatement(
		private val conversationId: String,
		private val messages: List<Message>,
		private val instantSeq: AtomicLong
	) : BatchPreparedStatementSetter {

		constructor(conversationId: String, messages: List<Message>) : this(
			conversationId, 
			messages, 
			AtomicLong(Instant.now().toEpochMilli())
		)

		override fun setValues(ps: PreparedStatement, i: Int) {
			val message = messages[i]

			ps.setString(1, conversationId)
			ps.setString(2, message.text)
			ps.setString(3, message.messageType.name)
			ps.setTimestamp(4, Timestamp(instantSeq.getAndIncrement()))
		}

		override fun getBatchSize(): Int = messages.size
	}

	private class MessageRowMapper : RowMapper<Message> {

		@Nullable
		override fun mapRow(rs: ResultSet, i: Int): Message? {
			val content = rs.getString(1)
			val type = MessageType.valueOf(rs.getString(2))

			return when (type) {
				MessageType.USER -> UserMessage(content)
				MessageType.ASSISTANT -> AssistantMessage(content)
				MessageType.SYSTEM -> SystemMessage(content)
				// The content is always stored empty for ToolResponseMessages.
				// If we want to capture the actual content, we need to extend
				// AddBatchPreparedStatement to support it.
//				MessageType.TOOL -> ToolResponseMessage(emptyList(), emptyMap<String, Any>())
				MessageType.TOOL -> null
			}
		}
	}

	/**
	 * 见 JdbcChatMemoryRepository 提供的MessageRowMapper。
	 * 自定义回复参数，这时候已经改变了返回格式，注意类型为SYSTEM和TOOL的类型需要特殊处理（当前因为没需求，数据库不存储这种类型，所以不处理）
	 */
	private class MyMessageRowMapper : RowMapper<Map<String, Any>> {

		@Nullable
		override fun mapRow(rs: ResultSet, i: Int): Map<String, Any>? {
			val content = rs.getString(1)
			val time = rs.getString(3)
			return mapOf(
				"messageType" to rs.getString(2),
				"media" to Array.newInstance(Any::class.java, 0),
				"text" to content,
				"time" to time // 将时间属性添加到结果中
			)
		}
	}
}
