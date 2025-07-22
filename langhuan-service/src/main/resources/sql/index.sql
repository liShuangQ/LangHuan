-- 文件组表索引
CREATE INDEX idx_rag_file_group_visibility ON 
t_rag_file_group(visibility);
CREATE INDEX idx_rag_file_group_created_by ON 
t_rag_file_group(created_by);
CREATE INDEX idx_rag_file_group_created_at ON 
t_rag_file_group(created_at DESC);

-- 文件表索引
CREATE INDEX idx_rag_file_group_id ON t_rag_file
(file_group_id);
CREATE INDEX idx_rag_file_uploaded_by ON t_rag_file
(uploaded_by);
CREATE INDEX idx_rag_file_uploaded_at ON t_rag_file
(uploaded_at DESC);
CREATE INDEX idx_rag_file_type ON t_rag_file
(file_type);

-- 文件组分享表索引
CREATE INDEX 
idx_rag_file_group_share_file_group_id ON 
t_rag_file_group_share(file_group_id);
CREATE INDEX idx_rag_file_group_share_shared_with 
ON t_rag_file_group_share(shared_with);
CREATE INDEX idx_rag_file_group_share_shared_by ON 
t_rag_file_group_share(shared_by);
CREATE INDEX idx_rag_file_group_share_shared_at ON 
t_rag_file_group_share(shared_at DESC);

-- 复合索引优化常用查询
CREATE INDEX idx_rag_file_group_share_composite ON 
t_rag_file_group_share(file_group_id, shared_with);
CREATE INDEX 
idx_rag_file_group_visibility_created_by ON 
t_rag_file_group(visibility, created_by);