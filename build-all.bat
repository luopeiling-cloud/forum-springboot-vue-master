@echo off
rem 容器化前置打包脚本：构建全部模块并生成各模块 target\*.jar
cd /d %~dp0
mvn clean package -Dmaven.test.skip=true
if errorlevel 1 (
    echo [ERROR] 打包失败
    exit /b 1
)
echo [OK] 打包完成，可执行 docker compose up -d --build
