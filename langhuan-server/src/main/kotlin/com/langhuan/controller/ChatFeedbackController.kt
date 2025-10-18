package com.langhuan.controller

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper
import com.langhuan.common.Result
import com.langhuan.model.domain.TChatFeedback
import com.langhuan.service.TChatFeedbackService
import com.langhuan.serviceai.RagService
import com.langhuan.utils.other.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/chatFeedback"])
class ChatFeedbackController(
    private val tChatFeedbackService: TChatFeedbackService,
    private val ragService: RagService
) {

    companion object {
        private val log = LoggerFactory.getLogger(ChatFeedbackController::class.java)
    }

    // @PreAuthorize("hasAuthority('/chatFeedback/add')")
    @PostMapping(path = ["/add"])
    fun add(@RequestBody chatFeedback: TChatFeedback): Result<*> {
        chatFeedback.userId = SecurityUtils.getCurrentUsername()
        return Result.success(tChatFeedbackService.save(chatFeedback))
    }

    @PreAuthorize("hasAuthority('/chatFeedback/delete')")
    @PostMapping(path = ["/delete"])
    fun delete(@RequestParam(name = "id", required = true) id: Int): Result<String> {
        val removeById = tChatFeedbackService.removeById(id)
        return if (removeById) {
            Result.success("删除成功")
        } else {
            Result.error("删除失败")
        }
    }

    @PreAuthorize("hasAuthority('/chatFeedback/search')")
    @PostMapping(path = ["/search"])
    fun search(
        @RequestParam(name = "username", required = false) username: String?,
        @RequestParam(name = "interaction", required = false) interaction: String?,
        @RequestParam(name = "pageNum", required = false, defaultValue = "1") pageNum: Int,
        @RequestParam(name = "pageSize", required = false, defaultValue = "10") pageSize: Int
    ): Result<*> {
        return Result.success(tChatFeedbackService.chatFeedbackSearch(username, interaction, pageNum, pageSize))
    }

    @PreAuthorize("hasAuthority('/chatFeedback/changeDocumentTextByString')")
    @PostMapping(path = ["/changeDocumentTextByString"])
    @Throws(Exception::class)
    fun changeDocumentTextByString(
        @RequestParam(name = "document", required = false) document: String?,
        @RequestParam(name = "documentId", required = false) documentId: String?
    ): Result<*> {
        return Result.success(ragService.changeDocumentTextByString(document as String, documentId as String))
    }

    @PreAuthorize("hasAuthority('/chatFeedback/queryDocumentsByIds')")
    @PostMapping(path = ["/queryDocumentsByIds"])
    fun queryDocumentsByIds(@RequestParam(name = "fileIds", required = false) fileIds: String?): Result<*> {
        return Result.success(fileIds?.let { ragService.queryDocumentsByIds(it) })
    }

    @PreAuthorize("hasAuthority('/chatFeedback/changeInteractionToEnd')")
    @PostMapping(path = ["/changeInteractionToEnd"])
    fun changeInteractionToEnd(@RequestParam(name = "id", required = false) id: Int?): Result<*> {
        val update = tChatFeedbackService.update(
            UpdateWrapper<TChatFeedback>()
                .set("interaction", "end")
                .eq("id", id)
        )
        return Result.success(if (update) "标记成功" else "标记失败")
    }
}
