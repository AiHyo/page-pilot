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

需要 JDK 21、MySQL、Redis、Node（生成 Vue 工程时）。

1. 复制配置，不要提交密钥：

```bash
copy src\main\resources\application-local-demo.yml src\main\resources\application-local.yml
```

把 `base-url` / `api-key` 换成自己的 OpenAI 兼容地址（可以是自建网关）。COS 只在部署截图上传时需要。

2. 执行 `sql/create_table.sql`，库名默认 `page_pilot`。

3. 启动：

```bash
mvnw.cmd spring-boot:run
```

```bash
cd page-pilot-frontend
npm install
npm run dev
```

后端默认 `http://localhost:8123/api`。

## 仓库

https://github.com/AiHyo/page-pilot
