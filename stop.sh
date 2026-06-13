#!/bin/bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=========================================="
echo "  怀旧老物件图文分享站 - 停止脚本"
echo "=========================================="
echo ""

echo "正在停止所有容器..."
docker compose down

echo ""
echo "✅ 所有容器已停止"
