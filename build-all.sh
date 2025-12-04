#!/usr/bin/env bash
# MiniLPA WebUI 完整构建脚本

set -e  # 遇到错误立即退出

echo "🚀 开始构建 MiniLPA WebUI..."
echo ""

# 1. 构建前端
echo "📦 步骤 1/3: 构建前端..."
cd frontend
echo "  安装依赖..."
npm install
echo "  构建静态文件..."
npm run build
cd ..
echo "✅ 前端构建完成"
echo ""

# 2. 构建后端
echo "🔧 步骤 2/3: 构建后端..."
cd backend
./gradlew build
cd ..
echo "✅ 后端构建完成"
echo ""

# 3. 创建发布包
echo "📦 步骤 3/3: 创建发布包..."
mkdir -p dist
cd backend/build/distributions
tar -xf backend.tar
mv backend ../../../dist/minilpa-web
cd ../../..
echo "✅ 发布包创建完成"
echo ""

echo "🎉 构建完成！"
echo ""
echo "📂 发布包位置: dist/minilpa-web/"
echo "🚀 运行方式:"
echo "   cd dist/minilpa-web/bin"
echo "   LPAC_PATH=<lpac路径> ./backend"
echo ""
