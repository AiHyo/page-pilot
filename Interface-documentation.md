---
title: 默认模块
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
code_clipboard: true
highlight_theme: darkula
headingLevel: 2
generator: "@tarslib/widdershins v4.0.30"

---

# 默认模块

Base URLs:

# Authentication

# 应用 控制层。

## POST 创建应用

POST /app

> Body 请求参数

```json
{
  "appName": "string",
  "initPrompt": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[AppAddRequest](#schemaappaddrequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": 0,
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseLong](#schemabaseresponselong)|

## DELETE 根据 id 删除应用（用户只能删除自己的应用）

DELETE /app/{id}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |应用ID|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": false,
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseBoolean](#schemabaseresponseboolean)|

## PATCH 更新应用（用户只能更新自己的应用名称）

PATCH /app/{id}

> Body 请求参数

```json
{
  "id": 0,
  "appName": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |应用ID|
|body|body|[AppUpdateRequest](#schemaappupdaterequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": false,
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseBoolean](#schemabaseresponseboolean)|

## GET 根据 id 获取应用详情

GET /app/{id}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |应用id|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "appName": "",
    "cover": "",
    "initPrompt": "",
    "codeGenType": "",
    "deployKey": "",
    "deployedTime": "",
    "priority": 0,
    "userId": 0,
    "user": {
      "id": 0,
      "userAccount": "",
      "userName": "",
      "userAvatar": "",
      "userProfile": "",
      "userRole": "",
      "createTime": ""
    },
    "editTime": "",
    "createTime": "",
    "updateTime": ""
  },
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseAppVO](#schemabaseresponseappvo)|

## GET 分页获取当前用户创建的应用列表

GET /app/list/my

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|pageNum|query|integer| 否 |当前页号|
|pageSize|query|integer| 否 |页面大小|
|sortField|query|string| 否 |排序字段|
|sortOrder|query|string| 否 |排序顺序（默认降序）|
|id|query|integer(int64)| 否 |id|
|appName|query|string| 否 |应用名称|
|cover|query|string| 否 |应用封面|
|initPrompt|query|string| 否 |应用初始化的 prompt|
|codeGenType|query|string| 否 |代码生成类型（枚举）|
|deployKey|query|string| 否 |部署标识|
|priority|query|integer| 否 |优先级|
|userId|query|integer(int64)| 否 |创建用户id|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 0,
        "appName": "",
        "cover": "",
        "initPrompt": "",
        "codeGenType": "",
        "deployKey": "",
        "deployedTime": "",
        "priority": 0,
        "userId": 0,
        "user": {
          "id": 0,
          "userAccount": "",
          "userName": "",
          "userAvatar": "",
          "userProfile": "",
          "userRole": "",
          "createTime": ""
        },
        "editTime": "",
        "createTime": "",
        "updateTime": ""
      }
    ],
    "pageNumber": 0,
    "pageSize": 0,
    "maxPageSize": 0,
    "totalPage": 0,
    "totalRow": 0,
    "optimizeCountQuery": false
  },
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponsePageAppVO](#schemabaseresponsepageappvo)|

## GET 分页获取精选应用列表

GET /app/list/featured

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|pageNum|query|integer| 否 |当前页号|
|pageSize|query|integer| 否 |页面大小|
|sortField|query|string| 否 |排序字段|
|sortOrder|query|string| 否 |排序顺序（默认降序）|
|id|query|integer(int64)| 否 |id|
|appName|query|string| 否 |应用名称|
|cover|query|string| 否 |应用封面|
|initPrompt|query|string| 否 |应用初始化的 prompt|
|codeGenType|query|string| 否 |代码生成类型（枚举）|
|deployKey|query|string| 否 |部署标识|
|priority|query|integer| 否 |优先级|
|userId|query|integer(int64)| 否 |创建用户id|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 0,
        "appName": "",
        "cover": "",
        "initPrompt": "",
        "codeGenType": "",
        "deployKey": "",
        "deployedTime": "",
        "priority": 0,
        "userId": 0,
        "user": {
          "id": 0,
          "userAccount": "",
          "userName": "",
          "userAvatar": "",
          "userProfile": "",
          "userRole": "",
          "createTime": ""
        },
        "editTime": "",
        "createTime": "",
        "updateTime": ""
      }
    ],
    "pageNumber": 0,
    "pageSize": 0,
    "maxPageSize": 0,
    "totalPage": 0,
    "totalRow": 0,
    "optimizeCountQuery": false
  },
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponsePageAppVO](#schemabaseresponsepageappvo)|

## DELETE 根据 id 删除应用（管理员）

DELETE /app/admin/{id}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |应用ID|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": false,
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseBoolean](#schemabaseresponseboolean)|

## PUT 更新应用（管理员）

PUT /app/admin/{id}

> Body 请求参数

```json
{
  "id": 0,
  "appName": "string",
  "cover": "string",
  "priority": 0
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |应用ID|
|body|body|[AppAdminUpdateRequest](#schemaappadminupdaterequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": false,
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseBoolean](#schemabaseresponseboolean)|

## GET 根据 id 获取应用详情（管理员）

GET /app/admin/{id}

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|path|integer| 是 |应用id|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "appName": "",
    "cover": "",
    "initPrompt": "",
    "codeGenType": "",
    "deployKey": "",
    "deployedTime": "",
    "priority": 0,
    "userId": 0,
    "editTime": "",
    "createTime": "",
    "updateTime": "",
    "isDelete": 0
  },
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseApp](#schemabaseresponseapp)|

## GET 分页获取应用列表（管理员）

GET /app/admin/list

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|pageNum|query|integer| 否 |当前页号|
|pageSize|query|integer| 否 |页面大小|
|sortField|query|string| 否 |排序字段|
|sortOrder|query|string| 否 |排序顺序（默认降序）|
|id|query|integer(int64)| 否 |id|
|appName|query|string| 否 |应用名称|
|cover|query|string| 否 |应用封面|
|initPrompt|query|string| 否 |应用初始化的 prompt|
|codeGenType|query|string| 否 |代码生成类型（枚举）|
|deployKey|query|string| 否 |部署标识|
|priority|query|integer| 否 |优先级|
|userId|query|integer(int64)| 否 |创建用户id|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 0,
        "appName": "",
        "cover": "",
        "initPrompt": "",
        "codeGenType": "",
        "deployKey": "",
        "deployedTime": "",
        "priority": 0,
        "userId": 0,
        "user": {
          "id": 0,
          "userAccount": "",
          "userName": "",
          "userAvatar": "",
          "userProfile": "",
          "userRole": "",
          "createTime": ""
        },
        "editTime": "",
        "createTime": "",
        "updateTime": ""
      }
    ],
    "pageNumber": 0,
    "pageSize": 0,
    "maxPageSize": 0,
    "totalPage": 0,
    "totalRow": 0,
    "optimizeCountQuery": false
  },
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponsePageAppVO](#schemabaseresponsepageappvo)|

## GET 应用聊天生成代码（流式 SSE）

GET /app/chat/gen/code

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|appId|query|integer| 是 |应用 ID|
|message|query|string| 是 |用户消息|

> 返回示例

> 200 Response

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

*生成结果流*

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|*anonymous*|[[ServerSentEventString](#schemaserversenteventstring)]|false|none||生成结果流|
|» id|string¦null|false|none||none|
|» event|string¦null|false|none||none|
|» retry|[Duration](#schemaduration)¦null|false|none||none|
|»» seconds|integer(int64)|false|none||The number of seconds in the duration.|
|»» nanos|integer|false|none||The number of nanoseconds in the duration, expressed as a fraction of the<br />number of seconds. This is always positive, and never exceeds 999,999,999.|
|» comment|string¦null|false|none||none|
|» data|string¦null|false|none||none|

## POST 应用部署

POST /app/deploy

> Body 请求参数

```json
{
  "appId": 0
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[AppDeployRequest](#schemaappdeployrequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": "",
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseString](#schemabaseresponsestring)|

# 用户 控制层。

## POST 用户注册

POST /user/register

> Body 请求参数

```json
{
  "userAccount": "string",
  "userPassword": "string",
  "checkPassword": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[UserRegisterRequest](#schemauserregisterrequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": 0,
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseLong](#schemabaseresponselong)|

## POST 用户登录

POST /user/login

> Body 请求参数

```json
{
  "userAccount": "string",
  "userPassword": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[UserLoginRequest](#schemauserloginrequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "userAccount": "",
    "userName": "",
    "userAvatar": "",
    "userProfile": "",
    "userRole": "",
    "createTime": "",
    "updateTime": ""
  },
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseLoginUserVO](#schemabaseresponseloginuservo)|

## GET getLoginUser

GET /user/get/login

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "userAccount": "",
    "userName": "",
    "userAvatar": "",
    "userProfile": "",
    "userRole": "",
    "createTime": "",
    "updateTime": ""
  },
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseLoginUserVO](#schemabaseresponseloginuservo)|

## POST 用户注销

POST /user/logout

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": false,
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseBoolean](#schemabaseresponseboolean)|

## POST 创建用户

POST /user/add

> Body 请求参数

```json
{
  "userName": "string",
  "userAccount": "string",
  "userAvatar": "string",
  "userProfile": "string",
  "userRole": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[UserAddRequest](#schemauseraddrequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": 0,
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseLong](#schemabaseresponselong)|

## GET 根据 id 获取用户（仅管理员）

GET /user/get

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|query|integer| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "userAccount": "",
    "userPassword": "",
    "userName": "",
    "userAvatar": "",
    "userProfile": "",
    "userRole": "",
    "editTime": "",
    "createTime": "",
    "updateTime": "",
    "isDelete": 0
  },
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseUser](#schemabaseresponseuser)|

## GET 根据 id 获取包装类

GET /user/get/vo

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|id|query|integer| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "userAccount": "",
    "userName": "",
    "userAvatar": "",
    "userProfile": "",
    "userRole": "",
    "createTime": ""
  },
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseUserVO](#schemabaseresponseuservo)|

## POST 删除用户

POST /user/delete

> Body 请求参数

```json
{
  "id": 0
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[DeleteRequest](#schemadeleterequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": false,
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseBoolean](#schemabaseresponseboolean)|

## POST 更新用户

POST /user/update

> Body 请求参数

```json
{
  "id": 0,
  "userName": "string",
  "userAvatar": "string",
  "userProfile": "string",
  "userRole": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[UserUpdateRequest](#schemauserupdaterequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": false,
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponseBoolean](#schemabaseresponseboolean)|

## POST 分页获取用户封装列表（仅管理员）

POST /user/list/page/vo

> Body 请求参数

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "sortField": "string",
  "sortOrder": "descend",
  "id": 0,
  "userName": "string",
  "userAccount": "string",
  "userProfile": "string",
  "userRole": "string"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|body|body|[UserQueryRequest](#schemauserqueryrequest)| 否 |none|

> 返回示例

> 200 Response

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 0,
        "userAccount": "",
        "userName": "",
        "userAvatar": "",
        "userProfile": "",
        "userRole": "",
        "createTime": ""
      }
    ],
    "pageNumber": 0,
    "pageSize": 0,
    "maxPageSize": 0,
    "totalPage": 0,
    "totalRow": 0,
    "optimizeCountQuery": false
  },
  "message": ""
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|[BaseResponsePageUserVO](#schemabaseresponsepageuservo)|

# 数据模型

<h2 id="tocS_BaseResponseLong">BaseResponseLong</h2>

<a id="schemabaseresponselong"></a>
<a id="schema_BaseResponseLong"></a>
<a id="tocSbaseresponselong"></a>
<a id="tocsbaseresponselong"></a>

```json
{
  "code": 0,
  "data": 0,
  "message": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|data|integer(int64)|false|none||none|
|message|string|false|none||none|

<h2 id="tocS_AppAddRequest">AppAddRequest</h2>

<a id="schemaappaddrequest"></a>
<a id="schema_AppAddRequest"></a>
<a id="tocSappaddrequest"></a>
<a id="tocsappaddrequest"></a>

```json
{
  "appName": "string",
  "initPrompt": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|appName|string|false|none||应用名称|
|initPrompt|string|false|none||应用初始化的 prompt|

<h2 id="tocS_UserRegisterRequest">UserRegisterRequest</h2>

<a id="schemauserregisterrequest"></a>
<a id="schema_UserRegisterRequest"></a>
<a id="tocSuserregisterrequest"></a>
<a id="tocsuserregisterrequest"></a>

```json
{
  "userAccount": "string",
  "userPassword": "string",
  "checkPassword": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|userAccount|string|false|none||账号|
|userPassword|string|false|none||密码|
|checkPassword|string|false|none||确认密码|

<h2 id="tocS_BaseResponseBoolean">BaseResponseBoolean</h2>

<a id="schemabaseresponseboolean"></a>
<a id="schema_BaseResponseBoolean"></a>
<a id="tocSbaseresponseboolean"></a>
<a id="tocsbaseresponseboolean"></a>

```json
{
  "code": 0,
  "data": true,
  "message": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|data|boolean|false|none||none|
|message|string|false|none||none|

<h2 id="tocS_LoginUserVO">LoginUserVO</h2>

<a id="schemaloginuservo"></a>
<a id="schema_LoginUserVO"></a>
<a id="tocSloginuservo"></a>
<a id="tocsloginuservo"></a>

```json
{
  "id": 0,
  "userAccount": "string",
  "userName": "string",
  "userAvatar": "string",
  "userProfile": "string",
  "userRole": "string",
  "createTime": "string",
  "updateTime": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||用户 id|
|userAccount|string|false|none||账号|
|userName|string|false|none||用户昵称|
|userAvatar|string|false|none||用户头像|
|userProfile|string|false|none||用户简介|
|userRole|string|false|none||用户角色：user/admin|
|createTime|string|false|none||创建时间|
|updateTime|string|false|none||更新时间|

<h2 id="tocS_AppUpdateRequest">AppUpdateRequest</h2>

<a id="schemaappupdaterequest"></a>
<a id="schema_AppUpdateRequest"></a>
<a id="tocSappupdaterequest"></a>
<a id="tocsappupdaterequest"></a>

```json
{
  "id": 0,
  "appName": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||id|
|appName|string|false|none||应用名称|

<h2 id="tocS_BaseResponseLoginUserVO">BaseResponseLoginUserVO</h2>

<a id="schemabaseresponseloginuservo"></a>
<a id="schema_BaseResponseLoginUserVO"></a>
<a id="tocSbaseresponseloginuservo"></a>
<a id="tocsbaseresponseloginuservo"></a>

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "userAccount": "string",
    "userName": "string",
    "userAvatar": "string",
    "userProfile": "string",
    "userRole": "string",
    "createTime": "string",
    "updateTime": "string"
  },
  "message": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|data|[LoginUserVO](#schemaloginuservo)|false|none||none|
|message|string|false|none||none|

<h2 id="tocS_UserVO">UserVO</h2>

<a id="schemauservo"></a>
<a id="schema_UserVO"></a>
<a id="tocSuservo"></a>
<a id="tocsuservo"></a>

```json
{
  "id": 0,
  "userAccount": "string",
  "userName": "string",
  "userAvatar": "string",
  "userProfile": "string",
  "userRole": "string",
  "createTime": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||id|
|userAccount|string|false|none||账号|
|userName|string|false|none||用户昵称|
|userAvatar|string|false|none||用户头像|
|userProfile|string|false|none||用户简介|
|userRole|string|false|none||用户角色：user/admin|
|createTime|string|false|none||创建时间|

<h2 id="tocS_UserLoginRequest">UserLoginRequest</h2>

<a id="schemauserloginrequest"></a>
<a id="schema_UserLoginRequest"></a>
<a id="tocSuserloginrequest"></a>
<a id="tocsuserloginrequest"></a>

```json
{
  "userAccount": "string",
  "userPassword": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|userAccount|string|false|none||账号|
|userPassword|string|false|none||密码|

<h2 id="tocS_AppVO">AppVO</h2>

<a id="schemaappvo"></a>
<a id="schema_AppVO"></a>
<a id="tocSappvo"></a>
<a id="tocsappvo"></a>

```json
{
  "id": 0,
  "appName": "string",
  "cover": "string",
  "initPrompt": "string",
  "codeGenType": "string",
  "deployKey": "string",
  "deployedTime": "string",
  "priority": 0,
  "userId": 0,
  "user": {
    "id": 0,
    "userAccount": "string",
    "userName": "string",
    "userAvatar": "string",
    "userProfile": "string",
    "userRole": "string",
    "createTime": "string"
  },
  "editTime": "string",
  "createTime": "string",
  "updateTime": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||id|
|appName|string|false|none||应用名称|
|cover|string|false|none||应用封面|
|initPrompt|string|false|none||应用初始化的 prompt|
|codeGenType|string|false|none||代码生成类型（枚举）|
|deployKey|string|false|none||部署标识|
|deployedTime|string|false|none||部署时间|
|priority|integer|false|none||优先级|
|userId|integer(int64)|false|none||创建用户id|
|user|[UserVO](#schemauservo)|false|none||创建用户信息|
|editTime|string|false|none||编辑时间|
|createTime|string|false|none||创建时间|
|updateTime|string|false|none||更新时间|

<h2 id="tocS_BaseResponseAppVO">BaseResponseAppVO</h2>

<a id="schemabaseresponseappvo"></a>
<a id="schema_BaseResponseAppVO"></a>
<a id="tocSbaseresponseappvo"></a>
<a id="tocsbaseresponseappvo"></a>

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "appName": "string",
    "cover": "string",
    "initPrompt": "string",
    "codeGenType": "string",
    "deployKey": "string",
    "deployedTime": "string",
    "priority": 0,
    "userId": 0,
    "user": {
      "id": 0,
      "userAccount": "string",
      "userName": "string",
      "userAvatar": "string",
      "userProfile": "string",
      "userRole": "string",
      "createTime": "string"
    },
    "editTime": "string",
    "createTime": "string",
    "updateTime": "string"
  },
  "message": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|data|[AppVO](#schemaappvo)|false|none||none|
|message|string|false|none||none|

<h2 id="tocS_UserAddRequest">UserAddRequest</h2>

<a id="schemauseraddrequest"></a>
<a id="schema_UserAddRequest"></a>
<a id="tocSuseraddrequest"></a>
<a id="tocsuseraddrequest"></a>

```json
{
  "userName": "string",
  "userAccount": "string",
  "userAvatar": "string",
  "userProfile": "string",
  "userRole": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|userName|string|false|none||用户昵称|
|userAccount|string|false|none||账号|
|userAvatar|string|false|none||用户头像|
|userProfile|string|false|none||用户简介|
|userRole|string|false|none||用户角色: user, admin|

<h2 id="tocS_PageAppVO">PageAppVO</h2>

<a id="schemapageappvo"></a>
<a id="schema_PageAppVO"></a>
<a id="tocSpageappvo"></a>
<a id="tocspageappvo"></a>

```json
{
  "records": [
    {
      "id": 0,
      "appName": "string",
      "cover": "string",
      "initPrompt": "string",
      "codeGenType": "string",
      "deployKey": "string",
      "deployedTime": "string",
      "priority": 0,
      "userId": 0,
      "user": {
        "id": 0,
        "userAccount": "string",
        "userName": "string",
        "userAvatar": "string",
        "userProfile": "string",
        "userRole": "string",
        "createTime": "string"
      },
      "editTime": "string",
      "createTime": "string",
      "updateTime": "string"
    }
  ],
  "pageNumber": 0,
  "pageSize": 0,
  "maxPageSize": 0,
  "totalPage": 0,
  "totalRow": 0,
  "optimizeCountQuery": true
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|records|[[AppVO](#schemaappvo)]|false|none||none|
|pageNumber|integer(int64)|false|none||none|
|pageSize|integer(int64)|false|none||none|
|maxPageSize|integer(int64)|false|none||none|
|totalPage|integer(int64)|false|none||none|
|totalRow|integer(int64)|false|none||none|
|optimizeCountQuery|boolean|false|none||none|

<h2 id="tocS_User">User</h2>

<a id="schemauser"></a>
<a id="schema_User"></a>
<a id="tocSuser"></a>
<a id="tocsuser"></a>

```json
{
  "id": 0,
  "userAccount": "string",
  "userPassword": "string",
  "userName": "string",
  "userAvatar": "string",
  "userProfile": "string",
  "userRole": "string",
  "editTime": "string",
  "createTime": "string",
  "updateTime": "string",
  "isDelete": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||id|
|userAccount|string|false|none||账号|
|userPassword|string|false|none||密码|
|userName|string|false|none||用户昵称|
|userAvatar|string|false|none||用户头像|
|userProfile|string|false|none||用户简介|
|userRole|string|false|none||用户角色：user/admin|
|editTime|string|false|none||编辑时间|
|createTime|string|false|none||创建时间|
|updateTime|string|false|none||更新时间|
|isDelete|integer|false|none||是否删除|

<h2 id="tocS_BaseResponsePageAppVO">BaseResponsePageAppVO</h2>

<a id="schemabaseresponsepageappvo"></a>
<a id="schema_BaseResponsePageAppVO"></a>
<a id="tocSbaseresponsepageappvo"></a>
<a id="tocsbaseresponsepageappvo"></a>

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 0,
        "appName": "string",
        "cover": "string",
        "initPrompt": "string",
        "codeGenType": "string",
        "deployKey": "string",
        "deployedTime": "string",
        "priority": 0,
        "userId": 0,
        "user": {
          "id": 0,
          "userAccount": "string",
          "userName": "string",
          "userAvatar": "string",
          "userProfile": "string",
          "userRole": "string",
          "createTime": "string"
        },
        "editTime": "string",
        "createTime": "string",
        "updateTime": "string"
      }
    ],
    "pageNumber": 0,
    "pageSize": 0,
    "maxPageSize": 0,
    "totalPage": 0,
    "totalRow": 0,
    "optimizeCountQuery": true
  },
  "message": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|data|[PageAppVO](#schemapageappvo)|false|none||none|
|message|string|false|none||none|

<h2 id="tocS_BaseResponseUser">BaseResponseUser</h2>

<a id="schemabaseresponseuser"></a>
<a id="schema_BaseResponseUser"></a>
<a id="tocSbaseresponseuser"></a>
<a id="tocsbaseresponseuser"></a>

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "userAccount": "string",
    "userPassword": "string",
    "userName": "string",
    "userAvatar": "string",
    "userProfile": "string",
    "userRole": "string",
    "editTime": "string",
    "createTime": "string",
    "updateTime": "string",
    "isDelete": 0
  },
  "message": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|data|[User](#schemauser)|false|none||none|
|message|string|false|none||none|

<h2 id="tocS_AppAdminUpdateRequest">AppAdminUpdateRequest</h2>

<a id="schemaappadminupdaterequest"></a>
<a id="schema_AppAdminUpdateRequest"></a>
<a id="tocSappadminupdaterequest"></a>
<a id="tocsappadminupdaterequest"></a>

```json
{
  "id": 0,
  "appName": "string",
  "cover": "string",
  "priority": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||id|
|appName|string|false|none||应用名称|
|cover|string|false|none||应用封面|
|priority|integer|false|none||优先级|

<h2 id="tocS_App">App</h2>

<a id="schemaapp"></a>
<a id="schema_App"></a>
<a id="tocSapp"></a>
<a id="tocsapp"></a>

```json
{
  "id": 0,
  "appName": "string",
  "cover": "string",
  "initPrompt": "string",
  "codeGenType": "string",
  "deployKey": "string",
  "deployedTime": "string",
  "priority": 0,
  "userId": 0,
  "editTime": "string",
  "createTime": "string",
  "updateTime": "string",
  "isDelete": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||id|
|appName|string|false|none||应用名称|
|cover|string|false|none||应用封面|
|initPrompt|string|false|none||应用初始化的 prompt|
|codeGenType|string|false|none||代码生成类型（枚举）|
|deployKey|string|false|none||部署标识|
|deployedTime|string|false|none||部署时间|
|priority|integer|false|none||优先级|
|userId|integer(int64)|false|none||创建用户id|
|editTime|string|false|none||编辑时间|
|createTime|string|false|none||创建时间|
|updateTime|string|false|none||更新时间|
|isDelete|integer|false|none||是否删除|

<h2 id="tocS_BaseResponseUserVO">BaseResponseUserVO</h2>

<a id="schemabaseresponseuservo"></a>
<a id="schema_BaseResponseUserVO"></a>
<a id="tocSbaseresponseuservo"></a>
<a id="tocsbaseresponseuservo"></a>

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "userAccount": "string",
    "userName": "string",
    "userAvatar": "string",
    "userProfile": "string",
    "userRole": "string",
    "createTime": "string"
  },
  "message": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|data|[UserVO](#schemauservo)|false|none||none|
|message|string|false|none||none|

<h2 id="tocS_BaseResponseApp">BaseResponseApp</h2>

<a id="schemabaseresponseapp"></a>
<a id="schema_BaseResponseApp"></a>
<a id="tocSbaseresponseapp"></a>
<a id="tocsbaseresponseapp"></a>

```json
{
  "code": 0,
  "data": {
    "id": 0,
    "appName": "string",
    "cover": "string",
    "initPrompt": "string",
    "codeGenType": "string",
    "deployKey": "string",
    "deployedTime": "string",
    "priority": 0,
    "userId": 0,
    "editTime": "string",
    "createTime": "string",
    "updateTime": "string",
    "isDelete": 0
  },
  "message": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|data|[App](#schemaapp)|false|none||none|
|message|string|false|none||none|

<h2 id="tocS_DeleteRequest">DeleteRequest</h2>

<a id="schemadeleterequest"></a>
<a id="schema_DeleteRequest"></a>
<a id="tocSdeleterequest"></a>
<a id="tocsdeleterequest"></a>

```json
{
  "id": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||id|

<h2 id="tocS_UserUpdateRequest">UserUpdateRequest</h2>

<a id="schemauserupdaterequest"></a>
<a id="schema_UserUpdateRequest"></a>
<a id="tocSuserupdaterequest"></a>
<a id="tocsuserupdaterequest"></a>

```json
{
  "id": 0,
  "userName": "string",
  "userAvatar": "string",
  "userProfile": "string",
  "userRole": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|integer(int64)|false|none||id|
|userName|string|false|none||用户昵称|
|userAvatar|string|false|none||用户头像|
|userProfile|string|false|none||简介|
|userRole|string|false|none||用户角色：user/admin|

<h2 id="tocS_Duration">Duration</h2>

<a id="schemaduration"></a>
<a id="schema_Duration"></a>
<a id="tocSduration"></a>
<a id="tocsduration"></a>

```json
{
  "seconds": 0,
  "nanos": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|seconds|integer(int64)|false|none||The number of seconds in the duration.|
|nanos|integer|false|none||The number of nanoseconds in the duration, expressed as a fraction of the<br />number of seconds. This is always positive, and never exceeds 999,999,999.|

<h2 id="tocS_PageUserVO">PageUserVO</h2>

<a id="schemapageuservo"></a>
<a id="schema_PageUserVO"></a>
<a id="tocSpageuservo"></a>
<a id="tocspageuservo"></a>

```json
{
  "records": [
    {
      "id": 0,
      "userAccount": "string",
      "userName": "string",
      "userAvatar": "string",
      "userProfile": "string",
      "userRole": "string",
      "createTime": "string"
    }
  ],
  "pageNumber": 0,
  "pageSize": 0,
  "maxPageSize": 0,
  "totalPage": 0,
  "totalRow": 0,
  "optimizeCountQuery": true
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|records|[[UserVO](#schemauservo)]|false|none||none|
|pageNumber|integer(int64)|false|none||none|
|pageSize|integer(int64)|false|none||none|
|maxPageSize|integer(int64)|false|none||none|
|totalPage|integer(int64)|false|none||none|
|totalRow|integer(int64)|false|none||none|
|optimizeCountQuery|boolean|false|none||none|

<h2 id="tocS_ServerSentEventString">ServerSentEventString</h2>

<a id="schemaserversenteventstring"></a>
<a id="schema_ServerSentEventString"></a>
<a id="tocSserversenteventstring"></a>
<a id="tocsserversenteventstring"></a>

```json
{
  "id": "string",
  "event": "string",
  "retry": {
    "seconds": 0,
    "nanos": 0
  },
  "comment": "string",
  "data": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|id|string¦null|false|none||none|
|event|string¦null|false|none||none|
|retry|[Duration](#schemaduration)|false|none||none|
|comment|string¦null|false|none||none|
|data|string¦null|false|none||none|

<h2 id="tocS_BaseResponsePageUserVO">BaseResponsePageUserVO</h2>

<a id="schemabaseresponsepageuservo"></a>
<a id="schema_BaseResponsePageUserVO"></a>
<a id="tocSbaseresponsepageuservo"></a>
<a id="tocsbaseresponsepageuservo"></a>

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "id": 0,
        "userAccount": "string",
        "userName": "string",
        "userAvatar": "string",
        "userProfile": "string",
        "userRole": "string",
        "createTime": "string"
      }
    ],
    "pageNumber": 0,
    "pageSize": 0,
    "maxPageSize": 0,
    "totalPage": 0,
    "totalRow": 0,
    "optimizeCountQuery": true
  },
  "message": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|data|[PageUserVO](#schemapageuservo)|false|none||none|
|message|string|false|none||none|

<h2 id="tocS_BaseResponseString">BaseResponseString</h2>

<a id="schemabaseresponsestring"></a>
<a id="schema_BaseResponseString"></a>
<a id="tocSbaseresponsestring"></a>
<a id="tocsbaseresponsestring"></a>

```json
{
  "code": 0,
  "data": "string",
  "message": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|code|integer|false|none||none|
|data|string|false|none||none|
|message|string|false|none||none|

<h2 id="tocS_UserQueryRequest">UserQueryRequest</h2>

<a id="schemauserqueryrequest"></a>
<a id="schema_UserQueryRequest"></a>
<a id="tocSuserqueryrequest"></a>
<a id="tocsuserqueryrequest"></a>

```json
{
  "pageNum": 1,
  "pageSize": 10,
  "sortField": "string",
  "sortOrder": "descend",
  "id": 0,
  "userName": "string",
  "userAccount": "string",
  "userProfile": "string",
  "userRole": "string"
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|pageNum|integer|false|none||当前页号|
|pageSize|integer|false|none||页面大小|
|sortField|string|false|none||排序字段|
|sortOrder|string|false|none||排序顺序（默认降序）|
|id|integer(int64)|false|none||id|
|userName|string|false|none||用户昵称|
|userAccount|string|false|none||账号|
|userProfile|string|false|none||简介|
|userRole|string|false|none||用户角色：user/admin/ban|

<h2 id="tocS_AppDeployRequest">AppDeployRequest</h2>

<a id="schemaappdeployrequest"></a>
<a id="schema_AppDeployRequest"></a>
<a id="tocSappdeployrequest"></a>
<a id="tocsappdeployrequest"></a>

```json
{
  "appId": 0
}

```

### 属性

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|appId|integer(int64)|false|none||应用 id|

