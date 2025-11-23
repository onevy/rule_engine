# 规则引擎管理系统

## 项目简介

医疗规则引擎系统是一个基于Drools规则引擎的通用业务规则管理平台,支持传染病上报、处方审核、病历质控等多种医疗业务场景。
系统提供可视化的规则配置界面,支持通过前端的配置界面将规则转换为Drools语法并存储到数据库中；
在业务系统使用时，从数据库中读取的Drools语法，然后执行。并支持复杂条件组合和动态规则编译执行。



## 技术栈

### 后端
- **框架**: Spring Boot 2.7.14 + JDK 11
- **规则引擎**: Drools 7.73.0.Final
- **ORM**: MyBatis-Plus 3.5.3
- **数据库**: MySQL 8.0+
- **缓存**: Redis 6.2+ / Caffeine
- **安全**: Spring Security + JWT
- **文档**: Knife4j 4.0.0

### 前端
- **框架**: Vue 3.3+ + JavaScript
- **构建**: Vite 4.4+
- **UI组件**: Element Plus 2.3+
- **状态管理**: Pinia 2.1+
- **路由**: Vue Router 4.2+
- **HTTP**: Axios 1.4+

### 部署
- **容器**: Docker + Docker Compose
- **反向代理**: Nginx

## 项目结构

```
rule_demo1/
├── backend/                    # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/ruleengine/
│   │   │   │       ├── common/          # 通用模块
│   │   │   │       ├── controller/      # 控制器层
│   │   │   │       ├── service/         # 服务层
│   │   │   │       ├── mapper/          # 数据访问层
│   │   │   │       ├── entity/          # 实体类
│   │   │   │       ├── dto/             # 数据传输对象
│   │   │   │       ├── adapter/         # 业务适配器
│   │   │   │       └── engine/          # 规则引擎核心
│   │   │   └── resources/
│   │   │       ├── mapper/              # MyBatis XML
│   │   │       ├── drools/              # Drools规则文件
│   │   │       └── application*.yml     # 配置文件
│   │   └── test/                        # 测试代码
│   ├── pom.xml                          # Maven配置
│   └── Dockerfile                       # Docker构建文件
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/                # API接口
│   │   ├── components/         # 组件
│   │   ├── views/              # 页面
│   │   ├── router/             # 路由配置
│   │   ├── store/              # 状态管理
│   │   ├── types/              # 类型定义
│   │   ├── utils/              # 工具函数
│   │   └── styles/             # 样式文件
│   ├── package.json            # NPM配置
│   ├── vite.config.ts          # Vite配置
│   ├── tsconfig.json           # TypeScript配置
│   ├── nginx.conf              # Nginx配置
│   └── Dockerfile              # Docker构建文件
├── docs/                       # 文档目录
│   ├── 需求规格说明书.md
│   ├── 系统架构设计文档.md
│   ├── 数据库设计文档.md
│   ├── API接口设计文档.md
│   └── 技术选型与代码规范.md
├── docker-compose.yml          # Docker Compose配置
└── README.md                   # 项目说明文档
```

## 快速开始

### 前置要求

- JDK 11+
- Node.js 18+
- MySQL 8.0+
- Redis 6.2+
- Docker & Docker Compose (可选)

### 本地开发

#### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p
CREATE DATABASE rule_engine CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

# 导入数据库表结构
mysql -u root -p rule_engine < docs/database/schema.sql

# 导入初始化数据
mysql -u root -p rule_engine < docs/database/init_data.sql
```

#### 2. 启动后端服务

```bash
cd backend

# 修改配置文件
vim src/main/resources/application-dev.yml
# 配置数据库连接、Redis连接等

# 编译并运行
mvn clean install
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动
API文档地址: http://localhost:8080/api/doc.html

#### 3. 启动前端服务

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 http://localhost:3000 启动

### Docker 部署

#### 使用 Docker Compose 一键部署

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

服务访问地址:
- 前端: http://localhost
- 后端API: http://localhost/api
- API文档: http://localhost/api/doc.html

#### 单独构建镜像

```bash
# 构建后端镜像
cd backend
docker build -t rule-engine-backend:latest .

# 构建前端镜像
cd frontend
docker build -t rule-engine-frontend:latest .
```

## 核心功能

### 1. 业务场景管理
- 场景配置与启用/禁用
- 场景字段元数据管理
- 业务适配器配置

### 2. 规则管理
- 可视化规则创建与编辑
- 规则组织与分组
- 规则版本管理
- 规则启用/禁用/草稿状态

### 3. 条件配置
- 支持AND/OR逻辑组合
- 支持多层嵌套条件组
- 15种常用操作符(EQ, IN, GT, LT, CONTAINS等)
- 可视化Query Builder组件

### 4. 规则执行
- 实时规则编译
- 高性能规则执行(KieBase缓存)
- 规则执行日志记录
- 批量规则执行

### 5. 权限管理
- RBAC权限模型
- 6种预定义角色
- 细粒度权限控制
- JWT Token认证

### 6. 审计日志
- 用户操作审计
- 规则执行日志
- 数据变更记录

## 开发指南

### 后端开发

#### 创建新的业务适配器

```java
@Component
public class CustomBusinessAdapter implements BusinessAdapter {

    @Override
    public String getSceneCode() {
        return "CUSTOM_SCENE";
    }

    @Override
    public Map<String, Object> adaptInput(Object businessData) {
        // 将业务数据转换为规则引擎输入格式
        return convertedData;
    }

    @Override
    public void processResult(RuleResult result, Object businessData) {
        // 处理规则执行结果
    }
}
```

#### 添加新的Controller

```java
@RestController
@RequestMapping("/api/v1/custom")
@Tag(name = "自定义模块")
public class CustomController {

    @GetMapping
    @Operation(summary = "查询列表")
    public Result<List<CustomVO>> list() {
        // 实现逻辑
    }
}
```

### 前端开发

#### 创建新页面

```typescript
// src/views/custom/index.vue
<template>
  <div class="custom-container">
    <!-- 页面内容 -->
  </div>
</template>

<script setup lang="ts">
// 页面逻辑
</script>
```

#### 添加API接口

```typescript
// src/api/custom.ts
import request from './request'

export const getCustomList = () => {
  return request({
    url: '/custom',
    method: 'get'
  })
}
```

#### 添加路由

```typescript
// src/router/index.ts
{
  path: '/custom',
  name: 'Custom',
  component: () => import('@/views/custom/index.vue'),
  meta: { title: '自定义模块', requiresAuth: true }
}
```

## 代码规范

### 后端代码规范
- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 所有公共方法添加注释
- 使用统一的Result响应对象
- 异常使用GlobalExceptionHandler处理

### 前端代码规范
- 使用JavaScript严格模式
- 遵循Vue 3 Composition API最佳实践
- 使用ESLint和Prettier格式化代码
- 组件使用`<script setup>`语法
- 所有API返回值定义类型

## 测试

### 后端测试

```bash
cd backend

# 运行单元测试
mvn test

# 生成测试覆盖率报告
mvn clean test jacoco:report
```

### 前端测试

```bash
cd frontend

# 运行单元测试
npm run test

# 运行E2E测试
npm run test:e2e
```

## 文档

详细文档请参考 `docs/` 目录:

- [需求规格说明书](./docs/需求规格说明书.md)
- [系统架构设计文档](./docs/系统架构设计文档.md)
- [数据库设计文档](./docs/数据库设计文档.md)
- [API接口设计文档](./docs/API接口设计文档.md)
- [技术选型与代码规范](./docs/技术选型与代码规范.md)

## 常见问题

### 1. 数据库连接失败
检查MySQL是否启动,数据库配置是否正确

### 2. Redis连接失败
检查Redis是否启动,配置是否正确

### 3. 前端启动报错
删除`node_modules`和`package-lock.json`,重新执行`npm install`

### 4. Docker构建失败
检查Docker版本,确保Docker服务正常运行

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'feat: add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 提交 Pull Request

## 许可证

本项目采用 Apache 2.0 许可证

## 联系方式

- 项目地址: https://github.com/example/rule-engine
- 问题反馈: https://github.com/example/rule-engine/issues
- 邮箱: dev@example.com

## 更新日志

### v1.0.0 (2025-01-01)
- 初始版本发布
- 支持传染病上报、处方审核、病历质控三大场景
- 完成规则引擎核心功能
- 完成前后端基础框架搭建
