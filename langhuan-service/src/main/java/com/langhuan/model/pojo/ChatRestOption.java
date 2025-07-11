package com.langhuan.model.pojo;

import lombok.Data;

@Data
public class ChatRestOption {
    String chatId;
    String prompt;
    String question;
    Boolean isRag;
    String ragGroupId;
    Boolean isReRank;
    Boolean isFunction;
    String modelName;
}
