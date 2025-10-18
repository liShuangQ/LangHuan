package com.langhuan.controller

import com.langhuan.common.Constant
import com.langhuan.common.Result
import com.langhuan.model.domain.TPrompts
import com.langhuan.service.TPromptsService
import com.langhuan.serviceai.ChatService
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/prompts")
class PromptsController(
    private val tPromptsService: TPromptsService,
    private val chatService: ChatService
) {

    companion object {
        private val log = LoggerFactory.getLogger(PromptsController::class.java)
    }

    // 提示词分类的枚举
    // NOTE：这里后续可能改动很小，直接在程序中定义死即可
    @PostMapping("/usePrompt/getCategoryEnum")
    fun getCategoryEnum(@RequestBody tPrompts: TPrompts): Result<*> {
        return Result.success(Constant.CATEGORYENUM)
    }

    // 新增
    @PreAuthorize("hasAuthority('/prompts/usePrompt/add')")
    @PostMapping("/usePrompt/add")
    fun saveTPrompts(@RequestBody tPrompts: TPrompts): Result<String> {
        val b = tPromptsService.saveTPrompts(tPrompts)
        return if (b) Result.success("操作成功") else Result.error("操作失败")
    }

    // 删除
    @PreAuthorize("hasAuthority('/prompts/usePrompt/delete')")
    @PostMapping("/usePrompt/delete")
    fun removeTPromptsById(@RequestParam id: Int): Result<String> {
        val b = tPromptsService.removeTPromptsById(id)
        return if (b) Result.success("操作成功") else Result.error("操作失败")
    }

    // 修改
    @PreAuthorize("hasAuthority('/prompts/usePrompt/update')")
    @PostMapping("/usePrompt/update")
    fun updateTPrompts(@RequestBody tPrompts: TPrompts): Result<String> {
        val b = tPromptsService.updateTPrompts(tPrompts)
        return if (b) Result.success("操作成功") else Result.error("操作失败")
    }

    // 刷新提示词
    @PreAuthorize("hasAuthority('/prompts/usePrompt/refresh')")
    @PostMapping("/usePrompt/refresh")
    fun refreshTPrompts(): Result<*> {
        tPromptsService.cacheTPrompts()
        return Result.success("生效成功")
    }

    // 分页查询并支持模糊搜索
    @PreAuthorize("hasAuthority('/prompts/usePrompt/query')")
    @PostMapping("/usePrompt/query")
    fun getTPromptsByPage(
        @RequestParam(defaultValue = "1") pageNum: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(required = false) methodName: String?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) description: String?
    ): Result<Any> {
        return Result.success(tPromptsService.getTPromptsByPage(pageNum, pageSize, methodName as String, category as String, description as String))
    }
}
