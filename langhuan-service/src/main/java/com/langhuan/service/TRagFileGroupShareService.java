package com.langhuan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.dao.TRagFileGroupShareDao;
import com.langhuan.model.domain.TRagFileGroupShare;
import com.langhuan.model.mapper.TRagFileGroupShareMapper;
import com.langhuan.utils.other.SecurityUtils;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author lishuangqi
 * @description 针对表【t_rag_file_group_share】的数据库操作Service实现
 * @createDate 2025-07-22 11:35:31
 */
@Slf4j
@Service
public class TRagFileGroupShareService extends ServiceImpl<TRagFileGroupShareMapper, TRagFileGroupShare> {

    private final TRagFileGroupShareDao tRagFileGroupShareDao;

    public TRagFileGroupShareService(TRagFileGroupShareDao tRagFileGroupShareDao) {
        this.tRagFileGroupShareDao = tRagFileGroupShareDao;
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
    public boolean shareFileGroup(Integer fileGroupId, String sharedWith,
            Boolean canRead, Boolean canAdd,
            Boolean canUpdate, Boolean canDelete) {
        log.info("Sharing file group {} to user {} with permissions: read={}, add={}, update={}, delete={}",
                fileGroupId, sharedWith, canRead, canAdd, canUpdate, canDelete);

        String currentUser = SecurityUtils.getCurrentUsername();

        // 检查是否已经分享过
        TRagFileGroupShare existingShare = this.getOne(
                new LambdaQueryWrapper<TRagFileGroupShare>()
                        .eq(TRagFileGroupShare::getFileGroupId, fileGroupId)
                        .eq(TRagFileGroupShare::getSharedWith, sharedWith));

        if (existingShare != null) {
            // 更新现有分享权限
            existingShare.setCanRead(canRead);
            existingShare.setCanAdd(canAdd);
            existingShare.setCanUpdate(canUpdate);
            existingShare.setCanDelete(canDelete);
            existingShare.setSharedAt(new Date());
            return this.updateById(existingShare);
        } else {
            // 创建新的分享记录
            TRagFileGroupShare share = new TRagFileGroupShare();
            share.setFileGroupId(fileGroupId);
            share.setSharedWith(sharedWith);
            share.setCanRead(canRead);
            share.setCanAdd(canAdd);
            share.setCanUpdate(canUpdate);
            share.setCanDelete(canDelete);
            share.setSharedBy(currentUser);
            share.setSharedAt(new Date());
            return this.save(share);
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
    public int shareFileGroupBatch(Integer fileGroupId, List<String> sharedWithList,
            Boolean canRead, Boolean canAdd,
            Boolean canUpdate, Boolean canDelete) {
        log.info("Batch sharing file group {} to users {} with permissions: read={}, add={}, update={}, delete={}",
                fileGroupId, sharedWithList, canRead, canAdd, canUpdate, canDelete);

        if (sharedWithList == null || sharedWithList.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (String sharedWith : sharedWithList) {
            if (sharedWith != null && !sharedWith.trim().isEmpty()) {
                boolean success = shareFileGroup(fileGroupId, sharedWith.trim(), canRead, canAdd, canUpdate, canDelete);
                if (success) {
                    successCount++;
                }
            }
        }

        log.info("Batch sharing completed. Successfully shared to {}/{} users", successCount, sharedWithList.size());
        return successCount;
    }

    /**
     * 取消分享文件组（支持单个或批量用户）
     * 
     * @param fileGroupId 文件组ID
     * @param sharedWithList 被分享的用户名列表
     * @return 成功取消分享的数量
     */
    public int unshareFileGroup(Integer fileGroupId, List<String> sharedWithList) {
        log.info("Unsharing file group {} from users {}", fileGroupId, sharedWithList);
        
        if (sharedWithList == null || sharedWithList.isEmpty()) {
            return 0;
        }
        
        int successCount = 0;
        // 分批处理，每批20个
        int batchSize = 20;
        for (int i = 0; i < sharedWithList.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, sharedWithList.size());
            List<String> batchUsers = sharedWithList.subList(i, endIndex);
            
            boolean batchResult = this.remove(
                    new LambdaQueryWrapper<TRagFileGroupShare>()
                            .eq(TRagFileGroupShare::getFileGroupId, fileGroupId)
                            .in(TRagFileGroupShare::getSharedWith, batchUsers));
            
            if (batchResult) {
                successCount += batchUsers.size();
            }
            
            log.info("Processed batch {}-{}, batch result: {}", i, endIndex - 1, batchResult);
        }
        
        return successCount;
    }

    /**
     * 获取文件组的分享列表（委托给Dao层处理）
     * 
     * @param fileGroupId 文件组ID
     * @param sharedWith  被分享的用户名
     * @return 分享列表（包含用户名信息）
     */
    public List<Map<String, Object>> getFileGroupShares(Integer fileGroupId, String sharedWith) {
        return tRagFileGroupShareDao.getFileGroupShares(fileGroupId, sharedWith);
    }

}
