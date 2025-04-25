package com.langhuan.controller;

import com.langhuan.common.Result;
import com.langhuan.model.domain.TChatFeedback;
import com.langhuan.service.TChatFeedbackService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/chatFeedback")
public class ChatFeedbackController {

    private final TChatFeedbackService tChatFeedbackService;

    public ChatFeedbackController(TChatFeedbackService tChatFeedbackService) {
        this.tChatFeedbackService = tChatFeedbackService;
    }

    public Result add(
            @RequestBody TChatFeedback chatFeedback
    ) {
        return Result.success(tChatFeedbackService.save(chatFeedback));
    }
}
