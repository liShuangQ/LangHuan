package com.langhuan.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class RagIntentionClassifierDTO {
    Boolean isAdd;
    List<String> documents;
}
