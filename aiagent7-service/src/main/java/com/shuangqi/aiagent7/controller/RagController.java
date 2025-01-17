package com.shuangqi.aiagent7.controller;

import com.shuangqi.aiagent7.service.TRagFileGroupService;
import com.shuangqi.aiagent7.service.TRagFileService;
import com.shuangqi.aiagent7.utils.rag.RagVectorUtils;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RagController {
    private final TRagFileService ragFileService;
    private final TRagFileGroupService ragFileGroupService;
    private final RagVectorUtils ragVectorUtils;

    public RagController(TRagFileService ragFileService, TRagFileGroupService ragFileGroupService, RagVectorUtils ragVectorUtils) {
        this.ragFileService = ragFileService;
        this.ragFileGroupService = ragFileGroupService;
        this.ragVectorUtils = ragVectorUtils;
    }


}
