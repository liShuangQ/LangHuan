package com.langhuan.service

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.langhuan.dao.TRagFileGroupShareDao
import com.langhuan.model.domain.TRagFileGroupShare
import com.langhuan.model.mapper.TRagFileGroupShareMapper
import com.langhuan.utils.other.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

/**
 * @author lishuangqi
 * @description 针对表【t_rag_file_group_share】的数据库操作Service实现
 * @createDate 2025-07-22 11:35:31
 */
@Service
class TRagFileGroupShareService(
    private val tRagFileGroupShareDao: TRagFileGroupShareDao
) : ServiceImpl<TRagFileGroupShareMapper, TRagFileGroupShare>() {

    companion object {
        private val log = LoggerFactory.getLogger(TRagFileGroupShareService::class.java)
    }

    /**
     * 分享文件组给指定用户
     *
     * @param fileGroupId 文件组ID
     * @param sharedWith  被分享的用户名
     * @param canRead     是否可读
     * @param canAdd      是否可添加
     * @param canUpdate   是否可更新
     * @param canDelete   是否可删除
     * @return 是否分享成功
     */
    fun shareFileGroup(
        fileGroupId: Int,
        sharedWith: String,
        canRead: Boolean?,
        canAdd: Boolean?,
        canUpdate: Boolean?,
        canDelete: Boolean?
    ): Boolean {
        TRagFileGroupShareService.log.info(
            "Sharing file group {} to user {} with permissions: read={}, add={}, update={}, delete={}",
            fileGroupId, sharedWith, canRead, canAdd, canUpdate, canDelete
        )

        val currentUser = SecurityUtils.getCurrentUsername()

        // 检查是否已经分享过
        val existingShare = this.getOne(
            QueryWrapper<TRagFileGroupShare>()
                .eq("fileGroupId", fileGroupId)
                .eq("sharedWith", sharedWith)
        )

        return if (existingShare != null) {
            // 更新现有分享权限
            existingShare.canRead = canRead
            existingShare.canAdd = canAdd
            existingShare.canUpdate = canUpdate
            existingShare.canDelete = canDelete
            existingShare.sharedAt = Date()
            this.updateById(existingShare)
        } else {
            // 创建新的分享记录
            val share = TRagFileGroupShare()
            share.fileGroupId = fileGroupId
            share.sharedWith = sharedWith
            share.canRead = canRead
            share.canAdd = canAdd
            share.canUpdate = canUpdate
            share.canDelete = canDelete
            share.sharedBy = currentUser
            share.sharedAt = Date()
            this.save(share)
        }
    }

    /**
     * 批量分享文件组给指定用户列表
     *
     * @param fileGroupId 文件组ID
     * @param sharedWithList 被分享的用户名列表
     * @param canRead     是否可读
     * @param canAdd      是否可添加
     * @param canUpdate   是否可更新
     * @param canDelete   是否可删除
     * @return 成功分享的用户数量
     */
    fun shareFileGroupBatch(
        fileGroupId: Int,
        sharedWithList: List<String>,
        canRead: Boolean?,
        canAdd: Boolean?,
        canUpdate: Boolean?,
        canDelete: Boolean?
    ): Int {
        TRagFileGroupShareService.log.info(
            "Batch sharing file group {} to users {} with permissions: read={}, add={}, update={}, delete={}",
            fileGroupId, sharedWithList, canRead, canAdd, canUpdate, canDelete
        )

        if (sharedWithList.isEmpty()) {
            return 0
        }

        var successCount = 0
        for (sharedWith in sharedWithList) {
            if (!sharedWith.trim().isEmpty()) {
                val success = shareFileGroup(fileGroupId, sharedWith.trim(), canRead, canAdd, canUpdate, canDelete)
                if (success) {
                    successCount++
                }
            }
        }

        TRagFileGroupShareService.log.info(
            "Batch sharing completed. Successfully shared to {}/{} users",
            successCount,
            sharedWithList.size
        )
        return successCount
    }

    /**
     * 取消分享文件组（支持单个或批量用户）
     *
     * @param fileGroupId 文件组ID
     * @param sharedWithList 被分享的用户名列表
     * @return 成功取消分享的数量
     */
    fun unshareFileGroup(fileGroupId: Int, sharedWithList: List<String>): Int {
        TRagFileGroupShareService.log.info("Unsharing file group {} from users {}", fileGroupId, sharedWithList)

        if (sharedWithList.isEmpty()) {
            return 0
        }

        var successCount = 0
        // 分批处理，每批20个
        val batchSize = 20
        for (i in sharedWithList.indices step batchSize) {
            val endIndex = minOf(i + batchSize, sharedWithList.size)
            val batchUsers = sharedWithList.subList(i, endIndex)

            val batchResult = this.remove(
                QueryWrapper<TRagFileGroupShare>()
                    .eq("fileGroupId", fileGroupId)
                    .`in`("sharedWith", batchUsers)
            )

            if (batchResult) {
                successCount += batchUsers.size
            }

            TRagFileGroupShareService.log.info("Processed batch {}-{}, batch result: {}", i, endIndex - 1, batchResult)
        }

        return successCount
    }

    /**
     * 获取文件组的分享列表（委托给Dao层处理）
     *
     * @param fileGroupId 文件组ID
     * @param sharedWith  被分享的用户名
     * @return 分享列表（包含用户名信息）
     */
    fun getFileGroupShares(fileGroupId: Int, sharedWith: String?): List<Map<String, Any>> {
        return tRagFileGroupShareDao.getFileGroupShares(fileGroupId, sharedWith)
    }
}
