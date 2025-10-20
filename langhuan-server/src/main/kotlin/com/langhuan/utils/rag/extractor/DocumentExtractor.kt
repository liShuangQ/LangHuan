package com.langhuan.utils.rag.extractor

import cn.hutool.core.util.IdUtil
import com.langhuan.common.Constant.CACHE_KEY
import com.langhuan.model.domain.TFileUrl
import com.langhuan.service.CacheService
import com.langhuan.service.MinioService
import com.langhuan.service.TFileUrlService
import com.langhuan.utils.other.SecurityUtils
import jakarta.annotation.PostConstruct
import jakarta.annotation.Resource
import lombok.SneakyThrows
import org.apache.poi.xwpf.usermodel.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.ai.document.Document
import org.springframework.ai.reader.tika.TikaDocumentReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.net.HttpURLConnection
import java.net.URI


/**
 * @author Afish
 * @date 2025/7/23 13:42
 */
@Component
class DocumentExtractor(
    private val minioService: MinioService
) {

    companion object {
        private val log = LoggerFactory.getLogger(DocumentExtractor::class.java)
    }

    @Value("\${minio.folder.document-img}")
    private lateinit var minioFolder: String // 在 MinIO 中模拟的"文件夹"前缀

    @Value("\${minio.img-bucket-name}")
    private lateinit var bucketName: String

    @Value("\${minio.url}")
    private lateinit var minioUrl: String

    private lateinit var publicBaseUrl: String // 用于生成图片访问链接

    @Resource
    private lateinit var cacheService: CacheService

    @Resource
    private lateinit var tFileUrlService: TFileUrlService

    @PostConstruct
    fun init() {
        // 确保 bucket 存在
        try {
            minioService.ensureBucketExists(bucketName)
        } catch (e: Exception) {
            log.error("Failed to ensure bucket exists", e)
        }

        publicBaseUrl = minioUrl + "/" + bucketName
        if (!publicBaseUrl.endsWith("/")) {
            publicBaseUrl += "/"
        }
    }

    @Throws(Exception::class)
    fun extract(file: MultipartFile): String {
        if (file.originalFilename != null &&
                file.originalFilename!!.lowercase().endsWith(".docx")) {
            return extractDocxWithImages(file)
        } else {
            return extractWithTika(file)
        }
    }

    @Throws(Exception::class)
    private fun extractWithTika(file: MultipartFile): String {
        val reader = TikaDocumentReader(InputStreamResource(file.inputStream))
        val contentBuilder = StringBuilder()

        for (doc in reader.read()) {
            contentBuilder.append(doc.formattedContent).append("\n")
        }

        var content = contentBuilder.toString()
        if (content.contains("source: Invalid source URI")) {
            content = content.replace(
                    "(?m)^\\s*source: Invalid source URI: InputStream resource.*$", "").trim()
        }
        return content
    }

    @Throws(Exception::class)
    private fun extractDocxWithImages(file: MultipartFile): String {
        val markdownContent = StringBuilder()

        // 生成随机 ID 并存入缓存
        val randomId = IdUtil.getSnowflakeNextId().toInt()
        cacheService.putId(SecurityUtils.getCurrentUsername() + CACHE_KEY, randomId)
        val fileUrlList = mutableListOf<TFileUrl>() // 用于存储图片信息的列表

        file.inputStream.use { inputStream ->
            XWPFDocument(inputStream).use { document ->
                val bodyElements = document.bodyElements
                for (element in bodyElements) {
                    if (element is XWPFParagraph) {
                        processParagraph(element, markdownContent, fileUrlList, randomId) // 修改以包含fileUrlList
                        markdownContent.append("\n")
                    } else if (element is XWPFTable) {
                        processTable(element, markdownContent, fileUrlList, randomId)
                        markdownContent.append("\n\n")
                    }
                }

                // 批量插入数据库
                if (fileUrlList.isNotEmpty()) {
                    tFileUrlService.saveBatch(fileUrlList)
                }
            }
        }
        return markdownContent.toString().trim()
    }

    private fun processParagraph(paragraph: XWPFParagraph, markdownContent: StringBuilder, 
                                fileUrlList: MutableList<TFileUrl>, randomId: Int) {
        for (run in paragraph.runs) {
            val text = run.getText(0)
            if (!text.isNullOrEmpty()) {
                markdownContent.append(text)
            }

            for (picture in run.embeddedPictures) {
                handleImage(picture.pictureData, fileUrlList, randomId).ifPresent { markdownContent.append(it) }
            }
        }
    }

    private fun processTable(table: XWPFTable, markdownContent: StringBuilder, 
                            fileUrlList: MutableList<TFileUrl>, randomId: Int) {
        for (row in table.rows) {
            for (cell in row.tableCells) {
                for (paragraph in cell.paragraphs) {
                    processParagraph(paragraph, markdownContent, fileUrlList, randomId)
                }
                markdownContent.append("\t") // 使用制表符分隔单元格内容
            }
            markdownContent.append("\n") // 每行结束后换行
        }
    }

    @SneakyThrows
    private fun handleImage(pictureData: XWPFPictureData, fileUrlList: MutableList<TFileUrl>, randomId: Int): Optional<String> {
        val extension = pictureData.suggestFileExtension()
        val fileName = UUID.randomUUID().toString() + "." + extension
        val objectName = (minioFolder + "/") + fileName

        try {
            ByteArrayInputStream(pictureData.data).use { inputStream ->
                minioService.handleUpload(objectName, inputStream, pictureData.data.size.toLong(), bucketName)
            }
        } catch (e: Exception) {
            log.error("Failed to upload image to MinIO", e)
            return Optional.empty()
        }

        // 生成可访问的 URL
        val imageUrl = publicBaseUrl + objectName

        // 创建 TFileUrl 实体并添加到列表中
        val fileUrl = TFileUrl()
        fileUrl.fileId = randomId
        fileUrl.fUrl = imageUrl
        fileUrl.fStatus = "临时"
        fileUrlList.add(fileUrl)

        return Optional.of("\n![image]($imageUrl)\n")
    }

    /**
     * 发送 GET 请求获取网页内容
     */
    @Throws(Exception::class)
    private fun sendGetRequest(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        connection.setRequestProperty("User-Agent", "Mozilla/5.0")
        
        return try {
            connection.inputStream.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line).append("\n")
                    }
                    response.toString()
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * 清理文件名，移除不安全字符
     */
    private fun sanitizeFilename(filename: String): String {
        return filename.replace("[^a-zA-Z0-9\\.\\-_]".toRegex(), "_")
    }

    /**
     * 提取 HTML 内容并下载图片到 MinIO
     */
    @Throws(Exception::class)
    fun extract(url: String): String {
        val htmlContent = sendGetRequest(url)

        val dirPath = Paths.get(minioFolder)
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath) // 本地调试用，可选
        }

        val doc = Jsoup.parse(htmlContent)
        val imgElements = doc.select("img")

        // 生成随机 ID 并存入缓存
        val randomId = IdUtil.getSnowflakeNextId().toInt()
        cacheService.putId(SecurityUtils.getCurrentUsername() + CACHE_KEY, randomId)

        // 用于批量插入的列表
        val fileUrlList = mutableListOf<TFileUrl>()

        for (img in imgElements) {
            val src = img.attr("src")
            val alt = if (img.hasAttr("alt")) img.attr("alt") else "image"

            if (src.isEmpty()) continue

            val imageUrl = URL(URL(url), src)
            var filename = sanitizeFilename(imageUrl.path)
            if (!filename.contains(".")) {
                filename += ".jpg" // 默认扩展名
            }
            val objectName = (minioFolder + "/") + filename

            try {
                imageUrl.openStream().use { `in` ->
                    minioService.handleUpload(objectName, `in`, -1, bucketName) // -1 表示未知大小
                }
            } catch (e: Exception) {
                log.error("Failed to upload image from URL to MinIO: {}", imageUrl, e)
                continue
            }

            // 构建公开访问 URL
            val publicImageUrl = publicBaseUrl + objectName

            // 创建 TFileUrl 实体
            val fileUrl = TFileUrl()
            fileUrl.fileId = randomId
            fileUrl.fUrl = publicImageUrl
            fileUrl.fStatus = "临时" // 或 "temp"，根据你的业务定义
            fileUrlList.add(fileUrl)

            val markdownLink = String.format("![%s](%s%s)", alt, publicBaseUrl, objectName)
            img.replaceWith(org.jsoup.nodes.TextNode(markdownLink))
        }

        // 批量插入数据库
        if (fileUrlList.isNotEmpty()) {
            tFileUrlService.saveBatch(fileUrlList)
        }

        return doc.body().text()
    }
}
