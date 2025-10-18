DROP TABLE IF EXISTS t_api_log;
-- 接口调用日志统计表
CREATE TABLE t_api_log (
    id BIGSERIAL PRIMARY KEY,
    api_name VARCHAR(255) NOT NULL,
    api_url VARCHAR(500) NOT NULL,
    api_description VARCHAR(1000),
    http_method VARCHAR(10) NOT NULL,
    user_id VARCHAR(100),
    username VARCHAR(100),
    request_ip VARCHAR(50),
    request_params TEXT,
    response_data TEXT,
    execution_time BIGINT,
    is_success BOOLEAN DEFAULT true,
    error_message TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_api_log_user_id ON t_api_log(user_id);
CREATE INDEX idx_api_log_api_url ON t_api_log(api_url);
CREATE INDEX idx_api_log_create_time ON t_api_log(create_time);
CREATE INDEX idx_api_log_api_name ON t_api_log(api_name);

-- 添加表注释
COMMENT ON TABLE t_api_log IS '接口调用日志统计表';
COMMENT ON COLUMN t_api_log.id IS '主键ID';
COMMENT ON COLUMN t_api_log.api_name IS '接口名称';
COMMENT ON COLUMN t_api_log.api_url IS '接口URL';
COMMENT ON COLUMN t_api_log.api_description IS '接口说明';
COMMENT ON COLUMN t_api_log.http_method IS 'HTTP请求方法';
COMMENT ON COLUMN t_api_log.user_id IS '用户ID';
COMMENT ON COLUMN t_api_log.username IS '用户名';
COMMENT ON COLUMN t_api_log.request_ip IS '请求IP地址';
COMMENT ON COLUMN t_api_log.request_params IS '接口入参JSON字符串';
COMMENT ON COLUMN t_api_log.response_data IS '接口出参JSON字符串';
COMMENT ON COLUMN t_api_log.execution_time IS '接口执行时长(毫秒)';
COMMENT ON COLUMN t_api_log.is_success IS '接口调用是否成功';
COMMENT ON COLUMN t_api_log.error_message IS '错误信息';
COMMENT ON COLUMN t_api_log.create_time IS '创建时间';
COMMENT ON COLUMN t_api_log.update_time IS '更新时间'; 