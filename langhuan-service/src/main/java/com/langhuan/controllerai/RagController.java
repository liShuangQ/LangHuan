package com.langhuan.controllerai;

import com.langhuan.common.Result;
import com.langhuan.serviceai.RagService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RagController {
    private final RagService ragService;


    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/rag/recallTesting")
    public Result recallTesting(
            @RequestParam(name = "q", required = true) String q,
            @RequestParam(name = "groupId", required = true) String groupId
    ) {
        return Result.success(ragService.ragSearch(q, groupId));
    }
}
