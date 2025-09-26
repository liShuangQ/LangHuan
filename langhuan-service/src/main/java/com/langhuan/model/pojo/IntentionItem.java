package com.langhuan.model.pojo;

import lombok.Data;

import java.util.List;

@Data
public class IntentionItem {
    String id;
    String task;
    List<String> examples;
    String description;
}
