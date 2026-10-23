@echo off
rem 推送镜像到阿里云 ACR（前提：build-all.bat + docker compose build 已完成，且已 docker login）
rem 用法：push-images.bat            —— 推送 6 个业务镜像
rem       push-images.bat middleware —— 推送 5 个中间件镜像（首次或中间件升级时）
setlocal
set R=crpi-5t4s3x80h9fvqb8u.cn-hangzhou.personal.cr.aliyuncs.com/jiehuo-forum

if "%1"=="middleware" goto middleware

rem ---- 业务镜像 ----
for %%s in (auth gateway im platform search util) do (
    echo [PUSH] forum-%%s
    docker tag forum-forum-%%s:latest %R%/forum-%%s:latest
    docker push %R%/forum-%%s:latest || echo [FAIL] forum-%%s & goto :err
)
goto done

:middleware
docker tag mysql:8.0 %R%/mysql:8.0
docker push %R%/mysql:8.0 || goto :err
docker tag redis:7-alpine %R%/redis:7-alpine
docker push %R%/redis:7-alpine || goto :err
docker tag nacos/nacos-server:v2.2.3 %R%/nacos:v2.2.3
docker push %R%/nacos:v2.2.3 || goto :err
docker tag docker.elastic.co/elasticsearch/elasticsearch:8.1.0 %R%/elasticsearch:8.1.0
docker push %R%/elasticsearch:8.1.0 || goto :err
docker tag xuxueli/xxl-job-admin:2.5.0 %R%/xxl-job-admin:2.5.0
docker push %R%/xxl-job-admin:2.5.0 || goto :err
goto done

:err
echo [ERROR] 推送失败
exit /b 1

:done
echo [OK] 推送完成
endlocal
