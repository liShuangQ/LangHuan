package com.langhuan.utils.other;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件工具类，提供文件相关的实用方法
 */
public class FileUtil {
    
    /**
     * 图片文件类型集合-前端限制的类型
     */
    private static final String[] IMAGE_TYPES = {
        "image/jpeg",
        "image/jpg", 
        "image/png",
        "image/webp"
    };
    
    /**
     * 文档文件类型集合-前端限制的类型
     */
    private static final String[] DOCUMENT_TYPES = {
        "text/plain", // .txt
        "text/markdown", // .markdown
        "text/x-markdown", // .md
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
    };
    
    /**
     * 根据文件类型区分MultipartFile数组
     * 
     * @param files 待区分的文件数组
     * @return 包含分类后文件的FileCategory对象
     */
    public static FileCategory categorizeFiles(MultipartFile[] files) {
        List<MultipartFile> imageFiles = new ArrayList<>();
        List<MultipartFile> documentFiles = new ArrayList<>();
        
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                
                String contentType = file.getContentType();
                if (contentType == null) {
                    continue;
                }
                
                if (isImageType(contentType)) {
                    imageFiles.add(file);
                } else if (isDocumentType(contentType)) {
                    documentFiles.add(file);
                }
                // 其他类型的文件将被忽略
            }
        }
        
        FileCategory result = new FileCategory();
        result.setImages(imageFiles.toArray(new MultipartFile[0]));
        result.setDocuments(documentFiles.toArray(new MultipartFile[0]));
        
        return result;
    }

    /**
     * 文件分类结果类
     */
    @Data
    public static class FileCategory {
        private MultipartFile[] images;
        private MultipartFile[] documents;
    }
    
    /**
     * 判断文件是否为图片类型
     * 
     * @param contentType 文件内容类型
     * @return 如果是图片类型返回true，否则返回false
     */
    private static boolean isImageType(String contentType) {
        for (String type : IMAGE_TYPES) {
            if (type.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 判断文件是否为文档类型
     * 
     * @param contentType 文件内容类型
     * @return 如果是文档类型返回true，否则返回false
     */
    private static boolean isDocumentType(String contentType) {
        for (String type : DOCUMENT_TYPES) {
            if (type.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
}
