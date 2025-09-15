package com.langhuan.utils.rag.extractor;

import cn.hutool.core.util.IdUtil;
import com.langhuan.model.domain.TFileUrl;
import com.langhuan.service.CacheService;
import com.langhuan.service.MinioService;
import com.langhuan.service.TFileUrlService;
import com.langhuan.utils.other.SecurityUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.langhuan.common.Constant.CACHE_KEY;
import static com.langhuan.utils.http.GetRequestUtils.sendGetRequest;
import static org.apache.poi.xdgf.util.Util.sanitizeFilename;

import java.io.*;

/**
 * @author Afish
 * @date 2025/7/23 13:42
 */
@Slf4j
@Component
public class DocumentExtractor {

    @Value("${minio.folder.document-img}")
    private String minioFolder; // 在 MinIO 中模拟的“文件夹”前缀

    @Value("${minio.img-bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    private String publicBaseUrl; // 用于生成图片访问链接

    @Resource
    private final MinioService minioService;

    @Resource
    private CacheService cacheService;

    @Resource
    private TFileUrlService tFileUrlService;

    public DocumentExtractor(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostConstruct
    public void init() {
        // 确保 bucket 存在
        try {
            minioService.ensureBucketExists(bucketName);
        } catch (Exception e) {
            log.error("Failed to ensure bucket exists", e);
        }

        publicBaseUrl = minioUrl + "/" + bucketName;
        if (!publicBaseUrl.endsWith("/")) {
            publicBaseUrl += "/";
        }
    }

    public String extract(MultipartFile file) throws Exception {
        if (file.getOriginalFilename() != null &&
                file.getOriginalFilename().toLowerCase().endsWith(".docx")) {
            return extractDocxWithImages(file);
        } else {
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

    private String extractDocxWithImages(MultipartFile file) throws Exception {
        StringBuilder markdownContent = new StringBuilder();

        // 生成随机 ID 并存入缓存
        int randomId = (int) IdUtil.getSnowflakeNextId();
        cacheService.putId(SecurityUtils.getCurrentUsername() + CACHE_KEY, randomId);
        List<TFileUrl> fileUrlList = new ArrayList<>(); // 用于存储图片信息的列表

        try (InputStream is = file.getInputStream();
                XWPFDocument document = new XWPFDocument(is)) {

            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph) {
                    processParagraph((XWPFParagraph) element, markdownContent, fileUrlList, randomId); // 修改以包含fileUrlList
                    markdownContent.append("\n");
                } else if (element instanceof XWPFTable) {
                    processTable((XWPFTable) element, markdownContent, fileUrlList, randomId);
                    markdownContent.append("\n\n");
                }
            }

            // 批量插入数据库
            if (!fileUrlList.isEmpty()) {
                tFileUrlService.saveBatch(fileUrlList);
            }
        }
        return markdownContent.toString().trim();
    }

    private void processParagraph(XWPFParagraph paragraph, StringBuilder markdownContent, List<TFileUrl> fileUrlList,
            int randomId) {
        for (XWPFRun run : paragraph.getRuns()) {
            String text = run.getText(0);
            if (text != null && !text.isEmpty()) {
                markdownContent.append(text);
            }

            for (XWPFPicture picture : run.getEmbeddedPictures()) {
                handleImage(picture.getPictureData(), fileUrlList, randomId).ifPresent(markdownContent::append);
            }
        }
    }

    private void processTable(XWPFTable table, StringBuilder markdownContent, List<TFileUrl> fileUrlList,
            int randomId) {
        for (XWPFTableRow row : table.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    processParagraph(paragraph, markdownContent, fileUrlList, randomId);
                }
                markdownContent.append("\t"); // 使用制表符分隔单元格内容
            }
            markdownContent.append("\n"); // 每行结束后换行
        }
    }

    @SneakyThrows
    private Optional<String> handleImage(XWPFPictureData pictureData, List<TFileUrl> fileUrlList, int randomId) {
        String extension = pictureData.suggestFileExtension();
        String fileName = UUID.randomUUID() + "." + extension;
        String objectName = (minioFolder != null ? minioFolder + "/" : "") + fileName;

        try (InputStream inputStream = new ByteArrayInputStream(pictureData.getData())) {
            minioService.handleUpload(objectName, inputStream, pictureData.getData().length, bucketName);
        } catch (Exception e) {
            log.error("Failed to upload image to MinIO", e);
            return Optional.empty();
        }

        // 生成可访问的 URL
        String imageUrl = publicBaseUrl + objectName;

        // 创建 TFileUrl 实体并添加到列表中
        TFileUrl fileUrl = new TFileUrl();
        fileUrl.setFileId(randomId);
        fileUrl.setFUrl(imageUrl);
        fileUrl.setFStatus("临时");
        fileUrlList.add(fileUrl);

        return Optional.of("\n![image](" + imageUrl + ")\n");
    }

    /**
     * 提取 HTML 内容并下载图片到 MinIO
     */
    public String extract(String url) throws Exception {
        String htmlContent = sendGetRequest(url);

        Path dirPath = Paths.get(minioFolder);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath); // 本地调试用，可选
        }

        org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);
        Elements imgs = doc.select("img");

        // 生成随机 ID 并存入缓存
        int randomId = (int) IdUtil.getSnowflakeNextId();
        cacheService.putId(SecurityUtils.getCurrentUsername() + CACHE_KEY, randomId);

        // 用于批量插入的列表
        List<TFileUrl> fileUrlList = new ArrayList<>();

        for (Element img : imgs) {
            String src = img.attr("src");
            String alt = img.hasAttr("alt") ? img.attr("alt") : "image";

            if (src.isEmpty())
                continue;

            URL imageUrl = new URL(new URL(url), src);
            String filename = sanitizeFilename(imageUrl.getPath());
            if (!filename.contains(".")) {
                filename += ".jpg"; // 默认扩展名
            }
            String objectName = (minioFolder != null ? minioFolder + "/" : "") + filename;

            try (InputStream in = imageUrl.openStream()) {
                minioService.handleUpload(objectName, in, -1, bucketName); // -1 表示未知大小
            } catch (Exception e) {
                log.error("Failed to upload image from URL to MinIO: {}", imageUrl, e);
                continue;
            }

            // 构建公开访问 URL
            String publicImageUrl = publicBaseUrl + objectName;

            // 创建 TFileUrl 实体
            TFileUrl fileUrl = new TFileUrl();
            fileUrl.setFileId(randomId);
            fileUrl.setFUrl(publicImageUrl);
            fileUrl.setFStatus("临时"); // 或 "temp"，根据你的业务定义
            fileUrlList.add(fileUrl);

            String markdownLink = String.format("![%s](%s%s)", alt, publicBaseUrl, objectName);
            img.replaceWith(new org.jsoup.nodes.TextNode(markdownLink));
        }

        // 批量插入数据库
        if (!fileUrlList.isEmpty()) {
            tFileUrlService.saveBatch(fileUrlList);
        }

        return doc.body().text();
    }
}