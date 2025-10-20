package com.langhuan.utils.rag.splitter;

import java.util.List;

/**
 * @author Afish
 * @date 2025/7/23 11:12
 */
public interface TextSplitter {
    List<String> apply(String text);
}
