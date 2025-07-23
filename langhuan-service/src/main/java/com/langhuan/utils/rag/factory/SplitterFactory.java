package com.langhuan.utils.rag.factory;

import com.langhuan.serviceai.ChatGeneralAssistanceService;
import com.langhuan.utils.rag.config.SplitConfig;
import com.langhuan.utils.rag.splitter.FixedWindowTextSplitter;
import com.langhuan.utils.rag.splitter.LlmTextSplitter;
import com.langhuan.utils.rag.splitter.PatternTokenTextSplitter;
import com.langhuan.utils.rag.splitter.TextSplitter;

import java.util.regex.Pattern;

/**
 * 分割器工厂
 */
public class SplitterFactory {
    public static TextSplitter createSplitter(SplitConfig config, ChatGeneralAssistanceService chatService) {
        switch (config.getSplitMethod()) {
            case "FixedWindowTextSplitter":
                return new FixedWindowTextSplitter((Integer) config.getMethodData().get("windowSize"));
            case "PatternTokenTextSplitter":
                return new PatternTokenTextSplitter(Pattern.compile((String) config.getMethodData().get("splitPattern")));
            case "LlmTextSplitter":
                return new LlmTextSplitter(
                        (Integer) config.getMethodData().get("windowSize"),
                        (String) config.getMethodData().get("modelName"),
                        chatService
                );
            default:
                throw new IllegalArgumentException("Unsupported split method: " + config.getSplitMethod());
        }
    }
}
