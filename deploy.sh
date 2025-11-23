#!/bin/bash
# 规则引擎部署脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Docker是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi

    log_info "Docker环境检查通过"
}

# 构建镜像
build() {
    log_info "开始构建Docker镜像..."
    docker-compose build --no-cache
    log_info "镜像构建完成"
}

# 启动服务
start() {
    log_info "启动规则引擎服务..."
    docker-compose up -d
    log_info "服务启动中，请等待..."

    # 等待服务健康检查
    sleep 10

    # 检查服务状态
    if docker-compose ps | grep -q "Up"; then
        log_info "服务启动成功!"
        log_info "前端访问地址: http://localhost"
        log_info "后端API地址: http://localhost:8080"
        log_info "API文档地址: http://localhost:8080/swagger-ui.html"
    else
        log_error "服务启动失败，请检查日志"
        docker-compose logs --tail=50
        exit 1
    fi
}

# 停止服务
stop() {
    log_info "停止规则引擎服务..."
    docker-compose down
    log_info "服务已停止"
}

# 重启服务
restart() {
    stop
    start
}

# 查看日志
logs() {
    docker-compose logs -f --tail=100 "$@"
}

# 查看服务状态
status() {
    log_info "服务状态:"
    docker-compose ps
}

# 清理资源
clean() {
    log_warn "即将清理所有容器和镜像..."
    read -p "确认清理? (y/n): " confirm
    if [ "$confirm" = "y" ]; then
        docker-compose down -v --rmi all
        log_info "清理完成"
    else
        log_info "已取消"
    fi
}

# 初始化数据库
init_db() {
    log_info "初始化数据库..."
    docker-compose exec mysql mysql -u root -proot123 rule_engine < backend/src/main/resources/sql/schema.sql
    docker-compose exec mysql mysql -u root -proot123 rule_engine < backend/src/main/resources/sql/data.sql
    log_info "数据库初始化完成"
}

# 备份数据库
backup_db() {
    BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"
    log_info "备份数据库到 $BACKUP_FILE..."
    docker-compose exec mysql mysqldump -u root -proot123 rule_engine > "$BACKUP_FILE"
    log_info "备份完成: $BACKUP_FILE"
}

# 帮助信息
help() {
    echo "规则引擎部署脚本"
    echo ""
    echo "用法: ./deploy.sh [命令]"
    echo ""
    echo "命令:"
    echo "  build      构建Docker镜像"
    echo "  start      启动所有服务"
    echo "  stop       停止所有服务"
    echo "  restart    重启所有服务"
    echo "  status     查看服务状态"
    echo "  logs       查看服务日志 (可指定服务名: logs backend)"
    echo "  init-db    初始化数据库"
    echo "  backup-db  备份数据库"
    echo "  clean      清理所有容器和镜像"
    echo "  help       显示帮助信息"
    echo ""
    echo "示例:"
    echo "  ./deploy.sh build    # 构建镜像"
    echo "  ./deploy.sh start    # 启动服务"
    echo "  ./deploy.sh logs backend  # 查看后端日志"
}

# 主入口
main() {
    check_docker

    case "$1" in
        build)
            build
            ;;
        start)
            start
            ;;
        stop)
            stop
            ;;
        restart)
            restart
            ;;
        status)
            status
            ;;
        logs)
            shift
            logs "$@"
            ;;
        init-db)
            init_db
            ;;
        backup-db)
            backup_db
            ;;
        clean)
            clean
            ;;
        help|--help|-h)
            help
            ;;
        *)
            help
            ;;
    esac
}

main "$@"
