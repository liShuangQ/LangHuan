package com.langhuan.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TChatFeedback;
import com.langhuan.service.TChatFeedbackService;
import com.langhuan.serviceai.RagService;
import com.langhuan.utils.other.SecurityUtils;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/chatFeedback")
public class ChatFeedbackController {

    private final TChatFeedbackService tChatFeedbackService;
    private final RagService ragService;

    public ChatFeedbackController(TChatFeedbackService tChatFeedbackService, RagService ragService) {
        this.tChatFeedbackService = tChatFeedbackService;
        this.ragService = ragService;
    }

    // @PreAuthorize("hasAuthority('/chatFeedback/add')")
    @PostMapping(path = "/add")
    public Result add(
            @RequestBody TChatFeedback chatFeedback) {
        chatFeedback.setUserId(SecurityUtils.getCurrentUsername());
        return Result.success(tChatFeedbackService.save(chatFeedback));
    }

    @PreAuthorize("hasAuthority('/chatFeedback/delete')")
    @PostMapping(path = "/delete")
    public Result delete(
            @RequestParam(name = "id", required = true) Integer id) {
        boolean removeById = tChatFeedbackService.removeById(id);
        if (removeById) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    @PreAuthorize("hasAuthority('/chatFeedback/search')")
    @PostMapping(path = "/search")
    public Result search(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "interaction", required = false) String interaction,
            @RequestParam(name = "pageNum", required = false, defaultValue = "1") int pageNum,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return Result.success(tChatFeedbackService.chatFeedbackSearch(username, interaction, pageNum, pageSize));
    }

    @PreAuthorize("hasAuthority('/chatFeedback/changeDocumentTextByString')")
    @PostMapping(path = "/changeDocumentTextByString")
    public Result changeDocumentTextByString(
            @RequestParam(name = "document", required = false) String document,
            @RequestParam(name = "documentId", required = false) String documentId) throws Exception {
        return Result.success(ragService.changeDocumentTextByString(
                document, documentId));
    }

    @PreAuthorize("hasAuthority('/chatFeedback/queryDocumentsByIds')")
    @PostMapping(path = "/queryDocumentsByIds")
    public Result queryDocumentsByIds(
            @RequestParam(name = "fileIds", required = false) String fileIds) {
        return Result.success(ragService.queryDocumentsByIds(fileIds));
    }

    @PreAuthorize("hasAuthority('/chatFeedback/changeInteractionToEnd')")
    @PostMapping(path = "/changeInteractionToEnd")
    public Result changeInteractionToEnd(
            @RequestParam(name = "id", required = false) Integer id) {
        boolean update = tChatFeedbackService.update(new LambdaUpdateWrapper<TChatFeedback>()
                .set(TChatFeedback::getInteraction, "end")
                .eq(TChatFeedback::getId, id));
        return Result.success(update ? "标记成功" : "标记失败");
    }

}
