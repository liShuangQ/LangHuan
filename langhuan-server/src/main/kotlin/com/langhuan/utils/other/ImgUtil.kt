package com.langhuan.utils.other

import org.springframework.web.multipart.MultipartFile
import java.io.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * 图片处理工具类
 *
 * @author langhuan
 */
object ImgUtil {
    /**
     * 将文件编码为base64字符串
     *
     * @param file 文件
     * @return base64编码的字符串
     * @throws IOException IO异常
     */
    @Throws(IOException::class)
    fun encodeFileToBase64(file: File): String {
        return FileInputStream(file).use { fis ->
            val bytes = ByteArray(file.length().toInt())
            val bytesRead = fis.read(bytes)
            if (bytesRead != bytes.size) {
                throw IOException("未能完全读取文件内容")
            }
            java.util.Base64.getEncoder().encodeToString(bytes)
        }
    }

    /**
     * 将MultipartFile编码为base64字符串
     *
     * @param file MultipartFile对象
     * @return base64编码的字符串
     * @throws IOException IO异常
     */
    @Throws(IOException::class)
    fun encodeMultipartFileToBase64(file: MultipartFile?): String {
        if (file == null || file.isEmpty) {
            throw IOException("文件为空")
        }
        return file.inputStream.use {
            val bytes = file.bytes
            java.util.Base64.getEncoder().encodeToString(bytes)
        }
    }

    /**
     * 根据文件扩展名获取图片格式
     *
     * @param file 文件
     * @return 图片格式（如png, jpeg等）
     */
    fun getImageFormat(file: MultipartFile): String {
        val fileName = file.originalFilename
        var extension = ""

        var lastDotIndex = 0
        if (fileName != null) {
            lastDotIndex = fileName.lastIndexOf('.')
        }
        if (lastDotIndex > 0) {
            if (fileName != null) {
                extension = fileName.substring(lastDotIndex + 1).lowercase()
            }
        }

        // 根据扩展名返回对应的MIME类型
        return when (extension) {
            "jpg", "jpeg" -> "jpeg"
            "png" -> "png"
            "webp" -> "webp"
            else ->
                // 默认返回jpeg
                "jpeg"
        }
    }

    // 获取base64格式图片的格式信息
    fun getImageFormatFromBase64(base64: String): String {
        return base64.substring(base64.indexOf("/") + 1, base64.indexOf(";"))
    }

    // base64格式图片转换为 InputStream。 带有data:xxxx;base64,前缀的
    fun base64ToInputStream(base64: String): InputStream {
        return ByteArrayInputStream(java.util.Base64.getDecoder().decode(base64.split(",")[1]))
    }

    // 将base64格式替换为其他信息
    fun replaceBase64WithOtherInfo(base64: String): String {
        return base64.replace(Regex("!\\[.*?\\]\\((.*?)\\)"), "[媒体信息]")
    }

    fun replaceBase64WithOtherInfo(base64: String, otherStr: String): String {
        return base64.replace(Regex("!\\[.*?\\]\\((.*?)\\)"), otherStr)
    }

    /**
     * 从字符串中提取Markdown格式图片的URL和剔除URL信息后的文本
     *
     * @param content 包含Markdown图片语法的字符串
     * @return MarkdownImageResult对象，包含提取到的URL列表和剔除URL信息后的文本
     */
    fun extractMarkdownImageUrls(content: String?): MarkdownImageResult? {
        if (content == null || content.isEmpty()) {
            return null
        }

        // Markdown图片语法的正则表达式：![alt](url)
        // 匹配任意字符（非贪婪）作为alt文本，然后是括号中的URL
        val pattern: Pattern = Pattern.compile("!\\[.*?\\]\\((.*?)\\)")
        val matcher: Matcher = pattern.matcher(content)

        val urls: MutableList<String> = ArrayList()
        val cleanedContent = StringBuffer()

        while (matcher.find()) {
            val url = matcher.group(1)
            if (!url.isNullOrEmpty()) {
                urls.add(url)
            }
            // 替换匹配到的图片语法为空字符串
            matcher.appendReplacement(cleanedContent, "")
        }
        matcher.appendTail(cleanedContent)

        // 如果没有找到任何URL，返回null
        return if (urls.isEmpty()) null else MarkdownImageResult(urls, cleanedContent.toString().trim())
    }

    /**
     * 封装Markdown图片提取结果的内部类
     */
    class MarkdownImageResult(
        private val urls: List<String>,
        private val cleanedContent: String
    ) {
        fun getUrls(): List<String> {
            return urls
        }

        fun getCleanedContent(): String {
            return cleanedContent
        }
    }
}
