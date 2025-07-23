package com.langhuan.utils.rag.util;

import com.langhuan.common.Constant;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author Afish
 * @date 2025/7/23 13:53
 */
@Component
public class DocumentStreamUtils {
    public InputStreamResource generateStream(List<String> contents) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
            for (String content : contents) {
                writer.write(content);
                writer.write(System.lineSeparator());
                writer.write(Constant.DEFAULT_RAG_EXPORT_SPLIT);
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate document stream", e);
        }
        return new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray()));
    }
}
