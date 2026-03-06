# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Markdown 文档检索系统 - 基于 Spring Boot 的混合检索服务

**技术栈:**
- JDK 8
- Spring Boot 2.7.18
- Elasticsearch 7.1.1
- Maven

**核心功能:**
- Markdown 文档索引（单文件、批量、目录扫描）
- 混合检索（BM25 + 标题向量 + 内容向量，可配置权重）
- 访问记录与统计

## Commands

```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包
mvn clean package -DskipTests

# 运行应用
mvn spring-boot:run

# 或直接运行 jar
java -jar target/markdown-search-1.0.0.jar
```

## Architecture

```
src/main/java/com/example/mdsearch/
├── MarkdownSearchApplication.java   # 入口类
├── config/                          # 配置
│   ├── ElasticsearchConfig.java     # ES 客户端配置
│   └── SearchProperties.java        # 检索权重、Embedding API 配置
├── controller/                      # REST API
│   ├── DocumentController.java      # 文档索引 API
│   ├── SearchController.java        # 混合检索 API
│   └── VisitController.java         # 访问记录 API
├── service/                         # 服务接口
│   └── impl/                        # 服务实现
├── model/                           # 数据模型
├── repository/                      # ES 数据访问层
└── util/                            # 工具类
```

**核心服务:**
- `IndexService` - 文档索引（解析 MD、生成向量、写入 ES）
- `SearchService` - 混合检索（BM25 + 向量检索融合）
- `EmbeddingService` - 调用外部 API 生成向量
- `VisitService` - 访问日志记录与统计

## Configuration

配置文件: `src/main/resources/application.yml`

关键配置项:
```yaml
mdsearch:
  search:
    bm25-weight: 0.4        # BM25 权重
    title-vector-weight: 0.3  # 标题向量权重
    content-vector-weight: 0.3 # 内容向量权重
  embedding:
    api-url: https://api.openai.rnd.huawei.com/v1
    api-key: sk-1234
    dimension: 1536         # 向量维度，需与 API 对齐
```

## API Endpoints

| 端点 | 方法 | 说明 |
|-----|------|-----|
| `/api/documents` | POST | 索引单个文档 |
| `/api/documents/directory` | POST | 索引目录 |
| `/api/search` | GET/POST | 混合检索 |
| `/api/search/bm25` | POST | BM25 检索 |
| `/api/search/vector` | POST | 向量检索 |
| `/api/visit/{docId}` | POST | 记录访问 |
| `/api/visit/stats/popular` | GET | 热门文档统计 |
