package com.langhuan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.langhuan.common.Result;
import com.langhuan.model.domain.TChatFeedback;
import com.langhuan.service.TChatFeedbackService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/chatFeedback")
public class ChatFeedbackController {

    private final TChatFeedbackService tChatFeedbackService;

    public ChatFeedbackController(TChatFeedbackService tChatFeedbackService) {
        this.tChatFeedbackService = tChatFeedbackService;
    }

    @PostMapping(path = "/add")
    public Result add(
            @RequestBody TChatFeedback chatFeedback
    ) {
        chatFeedback.setUserId(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.success(tChatFeedbackService.save(chatFeedback));
    }

    @PostMapping(path = "/search")
    public Result search(
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam(name = "interaction", required = false) String interaction,
            @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
            @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize
    ) {
        return Result.success(tChatFeedbackService.page(
                        new Page<>(currentPage, pageSize),
                        new LambdaQueryWrapper<TChatFeedback>()
                                .like(!userId.isEmpty(), TChatFeedback::getUserId, userId)
                                .eq(!interaction.isEmpty(), TChatFeedback::getInteraction, interaction)
                                .orderBy(true, false, TChatFeedback::getInteractionTime)
                )
        );
    }
}
