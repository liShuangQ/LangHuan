package com.langhuan.functionTools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.core.io.FileSystemResource;

import java.util.List;

@Slf4j
public class FileReadTools {

    @Tool(description = "文档解析函数", returnDirect = true)
    String DocumentAnalyzer(@ToolParam(description = "需要解析的文档路径") String path) {
        log.info("调用工具：" + "DocumentAnalyzer");
        // ai解析用户的提问得到path参数，使用tika读取本地文件获取内容。
        try {
            log.debug("DocumentAnalyzer path: {}", path);
            TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(new FileSystemResource(path));
            List<Document> documents = tikaDocumentReader.read();
            StringBuilder stringBuilder = new StringBuilder();
            for (Document document : documents) {
                stringBuilder.append(document.getFormattedContent());
            }
//            String prompt = """
//                      ***tools***
//                      你是一个文件阅读专家，擅长阅读文件内容，并给出文件内容的摘要。
//                      请根据用户提供的文件内容，给出文件内容的摘要。
//
//                      输入格式：
//                      文件内容：一段文件内容。
//                      文档内容使用```包裹。
//
//                      输出格式：
//                      直接输出一个字符串，内容为文件内容的摘要。
//                      ******
//                    """;
//            return prompt + "```" + stringBuilder.toString() + "```";
            return stringBuilder.toString();

        } catch (Exception e) {
            log.error("解析文件失败", e);
            return "解析文件失败";
        }
    }


}
