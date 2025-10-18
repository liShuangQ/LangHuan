package com.langhuan.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.langhuan.common.Constant
import com.langhuan.model.domain.TPrompts
import com.langhuan.model.mapper.TPromptsMapper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * @author lishuangqi
 * @description 针对表【t_prompts】的数据库操作Service实现
 * @createDate 2025-03-05 09:40:43
 */
@Service
class TPromptsService : ServiceImpl<TPromptsMapper, TPrompts>() {

    companion object {
        private val log = LoggerFactory.getLogger(TPromptsService::class.java)

        // 使用线程安全的来存储缓存的数据
        private val cachedPrompts = ConcurrentHashMap<String, String>()

        fun getCachedTPromptsByMethodName(method_name: String): String? {
            val content = cachedPrompts[method_name]
            if (content.isNullOrEmpty()) {
                log.error("No valid content found for method name {}", method_name)
                return null
            }
            return content
        }
    }

    @Scheduled(fixedRate = Constant.AIDEFAULTSYSTEMPROMPTRECTIME.toLong())
    fun cacheTPrompts() {
        val prompts = super.list()
        val newCache = ConcurrentHashMap<String, String>()
        for (prompt in prompts) {
            newCache[prompt.methodName as String] = prompt.content as String
        }
        synchronized(cachedPrompts) {
            cachedPrompts.clear()
            cachedPrompts.putAll(newCache)
        }
        TPromptsService.log.info("Cached t_prompts table data at {}", java.util.Date())
    }

    // 新增
    fun saveTPrompts(tPrompts: TPrompts): Boolean {
        return super.save(tPrompts)
    }

    // 删除
    fun removeTPromptsById(id: Int): Boolean {
        return super.removeById(id)
    }

    // 修改
    fun updateTPrompts(tPrompts: TPrompts): Boolean {
        return super.updateById(tPrompts)
    }

    // 分页查询并支持模糊搜索
    fun getTPromptsByPage(
        pageNum: Int,
        pageSize: Int,
        methodName: String,
        category: String,
        description: String
    ): IPage<TPrompts> {
        return super.page(
            Page(pageNum.toLong(), pageSize.toLong()),
            QueryWrapper<TPrompts>()
                .like(methodName.isNotEmpty(), "method_name", methodName)
                .like(category.isNotEmpty(), "category", category)
                .like(description.isNotEmpty(), "description", description)
        )
    }
}
