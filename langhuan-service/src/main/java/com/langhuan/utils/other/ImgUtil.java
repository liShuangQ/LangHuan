package com.langhuan.utils.other;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.web.multipart.MultipartFile;

/**
 * 图片处理工具类
 *
 * @author langhuan
 */
public class ImgUtil {
    /**
     * 将文件编码为base64字符串
     *
     * @param file 文件
     * @return base64编码的字符串
     * @throws IOException IO异常
     */
    public static String encodeFileToBase64(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            int bytesRead = fis.read(bytes);
            if (bytesRead != bytes.length) {
                throw new IOException("未能完全读取文件内容");
            }
            return Base64.getEncoder().encodeToString(bytes);
        }
    }

    /**
     * 将MultipartFile编码为base64字符串
     *
     * @param file MultipartFile对象
     * @return base64编码的字符串
     * @throws IOException IO异常
     */
    public static String encodeMultipartFileToBase64(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("文件为空");
        }
        try (InputStream inputStream = file.getInputStream()) {
            byte[] bytes = file.getBytes();
            return Base64.getEncoder().encodeToString(bytes);
        }
    }

    /**
     * 根据文件扩展名获取图片格式
     *
     * @param file 文件
     * @return 图片格式（如png, jpeg等）
     */
    public static String getImageFormat(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String extension = "";

        int lastDotIndex = 0;
        if (fileName != null) {
            lastDotIndex = fileName.lastIndexOf('.');
        }
        if (lastDotIndex > 0) {
            extension = fileName.substring(lastDotIndex + 1).toLowerCase();
        }

        // 根据扩展名返回对应的MIME类型
        return switch (extension) {
            case "jpg", "jpeg" -> "jpeg";
            case "png" -> "png";
            case "webp" -> "webp";
            default ->
                // 默认返回jpeg
                    "jpeg";
        };
    }

    // 获取base64格式图片的格式信息
    public static String getImageFormatFromBase64(String base64) {
        return base64.substring(base64.indexOf("/") + 1, base64.indexOf(";"));
    }

    // base64格式图片转换为 InputStream。 带有data:xxxx;base64,前缀的
    public static InputStream base64ToInputStream(String base64) {
        return new ByteArrayInputStream(Base64.getDecoder().decode(base64.split(",")[1]));
    }

    // 将base64格式替换为其他信息
    public static String replaceBase64WithOtherInfo(String base64) {
        return base64.replaceAll("!\\[.*?\\]\\((.*?)\\)", "[媒体信息]");
    }

    public static String replaceBase64WithOtherInfo(String base64, String otherStr) {
        return base64.replaceAll("!\\[.*?\\]\\((.*?)\\)", otherStr);
    }

    /**
     * 从字符串中提取Markdown格式图片的URL和剔除URL信息后的文本
     *
     * @param content 包含Markdown图片语法的字符串
     * @return MarkdownImageResult对象，包含提取到的URL列表和剔除URL信息后的文本
     */
    public static MarkdownImageResult extractMarkdownImageUrls(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }

        // Markdown图片语法的正则表达式：![alt](url)
        // 匹配任意字符（非贪婪）作为alt文本，然后是括号中的URL
        Pattern pattern = Pattern.compile("!\\[.*?\\]\\((.*?)\\)");
        Matcher matcher = pattern.matcher(content);

        List<String> urls = new ArrayList<>();
        StringBuffer cleanedContent = new StringBuffer();

        while (matcher.find()) {
            String url = matcher.group(1);
            if (url != null && !url.isEmpty()) {
                urls.add(url);
            }
            // 替换匹配到的图片语法为空字符串
            matcher.appendReplacement(cleanedContent, "");
        }
        matcher.appendTail(cleanedContent);

        // 如果没有找到任何URL，返回null
        return urls.isEmpty() ? null : new MarkdownImageResult(urls, cleanedContent.toString().trim());
    }

    /**
     * 封装Markdown图片提取结果的内部类
     */
    public static class MarkdownImageResult {
        private final List<String> urls;
        private final String cleanedContent;

        public MarkdownImageResult(List<String> urls, String cleanedContent) {
            this.urls = urls;
            this.cleanedContent = cleanedContent;
        }

        public List<String> getUrls() {
            return urls;
        }

        public String getCleanedContent() {
            return cleanedContent;
        }
    }
}
