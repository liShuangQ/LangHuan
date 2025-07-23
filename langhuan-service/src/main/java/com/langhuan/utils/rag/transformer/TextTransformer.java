package com.langhuan.utils.rag.transformer;

import com.langhuan.utils.rag.splitter.TextSplitter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Afish
 * @date 2025/7/23 13:45
 */
@Component
public class TextTransformer {
    public List<String> transform(String text, TextSplitter splitter) {
        return splitter.apply(text);
    }
}
