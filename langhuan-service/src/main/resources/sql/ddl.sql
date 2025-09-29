DROP TABLE IF EXISTS t_permission;
CREATE TABLE t_permission
(
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    url       VARCHAR(255) NOT NULL,
    parent_id INT          NOT NULL DEFAULT 0
);

COMMENT ON TABLE t_permission IS '系统权限表';
COMMENT ON COLUMN t_permission.name IS '名称';
COMMENT ON COLUMN t_permission.url IS '权限标识';
COMMENT ON COLUMN t_permission.parent_id IS '父级权限id';

DROP TABLE IF EXISTS t_role;
CREATE TABLE t_role
(
    id     SERIAL PRIMARY KEY,
    name   VARCHAR(50)  NOT NULL DEFAULT '',
    remark VARCHAR(255) NULL     DEFAULT NULL
);

COMMENT ON TABLE t_role IS '系统角色表';
COMMENT ON COLUMN t_role.name IS '名称';
COMMENT ON COLUMN t_role.remark IS '备注';

DROP TABLE IF EXISTS t_role_permission;
CREATE TABLE t_role_permission
(
    id            SERIAL PRIMARY KEY,
    role_id       INT NOT NULL,
    permission_id INT NOT NULL
);

COMMENT ON TABLE t_role_permission IS '角色-权限关联表';

DROP TABLE IF EXISTS t_user_role;
CREATE TABLE t_user_role
(
    id      SERIAL PRIMARY KEY,
    role_id INT NOT NULL,
    user_id INT NOT NULL
);

COMMENT ON TABLE t_user_role IS '用户-角色关系表';

DROP TABLE IF EXISTS t_user;
CREATE TABLE t_user
(
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    username        VARCHAR(255) NOT NULL DEFAULT '',
    password        VARCHAR(255) NOT NULL DEFAULT '',
    phone           VARCHAR(11)  NOT NULL,
    gender          SMALLINT     NOT NULL,
    enabled         SMALLINT     NOT NULL,
    creation_time   TIMESTAMP    NULL     DEFAULT NULL,
    last_login_time TIMESTAMP    NULL     DEFAULT NULL
);

COMMENT ON TABLE t_user IS '用户表';
COMMENT ON COLUMN t_user.name IS '姓名';
COMMENT ON COLUMN t_user.username IS '用户名(不可修改,在代码中实际作为主键使用)';
COMMENT ON COLUMN t_user.password IS '密码';
COMMENT ON COLUMN t_user.phone IS '手机号';
COMMENT ON COLUMN t_user.gender IS '性别';
COMMENT ON COLUMN t_user.enabled IS '是否启用（0-未启用；1-启用中）';
COMMENT ON COLUMN t_user.creation_time IS '创建时间';
COMMENT ON COLUMN t_user.last_login_time IS '上一次登录时间';


CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS vector_store_rag (
                                            id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
                                            content text,
                                            metadata json,
                                            embedding vector(1024)
);

CREATE INDEX ON vector_store_rag USING HNSW (embedding vector_cosine_ops);



-- 创建提示词表
CREATE TABLE t_prompts
(
    -- 提示词的唯一标识符，使用自增序列
    id         SERIAL PRIMARY KEY,
    -- 提示词的内容，使用文本类型存储较长的提示信息
    content    TEXT NOT NULL,
    -- 提示词的分类，可根据业务需求进行分类，如业务类型、使用场景等
    category   VARCHAR(255),
    -- 提示词的创建时间，使用时间戳类型自动记录创建时刻
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- 提示词的更新时间，使用时间戳类型，初始值为创建时间，后续更新时会修改
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- 向 prompts 表中添加 method_name 字段，用于存储方法名
ALTER TABLE t_prompts
    ADD COLUMN method_name VARCHAR(255);
-- 向 prompts 表中添加 description 字段，用于存储方法描述
ALTER TABLE t_prompts
    ADD COLUMN description TEXT;
-- 创建更新触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS
$$
BEGIN
    -- 当记录更新时，自动更新 updated_at 字段为当前时间
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为 prompts 表添加更新触发器
CREATE TRIGGER update_prompts_updated_at
    BEFORE UPDATE
    ON t_prompts
    FOR EACH ROW
EXECUTE PROCEDURE update_updated_at_column();



DROP TABLE IF EXISTS t_rag_file_group;
CREATE TABLE t_rag_file_group
(
    id         SERIAL PRIMARY KEY,                 -- 自增主键
    group_name VARCHAR(255) NOT NULL,              -- 文件组名称
    group_type VARCHAR(255) NOT NULL,              -- 文件组类型
    group_desc VARCHAR(255) NOT NULL,              -- 文件组描述
    created_by VARCHAR(100) NOT NULL,              -- 创建用户（可以是用户名或用户ID）
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 创建时间，默认为当前时间
);

ALTER TABLE t_rag_file_group
ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'public' CHECK (visibility IN ('private', 'public'));

DROP TABLE IF EXISTS t_rag_file_group_share;
CREATE TABLE t_rag_file_group_share
(
    id SERIAL PRIMARY KEY,                          -- 自增主键
    file_group_id INTEGER NOT NULL,                 -- 关联的文件组ID
    shared_with VARCHAR(255) NOT NULL,              -- 被分享的用户ID（关联t_user表）
    can_read BOOLEAN NOT NULL DEFAULT TRUE,         -- 是否有读取权限
    can_add BOOLEAN NOT NULL DEFAULT FALSE,         -- 是否有添加文件权限
    can_update BOOLEAN NOT NULL DEFAULT FALSE,      -- 是否有更新权限
    can_delete BOOLEAN NOT NULL DEFAULT FALSE,      -- 是否有删除权限
    shared_by VARCHAR(255) NOT NULL,                -- 分享人ID（关联t_user表）
    shared_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- 分享时间
    CONSTRAINT uk_file_group_shared_with UNIQUE (file_group_id, shared_with) -- 唯一约束：同一文件组不能重复分享给同一用户
);

-- 添加表和字段注释
COMMENT ON TABLE t_rag_file_group_share IS '文件组共享权限表';
COMMENT ON COLUMN t_rag_file_group_share.file_group_id IS '关联的文件组ID';
COMMENT ON COLUMN t_rag_file_group_share.shared_with IS '被分享的用户ID';
COMMENT ON COLUMN t_rag_file_group_share.can_read IS '是否有读取权限';
COMMENT ON COLUMN t_rag_file_group_share.can_add IS '是否有添加文件权限';
COMMENT ON COLUMN t_rag_file_group_share.can_update IS '是否有更新权限';
COMMENT ON COLUMN t_rag_file_group_share.can_delete IS '是否有删除权限';
COMMENT ON COLUMN t_rag_file_group_share.shared_by IS '分享人ID';
COMMENT ON COLUMN t_rag_file_group_share.shared_at IS '分享时间';

DROP TABLE IF EXISTS t_rag_file;
CREATE TABLE t_rag_file
(
    id            SERIAL PRIMARY KEY,                 -- 自增主键
    file_name     VARCHAR(255) NOT NULL,              -- 文件名
    file_type     VARCHAR(255)  NOT NULL,              -- 文件类型（例如：pdf, docx, jpg 等）
    file_size     VARCHAR(255) NOT NULL,              -- 文件大小
    document_num  VARCHAR(255) NOT NULL,              -- 切分出文档的个数
    file_desc     VARCHAR(255) NOT NULL,              -- 文件描述
    file_group_id VARCHAR(255)  NOT NULL,             -- 关联文件组id
    uploaded_by   VARCHAR(100) NOT NULL,              -- 上传用户（可以是用户名或用户ID）
    uploaded_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 上传时间，默认为当前时间
);

DROP TABLE IF EXISTS t_tool_group;
CREATE TABLE t_tool_group
(
    id         SERIAL PRIMARY KEY,                 -- 自增主键
    group_name VARCHAR(255) NOT NULL,              -- 工具组名称
    group_desc VARCHAR(255) NOT NULL,              -- 工具组描述
    created_by VARCHAR(100) NOT NULL,              -- 创建用户（可以是用户名或用户ID）
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 创建时间，默认为当前时间
);

DROP TABLE IF EXISTS t_tool;
CREATE TABLE t_tool
(
    id            SERIAL PRIMARY KEY,                 -- 自增主键
    tool_name     VARCHAR(255) NOT NULL,              -- 工具名
    tool_type     VARCHAR(50)  NOT NULL,              -- 工具类型（例如：文件，操作 等）
    tool_desc     VARCHAR(255) NOT NULL,              -- 工具描述
    tool_group_id VARCHAR(50)  NOT NULL,              -- 关联组id
    tool_by       VARCHAR(100) NOT NULL,              -- 上传用户（可以是用户名或用户ID）
    tool_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- 上传时间，默认为当前时间
);


-- 用户对话反馈统计表
DROP TABLE IF EXISTS t_chat_feedback;
CREATE TABLE t_chat_feedback (
                                  id SERIAL PRIMARY KEY,                   -- 自增主键
                                  user_id VARCHAR(64) NOT NULL,            -- 用户唯一标识
                                  user_info JSONB,                          -- 用户扩展信息（可选，存储用户画像等结构化数据）
                                  question_id VARCHAR(64) NOT NULL,        -- 问题唯一标识
                                  question_content TEXT NOT NULL,           -- 问题具体内容
                                  answer_content TEXT NOT NULL,             -- 问题对应的回答内容
                                  interaction VARCHAR(10) NOT NULL,         -- 互动类型（点赞/点踩/完成） like dislike end
                                  interaction_time TIMESTAMP DEFAULT NOW(), -- 互动时间（带时区）
                                  knowledge_base_ids VARCHAR(1024) NOT NULL,    -- 所调用的知识库ID数组（例如 '{KB001,KB002}'）
                                  suggestion TEXT                           -- 用户附加建议（可选，用于收集改进意见）
);
ALTER TABLE t_chat_feedback
ADD COLUMN use_prompt TEXT DEFAULT NULL;

ALTER TABLE t_chat_feedback
ADD COLUMN use_model VARCHAR(255) DEFAULT NULL;

ALTER TABLE t_chat_feedback
ADD COLUMN use_file_group_id VARCHAR(255) DEFAULT NULL;

ALTER TABLE t_chat_feedback
ADD COLUMN use_rank BOOLEAN DEFAULT FALSE;

-- 用户对话反馈统计表
DROP TABLE IF EXISTS t_user_chat_window;
CREATE TABLE t_user_chat_window (
                                  id SERIAL PRIMARY KEY,                   -- 自增主键
                                  user_id VARCHAR(36) NOT NULL,           -- 用户id
                                  conversation_id VARCHAR(100) NOT NULL,   -- 对话记忆id
                                  conversation_name VARCHAR(100) NOT NULL,   -- 对话名称
                                  created_time TIMESTAMP DEFAULT NOW()    -- 创建时间
);


-- 对话记忆
CREATE TABLE IF NOT EXISTS SPRING_AI_CHAT_MEMORY (
                                                     conversation_id VARCHAR(100) NOT NULL,
                                                     content TEXT NOT NULL,
                                                     type VARCHAR(10) NOT NULL CHECK (type IN ('USER', 'ASSISTANT', 'SYSTEM', 'TOOL')),
                                                     "timestamp" TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS SPRING_AI_CHAT_MEMORY_CONVERSATION_ID_TIMESTAMP_IDX
    ON SPRING_AI_CHAT_MEMORY(conversation_id, "timestamp");


-- 系统通知
CREATE TABLE t_notifications (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(255),  
    template_id VARCHAR(255),  
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    notification_level VARCHAR(20) NOT NULL CHECK (notification_level IN ('info', 'warning', 'error', 'critical')),
    notification_type VARCHAR(30) NOT NULL CHECK (notification_type IN ('system', 'reminder', 'alert', 'message', 'update')),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    reference_id VARCHAR(255),
    reference_type VARCHAR(30),
    expires_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);


COMMENT ON TABLE t_notifications IS '系统通知表，用于存储发送给用户的各类通知信息';
COMMENT ON COLUMN t_notifications.id IS '通知唯一标识，使用UUID确保全局唯一性';
COMMENT ON COLUMN t_notifications.user_id IS '接收通知的用户ID，关联到users表。为空就为全局通知';
COMMENT ON COLUMN t_notifications.template_id IS '';
COMMENT ON COLUMN t_notifications.title IS '通知标题，简要描述通知内容';
COMMENT ON COLUMN t_notifications.content IS '通知详细内容，支持HTML格式';
COMMENT ON COLUMN t_notifications.notification_level IS '通知级别，分为info(信息)、warning(警告)、error(错误)、critical(严重)';
COMMENT ON COLUMN t_notifications.notification_type IS '通知类型，分为system(系统)、reminder(提醒)、alert(警报)、message(消息)、update(更新)';
COMMENT ON COLUMN t_notifications.is_read IS '通知读取状态，true表示已读，false表示未读';
COMMENT ON COLUMN t_notifications.is_archived IS '通知归档状态，true表示已归档(用户手动隐藏)，false表示未归档';
COMMENT ON COLUMN t_notifications.reference_id IS '关联业务对象ID，如订单ID、任务ID等，用于业务对象关联';
COMMENT ON COLUMN t_notifications.reference_type IS '关联业务对象类型，如order、task等，与reference_id配合使用';
COMMENT ON COLUMN t_notifications.expires_at IS '通知过期时间，超过此时间的通知将被视为无效';
COMMENT ON COLUMN t_notifications.created_at IS '通知创建时间戳，自动记录通知创建时间';

DROP TABLE IF EXISTS t_file_url;
CREATE TABLE t_file_url (
                            id SERIAL PRIMARY KEY,
                            file_id INTEGER NOT NULL,
                            f_url VARCHAR(255),
                            f_status VARCHAR(255)
);