package com.shuangqi.aiagent7.serviceai;

import com.shuangqi.aiagent7.utils.rag.RagFileVectorUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private final RagFileVectorUtils ragFileVectorUtils;

    public RagService(RagFileVectorUtils ragFileVectorUtils) {
        this.ragFileVectorUtils = ragFileVectorUtils;
    }

    public List<String> readAndSplitDocument(MultipartFile file, String splitFileMethod,
                                             Map<String, Object> methodData) {
        return ragFileVectorUtils.readAndSplitDocument(file, splitFileMethod, methodData);
    }
}
