package com.langhuan.serviceai;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.langhuan.model.domain.TRagFile;
import com.langhuan.model.pojo.RagMetaData;
import com.langhuan.service.TRagFileService;
import com.langhuan.utils.rag.RagFileVectorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
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
    private final VectorStore vectorStore;
    private final TRagFileService ragFileService;
    private final JdbcTemplate dao;


    public RagService(RagFileVectorUtils ragFileVectorUtils, VectorStore vectorStore, TRagFileService ragFileService, JdbcTemplate dao) {
        this.ragFileVectorUtils = ragFileVectorUtils;
        this.vectorStore = vectorStore;
        this.ragFileService = ragFileService;
        this.dao = dao;
    }

    public List<String> readAndSplitDocument(MultipartFile file, String splitFileMethod,
                                             Map<String, Object> methodData) {
        return ragFileVectorUtils.readAndSplitDocument(file, splitFileMethod, methodData);
    }

    public String writeDocumentsToVectorStore(List<String> documents, TRagFile ragFile) {
        log.info("Adding new file: {}", ragFile);
        ragFile.setId((int) IdUtil.getSnowflakeNextId());
        ragFile.setUploadedAt(new java.util.Date());
        ragFile.setUploadedBy(SecurityContextHolder.getContext().getAuthentication().getName());

        RagMetaData metadata = new RagMetaData();
        metadata.setFilename(ragFile.getFileName());
        metadata.setFileId(String.valueOf(ragFile.getId()));
        metadata.setGroupId(ragFile.getFileGroupId());
        if (ragFileVectorUtils.writeDocumentsToVectorStore(documents, metadata, vectorStore)) {
            ragFileService.save(ragFile);
            return "添加成功";
        } else {
            return "添加失败，请检查日志。";
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteFileAndDocuments(Integer id) {
        log.info("Deleting file and documents with ID: {}", id);
        String sql = """
                        DELETE FROM vector_store
                        WHERE metadata ->> 'fileId' = ?;
                """;
        dao.update(sql, id.toString());
        ragFileService.removeById(id);
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public String changeFileAndDocuments(TRagFile ragFile) {
        log.info("Updating changeFileAndDocuments: {}", ragFile);
        String sql = """
                        UPDATE vector_store
                        SET metadata = jsonb_set(metadata::jsonb, '{groupId}', to_jsonb(?))
                        WHERE metadata ->> 'fileId' = ?;
                """;
        dao.update(sql, ragFile.getFileGroupId(), ragFile.getId().toString());
        ragFileService.updateById(ragFile);
        return "更新成功";
    }
}
