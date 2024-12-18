package com.shuangqi.aiagent7.utils;

import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MyTokenTextSplitter {

    private static final Pattern DEFAULT_SPLIT_PATTERN = Pattern.compile("[.。!?！？；；：：]+\\s*");

    public List<Document> apply(String text) {
        return apply(text, DEFAULT_SPLIT_PATTERN);
    }

    /**
     * 根据指定的分割模式将文本分割成多个文档
     * 此方法主要用于文本预处理阶段，将连续的文本内容根据提供的分割模式分割成独立的句子，
     * 并将这些句子转换成Document对象列表这有助于后续的文本处理或分析
     *
     * @param text         待处理的文本内容
     * @param splitPattern 分割文本的正则表达式模式
     * @return 文本中提取出的Document对象列表，每个Document对象代表一个分割后的句子
     */
    public List<Document> apply(String text, Pattern splitPattern) {
        // 初始化文档列表，用于存储分割后的句子创建的Document对象
        List<Document> documents = new ArrayList<>();
        // 使用提供的分割模式将文本分割成句子数组
        String[] sentences = splitPattern.split(text);

        // 遍历分割后的每个句子
        for (String sentence : sentences) {
            // 去除句子前后的空白字符
            sentence = sentence.trim();
            // 如果句子非空，则创建一个新的Document对象，并添加到文档列表中
            if (!sentence.isEmpty()) {
                documents.add(new Document(sentence));
            }
        }
        // 返回包含所有创建的Document对象的列表
        return documents;
    }

//    public static void main(String[] args) {
//        MyTokenTextSplitter splitter = new MyTokenTextSplitter();
//        String text = "Hello world！你好，世界。How are you doing today？今天你过得好吗？ I hope you're well.";
//        List<Document> documents = splitter.apply(text);
//        for (Document doc : documents) {
//            System.out.println(doc.getContent());
//        }
//    }
}
