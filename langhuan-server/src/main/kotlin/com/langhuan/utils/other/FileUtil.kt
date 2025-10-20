package com.langhuan.utils.other

import org.springframework.web.multipart.MultipartFile

/**
 * 文件工具类，提供文件相关的实用方法
 */
object FileUtil {
    
    /**
     * 图片文件类型集合-前端限制的类型
     */
    private val IMAGE_TYPES = arrayOf(
        "image/jpeg",
        "image/jpg", 
        "image/png",
        "image/webp"
    )
    
    /**
     * 文档文件类型集合-前端限制的类型
     */
    private val DOCUMENT_TYPES = arrayOf(
        "text/plain", // .txt
        "text/markdown", // .markdown
        "text/x-markdown", // .md
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
    )
    
    /**
     * 根据文件类型区分MultipartFile数组
     * 
     * @param files 待区分的文件数组
     * @return 包含分类后文件的FileCategory对象
     */
    fun categorizeFiles(files: Array<MultipartFile>?): FileCategory {
        val imageFiles: MutableList<MultipartFile> = ArrayList()
        val documentFiles: MutableList<MultipartFile> = ArrayList()
        
        if (!files.isNullOrEmpty()) {
            for (file in files) {
                if (file.isEmpty) {
                    continue
                }
                
                val contentType = file.contentType
                if (contentType == null) {
                    continue
                }
                
                if (isImageType(contentType)) {
                    imageFiles.add(file)
                } else if (isDocumentType(contentType)) {
                    documentFiles.add(file)
                }
                // 其他类型的文件将被忽略
            }
        }
        
        val result = FileCategory()
        result.images = imageFiles.toTypedArray()
        result.documents = documentFiles.toTypedArray()
        
        return result
    }

    /**
     * 文件分类结果类
     */
    class FileCategory {
        var images: Array<MultipartFile>? = null
        var documents: Array<MultipartFile>? = null
    }
    
    /**
     * 判断文件是否为图片类型
     * 
     * @param contentType 文件内容类型
     * @return 如果是图片类型返回true，否则返回false
     */
    private fun isImageType(contentType: String): Boolean {
        for (type in IMAGE_TYPES) {
            if (type.equals(contentType, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
    
    /**
     * 判断文件是否为文档类型
     * 
     * @param contentType 文件内容类型
     * @return 如果是文档类型返回true，否则返回false
     */
    private fun isDocumentType(contentType: String): Boolean {
        for (type in DOCUMENT_TYPES) {
            if (type.equals(contentType, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}
