#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=========================================="
echo "  怀旧老物件图文分享站 - 启动脚本"
echo "=========================================="
echo ""

echo "[1/4] 加载环境变量..."
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo "  ✓ 环境变量已加载"
else
    echo "  ✗ .env 文件不存在"
    exit 1
fi

echo ""
echo "[2/4] 检查端口占用..."
check_port() {
    local port=$1
    local name=$2
    if lsof -nP -iTCP:"$port" -sTCP:LISTEN > /dev/null 2>&1; then
        echo "  ✗ 端口 $port ($name) 已被占用:"
        lsof -nP -iTCP:"$port" -sTCP:LISTEN | tail -n +2
        return 1
    else
        echo "  ✓ 端口 $port ($name) 可用"
        return 0
    fi
}

PORTS_OK=true
check_port $FRONTEND_PORT "前端" || PORTS_OK=false
check_port $BACKEND_PORT "后端" || PORTS_OK=false
check_port $MYSQL_PORT "MySQL" || PORTS_OK=false
check_port $REDIS_PORT "Redis" || PORTS_OK=false

if [ "$PORTS_OK" = false ]; then
    echo ""
    echo "  端口冲突，请先释放上述端口后重试"
    exit 1
fi

echo ""
echo "[3/4] 启动 Docker 容器..."
docker compose up --build -d

echo ""
echo "[4/4] 等待服务启动..."
sleep 5

MAX_RETRIES=30
RETRY=0
FRONTEND_READY=false

while [ $RETRY -lt $MAX_RETRIES ]; do
    if curl -sSf "http://127.0.0.1:${FRONTEND_PORT}" > /dev/null 2>&1; then
        FRONTEND_READY=true
        break
    fi
    RETRY=$((RETRY + 1))
    echo "  等待前端服务... (${RETRY}/${MAX_RETRIES})"
    sleep 2
done

echo ""
echo "=========================================="
if [ "$FRONTEND_READY" = true ]; then
    echo "  ✅ 项目启动成功！"
    echo ""
    echo "  🌐 前端访问地址:"
    echo "     http://localhost:${FRONTEND_PORT}"
    echo "     http://127.0.0.1:${FRONTEND_PORT}"
    echo ""
    echo "  🔌 后端 API 地址:"
    echo "     http://127.0.0.1:${BACKEND_PORT}/api"
    echo ""
    echo "  💾 MySQL 数据库:"
    echo "     主机: 127.0.0.1:${MYSQL_PORT}"
    echo "     数据库: ${MYSQL_DATABASE}"
    echo "     用户: ${MYSQL_USER}"
    echo ""
    echo "  📦 Redis 缓存:"
    echo "     主机: 127.0.0.1:${REDIS_PORT}"
    echo ""
    echo "  🐳 容器列表:"
    docker compose ps
else
    echo "  ⚠️  服务启动可能需要更多时间，请稍后访问"
    echo ""
    echo "  🌐 前端地址:"
    echo "     http://localhost:${FRONTEND_PORT}"
fi
echo "=========================================="
