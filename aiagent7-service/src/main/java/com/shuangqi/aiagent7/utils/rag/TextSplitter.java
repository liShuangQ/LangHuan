
package com.shuangqi.aiagent7.utils.rag;

import java.util.List;

public interface TextSplitter {
    /**
     * 将输入文本分割成多个字符串块
     *
     * @param text 待分割的文本
     * @return 分割后的字符串块列表
     */
    List<String> apply(String text);
}