package com.langhuan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.common.BusinessException;
import com.langhuan.dao.TRagFileDao;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.domain.TRagFileGroup;
import com.langhuan.model.mapper.TRagFileMapper;
import com.langhuan.utils.rag.EtlPipeline;
import com.langhuan.utils.rag.RagFileVectorUtils;
import com.langhuan.utils.other.SecurityUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

/**
 * @author lishuangqi
 * @description 针对表【t_rag_file】的数据库操作Service实现
 * @createDate 2025-03-15 13:41:30
 */
@Service
@Slf4j
public class TRagFileService extends ServiceImpl<TRagFileMapper, TRagFile> {
    private final TRagFileGroupService ragFileGroupService;
    private final TRagFileDao ragFileDao;
    private final EtlPipeline etlPipeline;

    public TRagFileService(TRagFileGroupService ragFileGroupService, TRagFileDao ragFileDao,
            RagFileVectorUtils ragFileVectorUtils, EtlPipeline etlPipeline) {
        this.ragFileGroupService = ragFileGroupService;
        this.ragFileDao = ragFileDao;
        this.etlPipeline = etlPipeline;
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
    public IPage<Map<String, Object>> queryFiles(String fileName, String fileType, String fileGroupName, int pageNum,
            int pageSize) {
        String currentUser = SecurityUtils.getCurrentUsername();
        boolean isAdmin = SecurityUtils.hasAdminRole();

        if (isAdmin) {
            // 超级管理员查询所有文件
            return ragFileDao.queryFilesForAdmin(fileName, fileType, fileGroupName, pageNum, pageSize);
        } else {
            // 普通用户查询文件（根据文件组权限）
            return ragFileDao.queryFilesForUser(currentUser, fileName, fileType, fileGroupName, pageNum, pageSize);
        }
    }

    /**
     * 根据文件ID生成文档流
     * 
     * @param fileId 文件ID
     * @return 文档输入流资源
     */
    public InputStreamResource generateDocumentStreamByFileId(Integer fileId) {
        if (fileId == null) {
            throw new BusinessException("fileId不能为空");
        }
        List<Map<String, Object>> queryForList = ragFileDao.queryVectorContentByFileId(fileId);
        if (queryForList.isEmpty()) {
            throw new BusinessException("未找到相关文档");
        }
        return etlPipeline.exportChunks(queryForList.stream()
                .map(map -> map.get("content").toString())
                .collect(java.util.stream.Collectors.toList()));
    }
}
