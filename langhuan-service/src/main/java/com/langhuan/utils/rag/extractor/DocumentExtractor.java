package com.langhuan.utils.rag.extractor;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.langhuan.utils.http.GetRequestUtils.sendGetRequest;
import static org.apache.poi.xdgf.util.Util.sanitizeFilename;

import org.apache.poi.xwpf.usermodel.*;
import java.io.*;
import java.util.UUID;
/**
 * @author Afish
 * @date 2025/7/23 13:42
 */
@Slf4j
@Component
public class DocumentExtractor {
    @Value("${project.folder:}")
    private String downloadDir;

    @SneakyThrows
    public String extract(MultipartFile file) {
        // 检查是否为DOCX文件
        if (file.getOriginalFilename() != null &&
                file.getOriginalFilename().toLowerCase().endsWith(".docx")) {
            return extractDocxWithImages(file);
        } else {
            // 非DOCX文件使用原有Tika处理
            return extractWithTika(file);
        }
    }

    private String extractWithTika(MultipartFile file) throws Exception {
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

    @SneakyThrows
    private String extractDocxWithImages(MultipartFile file) {
        StringBuilder markdownContent = new StringBuilder();
        try (InputStream is = file.getInputStream();
             XWPFDocument document = new XWPFDocument(is)) {

            // 确保下载目录存在
            File outputDir = new File(downloadDir);
            if (!outputDir.exists()) outputDir.mkdirs();

            // 处理文档中的所有元素
            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph) {
                    processParagraph((XWPFParagraph) element, markdownContent);
                    // 段落结束后添加换行
                    markdownContent.append("\n");
                } else if (element instanceof XWPFTable) {
                    processTable((XWPFTable) element, markdownContent);
                    markdownContent.append("\n\n");  // 表格后添加额外换行
                }
            }
        }
        return markdownContent.toString().trim();
    }

    private void processParagraph(XWPFParagraph paragraph, StringBuilder markdownContent) {
        for (XWPFRun run : paragraph.getRuns()) {
            // 原样输出文本内容（不处理样式）
            String text = run.getText(0);
            if (text != null && !text.isEmpty()) {
                markdownContent.append(text);
            }

            // 处理图片（使用Markdown格式）
            for (XWPFPicture picture : run.getEmbeddedPictures()) {
                markdownContent.append(handleImage(picture.getPictureData()));
            }
        }
    }

    private void processTable(XWPFTable table, StringBuilder markdownContent) {
        // 原样输出表格内容（使用制表符分隔）
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    processParagraph(paragraph, markdownContent);
                }
                markdownContent.append("\t"); // 单元格分隔符
            }
            markdownContent.append("\n"); // 行结束
        }
    }

    @SneakyThrows
    private String handleImage(XWPFPictureData pictureData) {
        // 生成唯一文件名
        String extension = pictureData.suggestFileExtension();
        String fileName = UUID.randomUUID() + "." + extension;
        String filePath = downloadDir + fileName;
        File imageFile = new File(downloadDir, fileName);

        // 保存图片到本地
        try (OutputStream os = new FileOutputStream(imageFile)) {
            os.write(pictureData.getData());
        }

        // 返回Markdown格式的图片引用
        return "\n![" + imageFile.getName() + "](" + filePath + ")\n";
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
}
