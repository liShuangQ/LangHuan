package com.langhuan.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
public class VectorStoreConfig {
    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;

    public VectorStoreConfig(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
    }

//    TODO: 重写 VectorStore
    @Bean
    public VectorStore ragVectorStore() {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(1024)                    // 1536，嵌入维度，如果未显式指定，PgVectorStore将从提供的EmbeddingModel中获取维度，在创建表时设置嵌入列的维度，若更改维度则需重新创建对应vector_store表，同时注意嵌入模型支持的最大维度
                .distanceType(COSINE_DISTANCE)       // # 搜索距离类型，默认为COSINE_DISTANCE，如果向量已归一化长度为1，可使用EUCLIDEAN_DISTANCE或NEGATIVE_INNER_PRODUCT以获得最佳性能
                .indexType(HNSW)                     // 最近邻搜索索引类型，可选值为NONE（精确最近邻搜索）、IVFFlat（将向量划分为列表，然后搜索与查询向量最接近的子集，构建速度较快且内存使用较少，但查询性能较低）、HNSW（创建多层图，构建速度较慢且内存使用较多，但查询性能较好，无需训练步骤）
                .initializeSchema(false)              // 是否初始化所需的模式
                .schemaName("public")
                .vectorTableName("vector_store_rag")
                .maxDocumentBatchSize(10000)         // 单个批次中处理的最大文档数量
                .build();
    }
}
