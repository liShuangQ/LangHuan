-- 权限表索引
CREATE INDEX idx_permission_parent_id ON t_permission (parent_id);

CREATE INDEX idx_permission_name ON t_permission (name);

CREATE INDEX idx_permission_url ON t_permission (url);

-- 角色表索引
CREATE INDEX idx_role_name ON t_role (name);

-- 角色-权限关联表索引
CREATE INDEX idx_role_permission_role_id ON t_role_permission (role_id);

CREATE INDEX idx_role_permission_permission_id ON t_role_permission (permission_id);

CREATE INDEX idx_role_permission_composite ON t_role_permission (role_id, permission_id);

-- 用户-角色关系表索引
CREATE INDEX idx_user_role_user_id ON t_user_role (user_id);

CREATE INDEX idx_user_role_role_id ON t_user_role (role_id);

CREATE INDEX idx_user_role_composite ON t_user_role (user_id, role_id);

-- 用户表索引
CREATE UNIQUE INDEX idx_user_username ON t_user (username);

CREATE INDEX idx_user_phone ON t_user (phone);

CREATE INDEX idx_user_enabled ON t_user (enabled);

CREATE INDEX idx_user_creation_time ON t_user (creation_time DESC);

CREATE INDEX idx_user_last_login_time ON t_user (last_login_time DESC);

-- 提示词表索引
CREATE INDEX idx_prompts_category ON t_prompts (category);

CREATE INDEX idx_prompts_method_name ON t_prompts (method_name);

CREATE INDEX idx_prompts_created_at ON t_prompts (created_at DESC);

CREATE INDEX idx_prompts_updated_at ON t_prompts (updated_at DESC);

-- 工具组表索引
CREATE INDEX idx_tool_group_created_by ON t_tool_group (created_by);

CREATE INDEX idx_tool_group_created_at ON t_tool_group (created_at DESC);

-- 工具表索引
CREATE INDEX idx_tool_tool_group_id ON t_tool (tool_group_id);

CREATE INDEX idx_tool_tool_type ON t_tool (tool_type);

CREATE INDEX idx_tool_tool_by ON t_tool (tool_by);

CREATE INDEX idx_tool_tool_at ON t_tool (tool_at DESC);

-- 用户对话反馈表索引
CREATE INDEX idx_chat_feedback_user_id ON t_chat_feedback (user_id);

CREATE INDEX idx_chat_feedback_question_id ON t_chat_feedback (question_id);

CREATE INDEX idx_chat_feedback_interaction_time ON t_chat_feedback (interaction_time DESC);

CREATE INDEX idx_chat_feedback_interaction ON t_chat_feedback (interaction);

CREATE INDEX idx_chat_feedback_use_model ON t_chat_feedback (use_model);

CREATE INDEX idx_chat_feedback_use_file_group_id ON t_chat_feedback (use_file_group_id);

CREATE INDEX idx_chat_feedback_use_rank ON t_chat_feedback (use_rank);

-- 用户对话窗口表索引
CREATE INDEX idx_user_chat_window_user_id ON t_user_chat_window (user_id);

CREATE INDEX idx_user_chat_window_conversation_id ON t_user_chat_window (conversation_id);

CREATE INDEX idx_user_chat_window_created_time ON t_user_chat_window (created_time DESC);

CREATE UNIQUE INDEX idx_user_chat_window_user_conversation ON t_user_chat_window (user_id, conversation_id);

-- 系统通知表索引
CREATE INDEX idx_notifications_user_id ON t_notifications (user_id);

CREATE INDEX idx_notifications_notification_level ON t_notifications (notification_level);

CREATE INDEX idx_notifications_notification_type ON t_notifications (notification_type);

CREATE INDEX idx_notifications_is_read ON t_notifications (is_read);

CREATE INDEX idx_notifications_is_archived ON t_notifications (is_archived);

CREATE INDEX idx_notifications_created_at ON t_notifications (created_at DESC);

CREATE INDEX idx_notifications_expires_at ON t_notifications (expires_at);

CREATE INDEX idx_notifications_level_type_read ON t_notifications (notification_level, notification_type, is_read);

-- 文件URL表索引
CREATE INDEX idx_file_url_file_id ON t_file_url (file_id);

CREATE INDEX idx_file_url_f_status ON t_file_url (f_status);

-- 文件组表索引
CREATE INDEX idx_rag_file_group_group_name ON t_rag_file_group (group_name);

CREATE INDEX idx_rag_file_group_visibility ON t_rag_file_group (visibility);

CREATE INDEX idx_rag_file_group_created_by ON t_rag_file_group (created_by);

CREATE INDEX idx_rag_file_group_created_at ON t_rag_file_group (created_at DESC);

-- 文件表索引
CREATE INDEX idx_rag_file_group_id ON t_rag_file (file_group_id);

CREATE INDEX idx_rag_file_uploaded_by ON t_rag_file (uploaded_by);

CREATE INDEX idx_rag_file_uploaded_at ON t_rag_file (uploaded_at DESC);

CREATE INDEX idx_rag_file_type ON t_rag_file (file_type);

-- 文件组分享表索引
CREATE INDEX idx_rag_file_group_share_file_group_id ON t_rag_file_group_share (file_group_id);

CREATE INDEX idx_rag_file_group_share_shared_with ON t_rag_file_group_share (shared_with);

CREATE INDEX idx_rag_file_group_share_shared_by ON t_rag_file_group_share (shared_by);

CREATE INDEX idx_rag_file_group_share_shared_at ON t_rag_file_group_share (shared_at DESC);

-- 复合索引优化常用查询
CREATE INDEX idx_rag_file_group_share_composite ON t_rag_file_group_share (file_group_id, shared_with);

CREATE INDEX idx_rag_file_group_visibility_created_by ON t_rag_file_group (visibility, created_by);
