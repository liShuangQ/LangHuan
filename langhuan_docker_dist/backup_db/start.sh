DB_USER="postgres"
DB_NAME="postgres"
BACKUP_DIR="/home/ai-app/code/langhuan_docker_dist/sql_backup"
LOG_FILE="$BACKUP_DIR/postgres_backup.log"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 生成时间戳
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
# 查找pgvector容器
CONTAINER_ID=$(docker ps --filter "ancestor=pgvector/pgvector:pg17" -q)
if [ -z "$CONTAINER_ID" ]; then
    log_message "错误: 未找到pgvector/pgvector:pg17容器"
    exit 1
fi

BACKUP_FILE="$BACKUP_DIR/${DB_NAME}_${TIMESTAMP}.dump"

# 记录日志函数
log_message() {
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $1" >> $LOG_FILE
}

# 开始备份
log_message "开始备份数据库 ${DB_NAME}"

# 执行备份
docker exec $CONTAINER_ID pg_dump -Fc -U $DB_USER $DB_NAME > $BACKUP_FILE

# 检查备份是否成功
if [ $? -eq 0 ]; then
    log_message "数据库备份成功: $BACKUP_FILE"
else
    log_message "数据库备份失败!"
    docker logs $CONTAINER_ID >> $LOG_FILE 2>&1
    exit 1
fi

# 删除30天前的备份文件
find $BACKUP_DIR -name "${DB_NAME}_*.dump" -mtime +30 -exec rm {} \;
log_message "已清理30天前的备份文件"

log_message "备份过程完成"
