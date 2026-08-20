# Page-Pilot

用自然语言生成可预览、可修改、可部署的网站。

用户多轮描述需求，模型通过文件读写工具改工程，前端实时预览；校验构建产物后可以部署到独立静态目录，并用无头浏览器截封面。

这不是语音产品。语音通道在 [ChatPartner](https://github.com/AiHyo/ChatPartner)，不接到这里。

## 能力

- HTML / 多文件 / Vue 工程三种生成类型
- Tool Calling：读、写、改、删、列目录
- 输入护栏：超长输入、敏感词、提示词注入
- Redis 会话记忆 + 数据库历史
- Redisson 用户级 / IP 级限流
- 虚拟线程做 npm 构建和封面截图

## 本地运行

需要 JDK 21、MySQL（库 `page_pilot`，端口 3306）、Redis（6379）、Node（生成 Vue 工程时）。本机容器名一般是 `page-pilot-mysql` / `page-pilot-redis`。

1. 复制配置，不要提交密钥：

```bash
copy src\main\resources\application-local-demo.yml src\main\resources\application-local.yml
```

把 `base-url` / `api-key` 换成自己的 OpenAI 兼容地址（可以是自建网关）。COS 只在部署截图上传时需要。

2. 执行 `sql/create_table.sql`，库名默认 `page_pilot`。

3. 启动（本仓库本地演示约定 **后端 8124、前端 5175**。`application.yml` 里的 8123 不要直接用，本机 8123 是别的项目）：

```bash
mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8124" "-Dspring-boot.run.jvmArguments=-Dapp.code.deploy.host=http://localhost:8124/api/static"
```

```bash
cd page-pilot-frontend
npm install
npm run dev
```

前端会监听 `http://127.0.0.1:5175/`，`/api` 代理到 `http://localhost:8124`。演示账号 `demo_user` / `demo123456`。

若后端必须开在 8123：启动 Vite 前设置 `VITE_PROXY_TARGET=http://localhost:8123`。

## 仓库

https://github.com/AiHyo/page-pilot
