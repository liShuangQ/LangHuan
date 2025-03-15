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
COMMENT ON COLUMN t_user.username IS '用户名(不可修改)';
COMMENT ON COLUMN t_user.password IS '密码';
COMMENT ON COLUMN t_user.phone IS '手机号';
COMMENT ON COLUMN t_user.gender IS '性别';
COMMENT ON COLUMN t_user.enabled IS '是否启用（0-未启用；1-启用中）';
COMMENT ON COLUMN t_user.creation_time IS '创建时间';
COMMENT ON COLUMN t_user.last_login_time IS '上一次登录时间';


CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS vector_store;
CREATE TABLE IF NOT EXISTS vector_store
(
    id        uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content   text,
    metadata  json,
    embedding vector
);



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


