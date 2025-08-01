package com.langhuan.serviceai;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.langhuan.common.BusinessException;
import com.langhuan.common.Constant;
import com.langhuan.config.VectorStoreConfig;
import com.langhuan.model.domain.TFileUrl;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.service.CacheService;
import com.langhuan.service.MinioService;
import com.langhuan.service.TFileUrlService;
import com.langhuan.service.TRagFileService;
import com.langhuan.utils.other.SecurityUtils;
import com.langhuan.utils.rag.EtlPipeline;
import com.langhuan.utils.rag.RagFileVectorUtils;
import com.langhuan.utils.rag.config.SplitConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.langhuan.common.Constant.CACHE_KEY;

@Service
@Slf4j
public class RagService {

    private final TRagFileService ragFileService;
    private final JdbcTemplate baseDao;
    private final VectorStore ragVectorStore;
    private final ReRankModelService reRankModelService;
    private final EtlPipeline etlPipeline;
    private final TFileUrlService tFileUrlService;

    @Resource
    private CacheService cacheService;

    @Resource
    private MinioService minioService;

    public RagService(TRagFileService ragFileService, JdbcTemplate jdbcTemplate,
                      VectorStoreConfig vectorStoreConfig, ReRankModelService reRankModelService, EtlPipeline etlPipeline, TFileUrlService tFileUrlService) {
        this.ragFileService = ragFileService;
        this.baseDao = jdbcTemplate;
        this.ragVectorStore = vectorStoreConfig.ragVectorStore();
        this.reRankModelService = reRankModelService;
        this.etlPipeline = etlPipeline;
        this.tFileUrlService = tFileUrlService;
    }

    @Transactional(rollbackFor = Exception.class)
    public String changeDocumentText(String documents, String documentId, TRagFile ragFile) throws Exception {
        log.info("Updating changeDocumentText: {},documentId: {},documents: {}", ragFile, documentId, documents);
        String sql = "DELETE FROM vector_store_rag WHERE id = ?::uuid";
        baseDao.update(sql, documentId);
        if (etlPipeline.writeToVectorStore(List.of(documents), etlPipeline.getMetadataFactory().createMetadata(ragFile),
                ragVectorStore)) {
            return "更新成功";
        } else {
            return "更新失败，请检查日志。";
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String changeDocumentTextByString(String documents, String documentId) throws Exception {
        log.info("Updating changeDocumentTextByString: {}, documentId: {}, documents: {}", documentId, documents);

        String seqsql = "SELECT * FROM vector_store_rag WHERE id = ?::uuid";
        List<Map<String, Object>> queryForList = baseDao.queryForList(seqsql, documentId);
        PGobject pgObject = (PGobject) queryForList.get(0).get("metadata");
        String string = pgObject.getValue(); // 提取 JSON 字符串
        JSONObject object = JSONUtil.parseObj(string);
        String fileName = object.getStr("filename");
        String fileId = object.getStr("fileId");
        String groupId = object.getStr("groupId");

        String delsql = "DELETE FROM vector_store_rag WHERE id = ?::uuid";
        baseDao.update(delsql, documentId);
        if (etlPipeline.writeToVectorStore(List.of(documents),
                etlPipeline.getMetadataFactory().createMetadata(fileName, fileId, groupId),
                ragVectorStore)) {
            return "更新成功";
        } else {
            return "更新失败，请检查日志。";
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String deleteDocumentText(String documentId, TRagFile ragFile) {
        log.info("Deleting document text: {}, documentId: {}", ragFile, documentId);

        String sqlDeleteVectorStore = "DELETE FROM vector_store_rag WHERE id = ?::uuid";
        baseDao.update(sqlDeleteVectorStore, documentId);

        String sqlSelectTFileUrls = "SELECT id, f_url FROM t_file_url WHERE file_id = ?";
        List<TFileUrl> fileUrlList = baseDao.queryForList(sqlSelectTFileUrls, new Object[]{ragFile.getId()}, TFileUrl.class);

        if (fileUrlList != null && !fileUrlList.isEmpty()) {
            deleteImage(fileUrlList);

            String sqlDeleteTFileUrls = "DELETE FROM t_file_url WHERE file_id = ?";
            int deleteCount = baseDao.update(sqlDeleteTFileUrls, ragFile.getId());
            log.info("Deleted {} records from t_file_url", deleteCount);
        }

        Integer newDocNum = Math.max(0, Integer.parseInt(ragFile.getDocumentNum()) - 1);
        ragFile.setDocumentNum(newDocNum.toString());
        boolean updated = ragFileService.updateById(ragFile);
        if (!updated) {
            log.warn("Failed to update ragFile: {}", ragFile.getId());
            throw new RuntimeException("更新文件记录失败");
        }

        return "删除成功";
    }

    public List<String> readAndSplitDocument(MultipartFile file, SplitConfig splitConfig) throws Exception {
        return etlPipeline.process(file, splitConfig);
    }

    public List<String> readAndSplitDocument(String url, SplitConfig splitConfig) throws Exception {
        if (url.startsWith("http")) {
            return etlPipeline.process(url, splitConfig);
        }
        return etlPipeline.process(url, splitConfig, false);

    }

    @Transactional(rollbackFor = Exception.class)
    public String writeDocumentsToVectorStore(List<String> documents, TRagFile ragFile) throws Exception {
        log.info("writeDocumentsToVectorStore: {}", ragFile);

        boolean isInsert = ragFile.getId() == null || ragFile.getId() == 0 || ragFile.getId().toString().isEmpty();
        if (isInsert) {
            ragFile.setId((int) IdUtil.getSnowflakeNextId());
        }
        ragFile.setUploadedAt(new java.util.Date());
        ragFile.setUploadedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        // 写入向量库
        boolean writeSuccess = etlPipeline.writeToVectorStore(
                documents,
                etlPipeline.getMetadataFactory().createMetadata(ragFile),
                ragVectorStore
        );

        if (!writeSuccess) {
            return "添加失败，请检查日志。";
        }

        try {
            // 1. 从缓存中获取 extract 阶段生成的临时 fileId
            Integer tempFileId = cacheService.getId(SecurityUtils.getCurrentUsername() + CACHE_KEY);
            if (tempFileId == null) {
                log.warn("未找到缓存的临时 file_id，跳过 t_file_url 更新");
                return "添加成功，但未更新图片状态";
            }

            // 2. 使用 SQL 批量更新 t_file_url 表
            String updateSql = """
                                  UPDATE t_file_url
                                  SET file_id = ?, f_status = ?
                                  WHERE file_id = ? AND f_status = ?
            """;

            int updatedRows = baseDao.update(updateSql,
                    ragFile.getId(),     // 新的 file_id
                    "在用",              // 新状态
                    tempFileId,          // 旧的临时 file_id
                    "临时"               // 原状态
            );

            log.info("批量更新 t_file_url，影响行数: {}, file_id: {} -> {}", updatedRows, tempFileId, ragFile.getId());

            // 3. 清理缓存
            cacheService.removeId(SecurityUtils.getCurrentUsername() + CACHE_KEY);

        } catch (Exception e) {
            log.error("执行 t_file_url 批量更新时出错", e);
            throw new Exception("更新图片引用状态失败", e); // 触发事务回滚
        }

        // 4. 保存或更新 ragFile 记录
        if (isInsert) {
            ragFileService.save(ragFile);
            log.info("保存新文件记录: {}", ragFile.getId());
        } else {
            ragFileService.updateById(ragFile);
            log.info("更新已有文件记录: {}", ragFile.getId());
        }

        return "添加成功";
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteFileAndDocuments(Integer id) {
        log.info("Deleting file and documents with ID: {}", id);

        String sqlDeleteVectorStore = """
                        DELETE FROM vector_store_rag
                        WHERE metadata ->> 'fileId' = ?;
                """;
        int deletedRowsFromVectorStore = baseDao.update(sqlDeleteVectorStore, id.toString());
        log.info("Deleted {} rows from vector_store_rag", deletedRowsFromVectorStore);

        List<TFileUrl> fileUrlList = tFileUrlService.lambdaQuery()
                .eq(TFileUrl::getFileId, id)
                .select(TFileUrl::getFUrl, TFileUrl::getId) // 只选择需要的字段
                .list();

        if (fileUrlList != null && !fileUrlList.isEmpty()) {
            deleteImage(fileUrlList);

            List<Integer> idsToDelete = fileUrlList.stream()
                    .map(TFileUrl::getId)
                    .collect(Collectors.toList());
            boolean deletedFromTFileUrl = tFileUrlService.removeByIds(idsToDelete);
            if (!deletedFromTFileUrl) {
                log.warn("Failed to delete records from t_file_url");
                throw new RuntimeException("删除 t_file_url 记录失败");
            }
            log.info("Deleted {} records from t_file_url", idsToDelete.size());
        }

        boolean deletedRagFile = ragFileService.removeById(id);
        if (!deletedRagFile) {
            log.warn("Failed to delete TRagFile record with ID: {}", id);
            throw new RuntimeException("删除 TRagFile 记录失败");
        }

        return true;
    }

    private void deleteImage(List<TFileUrl> fileUrlList) {
        List<String> urlsToDelete = fileUrlList.stream()
                .map(TFileUrl::getFUrl)
                .filter(url -> url != null && !url.trim().isEmpty())
                .toList();

        for (String url : urlsToDelete) {
            try {
                String objectName = minioService.extractObjectName(url); // 使用之前的提取方法
                if (objectName != null) {
                    minioService.handleDelete(objectName);
                    log.info("MinIO object deleted: {}", objectName);
                }
            } catch (Exception e) {
                log.error("Failed to delete MinIO object: {}", url, e);
                throw new RuntimeException("删除 MinIO 文件失败: " + url, e); // 触发事务回滚
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String changeFileAndDocuments(TRagFile ragFile) {
        log.info("Updating changeFileAndDocuments: {}", ragFile);
        String sql = """
                        UPDATE vector_store_rag
                        SET metadata = jsonb_set(metadata::jsonb, '{groupId}', to_jsonb(?))
                        WHERE metadata ->> 'fileId' = ?;
                """;
        baseDao.update(sql, ragFile.getFileGroupId(), ragFile.getId().toString());
        ragFileService.updateById(ragFile);
        return "更新成功";
    }

    public String changeDocumentsRank(String documentId, Integer rank) {
        log.info("Updating changeDocumentsRank: {} rank: {}", documentId, rank);
        String sql = """
                        UPDATE vector_store_rag
                        SET metadata = jsonb_set(metadata::jsonb, '{rank}', to_jsonb(?))
                        WHERE id = ?::uuid;
                """;
        baseDao.update(sql, rank, documentId);
        return "更新成功";
    }

    public Map<String, Object> queryDocumentsByFileId(Integer fileId, String content, int pageNum, int pageSize) {
        if (fileId == null) {
            throw new BusinessException("fileId不能为空");
        }
        log.info("queryDocumentsByFileId: {}", fileId);
        String sql = """
                        SELECT * FROM vector_store_rag WHERE metadata ->> 'fileId' = ? AND content LIKE ?
                        LIMIT ? OFFSET ?;
                """;
        String count = """
                        SELECT COUNT(*) FROM vector_store_rag WHERE metadata ->> 'fileId' = ? AND content LIKE ?;
                """;
        return Map.of("list",
                baseDao.queryForList(sql, fileId.toString(), "%" + content + "%", pageSize, (pageNum - 1) * pageSize),
                "count", baseDao.queryForObject(count, Integer.class, fileId.toString(), "%" + content + "%"));
    }

    public List<Map<String, Object>> queryDocumentsByIds(String fileIds) {
        String[] fileIdsArray = fileIds.split(",");
        if (fileIdsArray.length == 0) {
            throw new BusinessException("fileId不能为空");
        }
        if (fileIdsArray.length > 10) {
            throw new BusinessException("fileId数量不能超过10");
        }

        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT vs.id,vs.content,fg.group_name AS groupName,vs.metadata->>'filename' AS filename FROM vector_store_rag vs LEFT JOIN t_rag_file_group fg ON vs.metadata->>'groupId' = CAST(fg.id AS TEXT) WHERE vs.id IN (");
        for (int i = 0; i < fileIdsArray.length; i++) {
            sqlBuilder.append("?::uuid");
            if (i < fileIdsArray.length - 1) {
                sqlBuilder.append(",");
            }
        }
        sqlBuilder.append(");");
        String sql = sqlBuilder.toString();
        Object[] params = new Object[fileIdsArray.length];
        for (int i = 0; i < fileIdsArray.length; i++) {
            params[i] = fileIdsArray[i];
        }
        // 执行查询
        log.info("queryDocumentsByFileIds: {}", fileIds);
        List<Map<String, Object>> queryForList = baseDao.queryForList(sql, params);
        queryForList.forEach(map -> {
            map.put("metadata", map.get("metadata"));
        });
        return queryForList;
    }

    public List<Document> ragSearch(String q, String groupId, String fileId, Boolean isReRank) throws Exception {
        List<Document> searchDocuments = new ArrayList<>();
        isReRank = isReRank == null ? Constant.ISRAGRERANK : isReRank;
        try {
            if (groupId.isEmpty()) {
                searchDocuments = ragVectorStore.similaritySearch(
                        SearchRequest.builder().query(q).topK(Constant.RAGWITHTOPK)
                                .similarityThreshold(Constant.RAGWITHSIMILARITYTHRESHOLD).build() // 单独设置多一些
                );
            } else {
                // HACK:一个组下有相同的文件ID再用 groupId == '" + groupId + "' AND
                String sql = fileId.isEmpty() ? "groupId == '" + groupId + "'" : "fileId == '" + fileId + "'";
                searchDocuments = ragVectorStore.similaritySearch(
                        SearchRequest.builder().query(q).topK(Constant.RAGWITHTOPK)
                                .similarityThreshold(Constant.RAGWITHSIMILARITYTHRESHOLD)
                                .filterExpression(sql)// 设置过滤条件
                                .build());
            }
        } catch (Exception e) {
            log.error("ragSearch error: {}", e.getMessage());
            throw new BusinessException("查询失败");
        }
        if ("numFilter".equals(Constant.RAGRANKMODULETYPE)) {
            searchDocuments = rank_numFilter(searchDocuments, q, isReRank);
        }
        if ("linearWeighting".equals(Constant.RAGRANKMODULETYPE)) {
            searchDocuments = rank_linearWeighting(searchDocuments, q, isReRank);
        }
        // 最后结果一定是LLM_RAG_TOPN的数量
        return searchDocuments.subList(0, Math.min(Constant.LLM_RAG_TOPN, searchDocuments.size()));
    }

    /**
     * 匹配度得分 → rerank 模型距离 → 手工标记排名
     */
    public List<Document> rank_numFilter(List<Document> searchDocuments, String q, Boolean isReRank) throws Exception {
        // 在得分最高的结果中，取前RAGRANKTOPN个结果给下面
        if (searchDocuments.size() > Constant.NUMFILTER[0]) {
            searchDocuments = searchDocuments.subList(0, Constant.LLM_RAG_TOPN);
        }
        // rerank模型重排
        if (isReRank) {
            searchDocuments = reRankModelService.chat(q, searchDocuments, Constant.NUMFILTER[1]);
        }
        // 按手工标记排序
        searchDocuments.sort((o1, o2) -> {
            Integer rank1 = (int) o1.getMetadata().get("rank");
            Integer rank2 = (int) o2.getMetadata().get("rank");
            return rank2.compareTo(rank1);
        });
        return searchDocuments;
    }

    /**
     * 线性加权法
     */
    public List<Document> rank_linearWeighting(List<Document> searchDocuments, String q, Boolean isReRank)
            throws Exception {
        if (isReRank) {
            searchDocuments = reRankModelService.chat(q, searchDocuments, searchDocuments.size());
            // 修正归一化排名存储类型
            for (Document doc : searchDocuments) {
                int rank = (int) doc.getMetadata().get("rank");
                doc.getMetadata().put("normalizationRank", rank * 0.01);
            }
        } else {
            // 手工排名归一化指标 加入没relevance_score的情况
            for (Document doc : searchDocuments) {
                int rank = (int) doc.getMetadata().get("rank");
                doc.getMetadata().put("normalizationRank", rank * 0.01);
                doc.getMetadata().put("relevance_score", 1);
            }
        }

        // score（Double类型） -> spring ai的得分 searchDocuments.get(0).get("score");
        // distance（Float类型） -> 数据库向量距离
        // searchDocuments.get(0).getMetadata().get("distance")
        // relevance_score（Double类型） -> rerank模型距离
        // searchDocuments.get(0).getMetadata().get("relevance_score")
        // normalizationRank（Double类型） -> 手工排名
        // searchDocuments.get(0).getMetadata().get("normalizationRank")

        // 线性加权法计算综合得分
        // LINEARWEIGHTING = {数据库向量距离，spring ai得分，rerank模型距离，手工排名}
        for (Document doc : searchDocuments) {
            // 获取各项指标值，安全地处理类型转换
            Object scoreObj = doc.getScore();
            Object distanceObj = doc.getMetadata().get("distance");
            Object rerankScoreObj = doc.getMetadata().get("relevance_score");
            Object normalizedRankObj = doc.getMetadata().get("normalizationRank");

            // 安全转换为Double类型，处理可能的Integer、Float等类型
            Double springAiScore = convertToDouble(scoreObj, 0.0);
            Double vectorDistance = convertToDouble(distanceObj, 1.0);
            Double rerankScore = convertToDouble(rerankScoreObj, 1.0);
            Double normalizedRank = convertToDouble(normalizedRankObj, 0.0);

            // 向量距离需要转换为相似度（距离越小，相似度越高）
            // 这里使用 1 / (1 + distance) 进行转换，确保值在0-1之间
            double distanceSimilarity = 1.0 / (1.0 + vectorDistance);

            // 线性加权计算综合得分
            // 权重顺序：{数据库向量距离，spring ai得分，rerank模型距离，手工排名}
            double weightedScore = Constant.LINEARWEIGHTING[0] * distanceSimilarity +
                    Constant.LINEARWEIGHTING[1] * springAiScore +
                    Constant.LINEARWEIGHTING[2] * rerankScore +
                    Constant.LINEARWEIGHTING[3] * normalizedRank;

            // 将计算出的综合得分存储到metadata中
            doc.getMetadata().put("weightedScore", weightedScore);
        }

        // 根据综合得分降序排序
        searchDocuments.sort((doc1, doc2) -> {
            Double score1 = (Double) doc1.getMetadata().get("weightedScore");
            Double score2 = (Double) doc2.getMetadata().get("weightedScore");
            return Double.compare(score2, score1); // 降序排序
        });

        return searchDocuments;
    }

    /**
     * 安全地将Object转换为Double类型
     * 处理Integer、Float、Double等数值类型的转换
     *
     * @param obj          待转换的对象
     * @param defaultValue 默认值
     * @return 转换后的Double值
     */
    private Double convertToDouble(Object obj, Double defaultValue) {
        if (obj == null) {
            return defaultValue;
        }

        if (obj instanceof Double) {
            return (Double) obj;
        } else if (obj instanceof Integer) {
            return ((Integer) obj).doubleValue();
        } else if (obj instanceof Float) {
            return ((Float) obj).doubleValue();
        } else if (obj instanceof Long) {
            return ((Long) obj).doubleValue();
        } else if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else {
            log.warn("无法转换类型 {} 为Double，使用默认值 {}", obj.getClass().getSimpleName(), defaultValue);
            return defaultValue;
        }

    }
}
