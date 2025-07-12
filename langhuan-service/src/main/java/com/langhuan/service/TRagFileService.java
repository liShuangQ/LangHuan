package com.langhuan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.langhuan.common.BusinessException;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.domain.TRagFileGroup;
import com.langhuan.model.mapper.TRagFileMapper;
import com.langhuan.utils.pagination.JdbcPaginationHelper;
import com.langhuan.utils.rag.RagFileVectorUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.core.io.InputStreamResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private final JdbcPaginationHelper paginationHelper;

    public TRagFileService(TRagFileGroupService ragFileGroupService, JdbcTemplate jdbcTemplate,
            RagFileVectorUtils ragFileVectorUtils, JdbcPaginationHelper paginationHelper) {
        this.ragFileGroupService = ragFileGroupService;
        this.baseDao = jdbcTemplate;
        this.ragFileVectorUtils = ragFileVectorUtils;
        this.paginationHelper = paginationHelper;
    }

    /**
     * 查询文件列表
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
        // 根据文件组名查找文件组ID
        String fileGroupId = "";
        if (StringUtils.hasText(fileGroupName)) {
            List<TRagFileGroup> list = ragFileGroupService
                    .list(new LambdaQueryWrapper<TRagFileGroup>().like(TRagFileGroup::getGroupName, fileGroupName));
            if (!list.isEmpty()) {
                fileGroupId = list.get(0).getId().toString();
            }
        }

        // 构建查询条件
        JdbcPaginationHelper.QueryCondition condition = new JdbcPaginationHelper.QueryCondition()
                .like("f.file_name", fileName.isEmpty() ? null : fileName)
                .like("f.file_type", fileType.isEmpty() ? null : fileType)
                .eq("f.file_group_id", fileGroupId.isEmpty() ? null : fileGroupId);

        // 构建SQL查询语句
        String sql = """
                SELECT f.*,
                u.name as "userName"
                FROM t_rag_file f
                LEFT JOIN t_user u ON f.uploaded_by = u.username
                """ + condition.getWhereClause() + " ORDER BY f.uploaded_at DESC";

        // 执行分页查询
        return paginationHelper.selectPageForMap(sql, condition.getParams(), pageNum, pageSize);
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
