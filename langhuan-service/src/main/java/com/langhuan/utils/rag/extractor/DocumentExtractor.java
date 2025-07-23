package com.langhuan.utils.rag.extractor;

<<<<<<< HEAD
import com.langhuan.utils.rag.config.SplitConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
=======
import lombok.SneakyThrows;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
>>>>>>> f79417a (feat(RAG): 实现ETL管道重构及组件化)
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

<<<<<<< HEAD
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.langhuan.utils.http.GetRequestUtils.sendGetRequest;
import static org.apache.poi.xdgf.util.Util.sanitizeFilename;


=======
>>>>>>> f79417a (feat(RAG): 实现ETL管道重构及组件化)
/**
 * @author Afish
 * @date 2025/7/23 13:42
 */
<<<<<<< HEAD
@Slf4j
@Component
public class DocumentExtractor {
    @Value("${project.folder:}")
    private String downloadDir;

=======
@Component
public class DocumentExtractor {
>>>>>>> f79417a (feat(RAG): 实现ETL管道重构及组件化)
    @SneakyThrows
    public String extract(MultipartFile file) {
        TikaDocumentReader reader = new TikaDocumentReader(new InputStreamResource(file.getInputStream()));
        StringBuilder contentBuilder = new StringBuilder();

        for (Document doc : reader.read()) {
            contentBuilder.append(doc.getFormattedContent()).append("\n");
        }

        String content = contentBuilder.toString();
<<<<<<< HEAD

=======
>>>>>>> f79417a (feat(RAG): 实现ETL管道重构及组件化)
        if (content.contains("source: Invalid source URI")) {
            content = content.replaceAll(
                    "(?m)^\\s*source: Invalid source URI: InputStream resource.*$", "").trim();
        }
<<<<<<< HEAD
        return content;
    }

    /**
     * 发送GET请求并解析HTML中的图片，下载图片并替换为Markdown格式
     *
     * @param url         请求的URL
     * @return 处理后的HTML内容（图片被替换为Markdown格式）
     * @throws Exception 如果请求或处理过程中发生异常
     */
    public  String extract(String url) throws Exception {
        // 1. 发送GET请求获取HTML内容
        String htmlContent = sendGetRequest(url);

        // 2. 创建下载目录
        Path dirPath = Paths.get(downloadDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 3. 使用Jsoup解析HTML
        org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);
        Elements imgs = doc.select("img");

        // 4. 遍历所有图片
        for (Element img : imgs) {
            String src = img.attr("src");
            String alt = img.hasAttr("alt") ? img.attr("alt") : "image";

            if (src.isEmpty()) {
                continue;
            }

            // 5. 下载图片
            URL imageUrl = new URL(new URL(url), src); // 处理相对路径
            String filename = sanitizeFilename(imageUrl.getPath());
            String localPath = downloadDir + File.separator + filename;

            downloadImage(imageUrl, localPath);

            // 6. 构建Markdown图片链接
            String markdownLink = String.format("![%s](%s)", alt, localPath);

            // 7. 替换img标签为Markdown文本
            img.replaceWith(new org.jsoup.nodes.TextNode(markdownLink));
        }

        // 8. 返回处理后的HTML文本（此时img标签已被替换）
        return doc.body().text();
    }

    /**
     * 下载图片到本地
     */
    private static void downloadImage(URL imageUrl, String localPath) throws IOException {
        try (InputStream in = imageUrl.openStream();
             FileOutputStream out = new FileOutputStream(localPath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            log.info("Downloaded image: {}", localPath);
        } catch (IOException e) {
            log.error("Failed to download image: {}", imageUrl, e);
        }
    }
=======

        return content;
    }
>>>>>>> f79417a (feat(RAG): 实现ETL管道重构及组件化)
}
