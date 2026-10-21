# 校园论坛（forum-springboot-vue）

基于 **Spring Cloud 微服务架构**的校园论坛移动端 App 后端 + uni-app 前端，支持图文发帖、评论互动、点赞收藏、关注、WebSocket 私信、ES 全文检索。

> 由 [烟火平台](https://gitee.com/xzjsccz/forum_dev) 改造而来，包名/模块名/业务已全部替换为 forum 校园论坛。

## 功能

- 图文发帖（多图直传七牛云对象存储）、分类/标签、浏览计数
- 评论（两级树形）、点赞、收藏、关注/粉丝
- WebSocket 实时私信、未读消息计数
- Elasticsearch 全文检索（标题/正文/标签/分类加权）+ 搜索历史
- JWT 双 Token（accessToken + refreshToken）认证
- XXL-Job MySQL → ES 增量同步兜底
- 云端容器化部署（Docker Compose 编排 11 容器，镜像推送阿里云 ACR）

## 技术栈

| 层 | 技术 |
|----|------|
| 后端 | Spring Boot 2.3.2 · Spring Cloud Hoxton.SR12 · Spring Cloud Alibaba 2.2.7（Nacos）· Gateway · OpenFeign · MyBatis-Plus |
| 中间件 | MySQL 8.0 · Redis · Elasticsearch 8.1.0 · Nacos 2.2.3 · XXL-Job 2.5.0 |
| 存储 | 七牛云对象存储（S3 协议，AWS SDK v2），本地策略保留兜底 |
| 前端 | uni-app（Vue2，`forum-web/`，HBuilderX 运行） |
| 部署 | Docker Compose · 阿里云 ECS + 容器镜像服务 ACR |

## 模块

| 模块 | 端口 | 职责 |
|------|:--:|------|
| forum-gateway | 88 | 统一入口、路由、CORS |
| forum-auth | 8801 | 登录注册、JWT 签发/刷新 |
| forum-platform | 8803 | 帖子/评论/点赞收藏/关注/专辑/标签，内嵌 XXL-Job 执行器（9998） |
| forum-search | 8804 | ES 全文检索、搜索历史 |
| forum-im | 8802 | WebSocket 私信、未读计数 |
| forum-util | 8805 | 文件上传（七牛云）、短信/邮件验证码 |
| forum-common | — | JWT/Redis/异常/拦截器等公共组件 |
| forum-xo | — | Entity/DAO/DTO/VO/Mapper XML |

## 快速开始

### 本地运行

1. 启动中间件：MySQL 8.0（导入 `doc/forum-test.sql`）、Redis、Nacos 2.2.3（standalone，`localhost:8848`）、Elasticsearch 8.1.0
2. 启动服务（顺序任意，Nacos 有重试）：auth → gateway → platform → search → im → util
3. 七牛云上传需配置环境变量 `QINIU_ACCESS_KEY` / `QINIU_SECRET_KEY`（util 的 IDE Run Configuration 中注入）
4. 前端：HBuilderX 打开 `forum-web/` 运行

### 云端部署（Docker Compose）

```bash
build-all.bat                      # 1. 本地打包
docker compose build --provenance=false   # 2. 构建镜像（须关 provenance，见 改造方案/05）
push-images.bat                    # 3. 推送阿里云 ACR
# 服务器：上传 deploy/ 目录后
docker compose pull && docker compose up -d
```

详见 `deploy/部署说明.md`。

> 上传接口要求登录；短信/邮件验证码接口预留未启用。
