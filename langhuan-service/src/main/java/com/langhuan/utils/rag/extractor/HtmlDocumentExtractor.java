package com.langhuan.utils.rag.extractor;

import lombok.SneakyThrows;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author Afish
 * @date 2025/7/23 13:42
 */
@Component
public class HtmlDocumentExtractor {
    @Value("${project.folder:}")
    private String downloadDir;

    @SneakyThrows
    public String extract(MultipartFile file) {
        TikaDocumentReader reader = new TikaDocumentReader(new InputStreamResource(file.getInputStream()));
        StringBuilder contentBuilder = new StringBuilder();

        for (Document doc : reader.read()) {
            contentBuilder.append(doc.getFormattedContent()).append("\n");
        }

        String content = contentBuilder.toString();
        if (content.contains("source: Invalid source URI")) {
            content = content.replaceAll(
                    "(?m)^\\s*source: Invalid source URI: InputStream resource.*$", "").trim();
        }

        return content;
    }
}
