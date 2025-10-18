package com.langhuan.service

import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.langhuan.common.BusinessException
import com.langhuan.dao.TRagFileDao
import com.langhuan.model.domain.TRagFile
import com.langhuan.model.mapper.TRagFileMapper
import com.langhuan.utils.other.SecurityUtils
import com.langhuan.utils.rag.EtlPipeline
import com.langhuan.utils.rag.RagFileVectorUtils
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Service

/**
 * @author lishuangqi
 * @description 针对表【t_rag_file】的数据库操作Service实现
 * @createDate 2025-03-15 13:41:30
 */
@Service
class TRagFileService(
    private val ragFileGroupService: TRagFileGroupService,
    private val ragFileDao: TRagFileDao,
    ragFileVectorUtils: RagFileVectorUtils,
    private val etlPipeline: EtlPipeline
) : ServiceImpl<TRagFileMapper, TRagFile>() {
    
    companion object {
        private val log = LoggerFactory.getLogger(TRagFileService::class.java)
    }

    /**
     * 查询文件列表（带权限控制）
     *
     * @param fileName      文件名（模糊查询）
     * @param fileType      文件类型（模糊查询）
     * @param fileGroupName 文件组名（模糊查询）
     * @param pageNum       页码
     * @param pageSize      每页大小
     * @return 分页结果
     */
    fun queryFiles(
        fileName: String?, 
        fileType: String?, 
        fileGroupName: String?, 
        pageNum: Int,
        pageSize: Int
    ): IPage<Map<String, Any>> {
        val currentUser = SecurityUtils.getCurrentUsername()
        val isAdmin = SecurityUtils.hasAdminRole()

        return if (isAdmin) {
            // 超级管理员查询所有文件
            ragFileDao.queryFilesForAdmin(fileName, fileType, fileGroupName, pageNum, pageSize)
        } else {
            // 普通用户查询文件（根据文件组权限）
            ragFileDao.queryFilesForUser(currentUser as String, fileName, fileType, fileGroupName, pageNum, pageSize)
        }
    }

    /**
     * 根据文件ID生成文档流
     *
     * @param fileId 文件ID
     * @return 文档输入流资源
     */
    fun generateDocumentStreamByFileId(fileId: Int?): InputStreamResource {
        if (fileId == null) {
            throw BusinessException("fileId不能为空")
        }
        val queryForList = ragFileDao.queryVectorContentByFileId(fileId)
        if (queryForList.isEmpty()) {
            throw BusinessException("未找到相关文档")
        }
        return etlPipeline.exportChunks(queryForList.stream()
                .map { map: Map<String, Any> -> map["content"].toString() }
                .collect(java.util.stream.Collectors.toList()))
    }

    /**
     * 获取所有需要导出的 fileId 列表
     */
    fun getAllFileIdsForExport(): List<Int> {
        return ragFileDao.queryAllFileIds() // 返回 List<Integer>
    }
}
