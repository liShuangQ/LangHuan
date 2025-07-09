package com.langhuan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.common.BusinessException;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.domain.TRagFileGroup;
import com.langhuan.model.mapper.TRagFileMapper;
import com.langhuan.utils.rag.RagFileVectorUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * @author lishuangqi
 * @description 针对表【t_rag_file】的数据库操作Service实现
 * @createDate 2025-03-15 13:41:30
 */
@Service
@Slf4j
public class TRagFileService extends ServiceImpl<TRagFileMapper, TRagFile> {
    TRagFileGroupService ragFileGroupService;
    JdbcTemplate baseDao;
    RagFileVectorUtils ragFileVectorUtils;

    public TRagFileService(TRagFileGroupService ragFileGroupService, JdbcTemplate jdbcTemplate,
            RagFileVectorUtils ragFileVectorUtils) {
        this.ragFileGroupService = ragFileGroupService;
        this.baseDao = jdbcTemplate;
        this.ragFileVectorUtils = ragFileVectorUtils;
    }

    public Page<TRagFile> queryFiles(String fileName, String fileType, String fileGroupName, int pageNum,
            int pageSize) {
        String fileGroupId = "";

        if (!fileGroupName.isEmpty()) {
            List<TRagFileGroup> list = ragFileGroupService
                    .list(new LambdaQueryWrapper<TRagFileGroup>().like(TRagFileGroup::getGroupName, fileGroupName));
            if (list.size() > 0) {
                fileGroupId = list.get(0).getId().toString();
            }
        }
        return super.page(new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<TRagFile>()
                        .like(!fileName.isEmpty(), TRagFile::getFileName, fileName)
                        .like(!fileType.isEmpty(), TRagFile::getFileType, fileType)
                        .eq(!fileGroupId.isEmpty(), TRagFile::getFileGroupId, fileGroupId)
                        .orderBy(true, false, TRagFile::getUploadedAt));
    }

    public InputStreamResource generateDocumentStreamByFileId(Integer fileId) {
        if (fileId == null) {
            throw new BusinessException("fileId不能为空");
        }
        String sql = "SELECT content FROM vector_store_rag WHERE metadata ->> 'fileId' = ? ;";
        List<Map<String, Object>> queryForList = baseDao.queryForList(sql, fileId.toString());
        if (queryForList.isEmpty()) {
            throw new BusinessException("未找到相关文档");
        }
        return ragFileVectorUtils.generateDocumentStreamByFileId(queryForList.stream()
                .map(map -> map.get("content").toString())
                .collect(java.util.stream.Collectors.toList()));
    }
}
