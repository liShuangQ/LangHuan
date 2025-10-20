package com.langhuan.utils.other;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FileUtil类的单元测试
 */
class FileUtilTest {

    @Test
    void testCategorizeFiles() {
        // 创建测试文件
        MultipartFile[] files = new MultipartFile[]{
            new MockMultipartFile("image1", "image1.jpg", "image/jpeg", "test image content".getBytes()),
            new MockMultipartFile("image2", "image2.png", "image/png", "test image content".getBytes()),
            new MockMultipartFile("doc1", "doc1.txt", "text/plain", "test text content".getBytes()),
            new MockMultipartFile("doc2", "doc2.md", "text/x-markdown", "test markdown content".getBytes()),
            new MockMultipartFile("doc3", "doc3.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "test docx content".getBytes()),
            new MockMultipartFile("unknown", "unknown.pdf", "application/pdf", "test pdf content".getBytes()) // 不支持的类型
        };

        // 调用分类方法
        FileUtil.FileCategory result = FileUtil.categorizeFiles(files);

        // 验证结果
        assertNotNull(result, "返回结果不应为null");
        
        MultipartFile[] imageFiles = result.getImages();
        MultipartFile[] documentFiles = result.getDocuments();
        
        assertEquals(2, imageFiles.length, "应该有2个图片文件");
        assertEquals(3, documentFiles.length, "应该有3个文档文件");
        
        // 验证图片文件类型
        assertTrue(containsContentType(imageFiles, "image/jpeg"), "应该包含jpeg图片");
        assertTrue(containsContentType(imageFiles, "image/png"), "应该包含png图片");
        
        // 验证文档文件类型
        assertTrue(containsContentType(documentFiles, "text/plain"), "应该包含txt文档");
        assertTrue(containsContentType(documentFiles, "text/x-markdown"), "应该包含markdown文档");
        assertTrue(containsContentType(documentFiles, "application/vnd.openxmlformats-officedocument.wordprocessingml.document"), "应该包含docx文档");
    }

    @Test
    void testCategorizeFilesWithEmptyArray() {
        // 测试空数组
        MultipartFile[] emptyFiles = new MultipartFile[0];
        FileUtil.FileCategory result = FileUtil.categorizeFiles(emptyFiles);
        
        assertNotNull(result, "返回结果不应为null");
        assertEquals(0, result.getImages().length, "图片文件数组应该为空");
        assertEquals(0, result.getDocuments().length, "文档文件数组应该为空");
    }

    @Test
    void testCategorizeFilesWithNull() {
        // 测试null输入
        FileUtil.FileCategory result = FileUtil.categorizeFiles(null);
        
        assertNotNull(result, "返回结果不应为null");
        assertEquals(0, result.getImages().length, "图片文件数组应该为空");
        assertEquals(0, result.getDocuments().length, "文档文件数组应该为空");
    }

    @Test
    void testCategorizeFilesWithOnlyImages() {
        // 测试只有图片文件的情况
        MultipartFile[] imageFiles = new MultipartFile[]{
            new MockMultipartFile("image1", "image1.jpg", "image/jpeg", "test image content".getBytes()),
            new MockMultipartFile("image2", "image2.webp", "image/webp", "test image content".getBytes())
        };

        FileUtil.FileCategory result = FileUtil.categorizeFiles(imageFiles);
        
        assertNotNull(result, "返回结果不应为null");
        assertEquals(2, result.getImages().length, "应该有2个图片文件");
        assertEquals(0, result.getDocuments().length, "文档文件数组应该为空");
    }

    @Test
    void testCategorizeFilesWithOnlyDocuments() {
        // 测试只有文档文件的情况
        MultipartFile[] docFiles = new MultipartFile[]{
            new MockMultipartFile("doc1", "doc1.txt", "text/plain", "test text content".getBytes()),
            new MockMultipartFile("doc2", "doc2.markdown", "text/markdown", "test markdown content".getBytes())
        };

        FileUtil.FileCategory result = FileUtil.categorizeFiles(docFiles);
        
        assertNotNull(result, "返回结果不应为null");
        assertEquals(0, result.getImages().length, "图片文件数组应该为空");
        assertEquals(2, result.getDocuments().length, "应该有2个文档文件");
    }

    /**
     * 辅助方法：检查文件数组中是否包含指定内容类型的文件
     */
    private boolean containsContentType(MultipartFile[] files, String contentType) {
        for (MultipartFile file : files) {
            if (contentType.equals(file.getContentType())) {
                return true;
            }
        }
        return false;
    }
}
