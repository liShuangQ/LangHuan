package com.langhuan.model.pojo;

import lombok.Data;


@Data
public class ChatRestOption {
    String chatId;
    String prompt;
    // 对应userMessage
    String userMessage;
    Boolean isRag;
    String ragGroupId;
    Boolean isReRank;
    Boolean isFunction;
    String modelName;
}
