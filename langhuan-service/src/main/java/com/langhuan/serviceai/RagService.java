package com.langhuan.serviceai;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

import com.langhuan.common.BusinessException;
import com.langhuan.common.Constant;
import com.langhuan.config.VectorStoreConfig;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.service.TRagFileService;
import com.langhuan.utils.rag.RagFileVectorUtils;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RagService {

    private final RagFileVectorUtils ragFileVectorUtils;
    private final TRagFileService ragFileService;
    private final JdbcTemplate baseDao;
    private final VectorStore ragVectorStore;

    @Autowired
    public RagService(RagFileVectorUtils ragFileVectorUtils, TRagFileService ragFileService, JdbcTemplate jdbcTemplate,
            VectorStoreConfig vectorStoreConfig) {
        this.ragFileVectorUtils = ragFileVectorUtils;
        this.ragFileService = ragFileService;
        this.baseDao = jdbcTemplate;
        this.ragVectorStore = vectorStoreConfig.ragVectorStore();
    }

    @Transactional(rollbackFor = Exception.class)
    public String changeDocumentText(String documents, String documentId, TRagFile ragFile) {
        log.info("Updating changeDocumentText: {},documentId: {},documents: {}", ragFile, documentId, documents);
        String sql = "DELETE FROM vector_store_rag WHERE id = ?::uuid";
        baseDao.update(sql, documentId);
        if (ragFileVectorUtils.writeDocumentsToVectorStore(List.of(documents), ragFileVectorUtils.makeMateData(ragFile),
                ragVectorStore)) {
            return "更新成功";
        } else {
            return "更新失败，请检查日志。";
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String changeDocumentTextByString(String documents, String documentId) {
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
        if (ragFileVectorUtils.writeDocumentsToVectorStore(List.of(documents),
                ragFileVectorUtils.makeMateData(fileName, fileId, groupId),
                ragVectorStore)) {
            return "更新成功";
        } else {
            return "更新失败，请检查日志。";
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public String deleteDocumentText(String documentId, TRagFile ragFile) {
        log.info("Delete deleteDocumentText: {},documentId: {}", ragFile, documentId);
        String sql = "DELETE FROM vector_store_rag WHERE id = ?::uuid";
        baseDao.update(sql, documentId);
        String num = String.valueOf(Integer.parseInt(ragFile.getDocumentNum()) - 1);
        ragFile.setDocumentNum(num);
        boolean updated = ragFileService.updateById(ragFile);
        return updated ? "删除成功" : "删除失败";
    }

    public List<String> readAndSplitDocument(MultipartFile file, String splitFileMethod,
            Map<String, Object> methodData) {
        return ragFileVectorUtils.readAndSplitDocument(file, splitFileMethod, methodData);
    }

    public String writeDocumentsToVectorStore(List<String> documents, TRagFile ragFile) {
        log.info("writeDocumentsToVectorStore: {}", ragFile);
        boolean b = ragFile.getId() == null || ragFile.getId() == 0 || ragFile.getId().toString().isEmpty();
        if (b) {
            ragFile.setId((int) IdUtil.getSnowflakeNextId());
        }
        ragFile.setUploadedAt(new java.util.Date());
        ragFile.setUploadedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        if (ragFileVectorUtils.writeDocumentsToVectorStore(documents, ragFileVectorUtils.makeMateData(ragFile),
                ragVectorStore)) {
            if (b) {
                log.info("添加到新增文件");
                ragFileService.save(ragFile);
            } else {
                log.info("添加到已有文件");
                ragFileService.updateById(ragFile);
            }
            return "添加成功";
        } else {
            return "添加失败，请检查日志。";
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteFileAndDocuments(Integer id) {
        log.info("Deleting file and documents with ID: {}", id);
        String sql = """
                        DELETE FROM vector_store_rag
                        WHERE metadata ->> 'fileId' = ?;
                """;
        baseDao.update(sql, id.toString());
        ragFileService.removeById(id);
        return true;
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
        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM vector_store_rag WHERE id IN (");
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
        return baseDao.queryForList(sql, params);
    }

    public List<Document> ragSearch(String q, String groupId, String fileId) {
        List<Document> searchDocuments = null;
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
        // 排序rank
        searchDocuments.sort((o1, o2) -> {
            Integer rank1 = (int) o1.getMetadata().get("rank");
            Integer rank2 = (int) o2.getMetadata().get("rank");
            return rank2.compareTo(rank1);
        });
        return searchDocuments;
    }

}
