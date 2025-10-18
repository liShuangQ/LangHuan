package com.langhuan.utils.minio

import com.langhuan.config.WebLogInterceptor
import io.minio.*
import io.minio.errors.ErrorResponseException
import io.minio.messages.DeleteError
import io.minio.messages.DeleteObject
import io.minio.messages.Item
import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.util.*

class MinioUtils(private val minioClient: MinioClient, private val minioUrl: String?) {
    companion object {
        private val log = LoggerFactory.getLogger(MinioUtils::class.java)
    }
    @Throws(Exception::class)
    fun createBucket(bucketName: String?) {
        val found =
            minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build())
        }
    }

    @Throws(Exception::class)
    fun uploadFile(objectName: String?, inputStream: InputStream?, size: Long, bucketName: String?) {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .stream(inputStream, size, (10 * 1024 * 1024).toLong())
                .build()
        )
    }

    /**
     * 上传MultipartFile格式的文件到MinIO
     *
     * @param objectName 文件对象名称（即路径+文件名）
     * @param file       MultipartFile格式的文件
     * @param bucketName 存储桶名称
     * @throws Exception 上传过程中可能抛出的异常
     */
    @Throws(Exception::class)
    fun uploadFile(objectName: String?, file: MultipartFile, bucketName: String?) {
        file.getInputStream().use { inputStream ->
            uploadFile(objectName, inputStream, file.getSize(), bucketName)
        }
    }

    @Throws(Exception::class)
    fun downloadFile(objectName: String?, bucketName: String?): InputStream? {
        return minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .build()
        )
    }

    @Throws(Exception::class)
    fun deleteFile(objectName: String?, bucketName: String?) {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucketName)
                .`object`(objectName)
                .build()
        )
    }

    // 生成minio访问链接
    fun generateMinioUrl(objectName: String?, bucketName: String?): String {
        return minioUrl + "/" + bucketName + "/" + objectName
    }

    /**
     * 从 URL 中提取 MinIO 的 objectName
     */
    fun extractObjectName(url: String?, bucketName: String?): String? {
        if (url == null || url.isEmpty()) return null
        try {
            val baseUrl = minioUrl + "/" + bucketName + "/"
            if (url.startsWith(baseUrl)) {
                return url.substring(baseUrl.length)
            }
            log.warn("Unrecognized MinIO URL format: {}", url)
            return null
        } catch (e: Exception) {
            log.error("Error extracting object name from URL: {}", url, e)
            return null
        }
    }

    /**
     * 判断指定 objectName 的文件是否存在于 MinIO 的 bucket 中
     *
     * @param objectName 文件对象名称（即路径+文件名）
     * @return true 如果文件存在，false 不存在或发生异常
     */
    fun fileExists(objectName: String?, bucketName: String?): Boolean {
        if (objectName == null || objectName.isEmpty()) {
            log.warn("Object name is null or empty.")
            return false
        }

        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )
            return true // 能成功获取元信息，说明文件存在
        } catch (e: Exception) {
            // 文件不存在会抛出异常（如 404），其他可能是网络等问题
            if (e.message!!.contains("404") || e is ErrorResponseException) {
                log.debug("File not found in MinIO: {}", objectName)
                return false
            }
            log.error("Error occurred while checking file existence: {}", objectName, e)
            return false
        }
    }

    /**
     * 删除文件夹及其所有内容
     */
    @Throws(Exception::class)
    fun deleteFolder(folderName: String, bucketName: String?) {
        var folderName = folderName
        require(!(folderName == null || folderName.isEmpty())) { "Folder name cannot be null or empty" }

        // 确保文件夹名以 / 结尾，表示一个目录
        if (!folderName.endsWith("/")) {
            folderName += "/"
        }

        // 列出该文件夹下的所有对象
        val objectsToDelete: MutableList<DeleteObject?> = LinkedList<DeleteObject?>()
        val results = minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(folderName)
                .recursive(true)
                .build()
        )

        for (result in results) {
            val item: Item = result.get()!!
            objectsToDelete.add(DeleteObject(item.objectName()))
        }

        // 批量删除所有对象
        if (!objectsToDelete.isEmpty()) {
            val deleteErrors = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                    .bucket(bucketName)
                    .objects(objectsToDelete)
                    .build()
            )

            // 检查是否有删除错误
            for (deleteError in deleteErrors) {
                val error: DeleteError = deleteError.get()!!
                log.error("Error deleting object: {} - {}", error.objectName(), error.message())
            }
        }
    }
}