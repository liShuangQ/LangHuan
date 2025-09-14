package com.langhuan.model.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ChatRestOption {
    String chatId;
    String prompt;
    // 对应userMessage
    String question;
    Boolean isRag;
    String ragGroupId;
    Boolean isReRank;
    Boolean isFunction;
    String modelName;
    List<String> imageunderstanding;
}
