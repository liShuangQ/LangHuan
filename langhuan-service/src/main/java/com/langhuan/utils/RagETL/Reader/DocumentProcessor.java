package com.langhuan.utils.RagETL.Reader;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DocumentProcessor {

    public List<Document> processDocument(Resource resource, String fileType) {
        List<Document> documents = null;
        switch (fileType.toLowerCase()) {
            case "json":
                JsonReader jsonReader = new JsonReader(resource, "description");
                documents = jsonReader.get();
                break;
            case "txt":
                TextReader textReader = new TextReader(resource);
                textReader.getCustomMetadata().put("filename", "text-source.txt");
                documents = textReader.read();
                break;
//            case "html":
//                JsoupDocumentReaderConfig htmlConfig = JsoupDocumentReaderConfig.builder()
//                        .selector("article p")
//                        .charset("ISO-8859-1")
//                        .includeLinkUrls(true)
//                        .metadataTags(List.of("author", "date"))
//                        .additionalMetadata("source", "my-page.html")
//                        .build();
//                JsoupDocumentReader htmlReader = new JsoupDocumentReader(resource, htmlConfig);
//                documents = htmlReader.get();
//                break;
//            case "md":
//                MarkdownDocumentReaderConfig markdownConfig = MarkdownDocumentReaderConfig.builder()
//                        .withHorizontalRuleCreateDocument(true)
//                        .withIncludeCodeBlock(false)
//                        .withIncludeBlockquote(false)
//                        .withAdditionalMetadata("filename", "code.md")
//                        .build();
//                MarkdownDocumentReader markdownReader = new MarkdownDocumentReader(resource, markdownConfig);
//                documents = markdownReader.get();
//                break;
//            case "pdf":
//                // 这里假设根据需求选择 PagePdfDocumentReader 或 ParagraphPdfDocumentReader
//                // 简单起见，先使用 PagePdfDocumentReader
//                PdfDocumentReaderConfig pdfConfig = PdfDocumentReaderConfig.builder()
//                        .withPageTopMargin(0)
//                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
//                                .withNumberOfTopTextLinesToDelete(0)
//                                .build())
//                        .withPagesPerDocument(1)
//                        .build();
//                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(resource, pdfConfig);
//                documents = pdfReader.read();
//                break;
            case "docx":
            case "pptx":
//            case "pdf":
            case "md":
                TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
                documents = tikaDocumentReader.read();
                break;
            default:
                throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }
        return documents;
    }
}