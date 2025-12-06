# **Document**

## Table of Contents

1. System Architecture Overview
2. Prerequisites
3. Create S3 Buckets (Original / Processed)
4. Create SQS Queue (Standard or FIFO)
5. Create DynamoDB Table (Metadata Table)
6. Create SNS Topic (Notifications)
7. Create Lambdas (3 Functions)
8. Configure IAM + Permission Boundaries
9. Configure Cognito (User Pool + Identity Pool)
10. Create API Gateway
11. Test the Complete Flow
12. Post-Deployment Operations (CloudWatch Metrics)

### 1. System Architecture Overview

This project uses a fully Serverless architecture:

```
Cognito → API Gateway → Lambda → S3 → SQS → Lambda → S3 → DynamoDB → SNS
```

Includes three Lambdas: Generate-Upload-URL, image-processor-lambda, s3-metrics-lambda

### 2. Prerequisites

You need:

- An AWS Account
- Selected **Region**: `ap-northeast-1`(Tokyo)
- Administrator permissions (or equivalent permissions)

### 3. Create S3 Buckets (Original + Processed)

#### Original Bucket (User Uploads)

Example name:

```
my-unique-image-upload-bucket
```

**Settings:**

- **Block public access:** Enabled
- **Bucket policy:** Enforce HTTPS
- Enable **CORS** (Allow PUT, GET)
- **Lifecycle rules** (Optional) Transition to Standard-IA after 30 days Transition to Glacier after 90 days

------

#### Processed Bucket

```
my-image-processed-first
```

**Settings are the same as above.**

### 4. Create SQS Queue

Go to: SQS → Create Queue

- **Type:** **Standard** or **FIFO**
- **Name:** `image-processing-queue`
- Configure **DLQ** (Dead Letter Queue)

DLQ Name:

```
image-processing-dlq
```

### 5. Create DynamoDB Table

**Table Name:**

```
ImageMetadata
```

**Partition Key:**

```
imageId (String)
```

**Capacity mode:** On-demand

### 6. Create SNS Topic

**Topic Name:**

```
image-processed-topic
```

Get the Topic ARN:

```
arn:aws:sns:ap-northeast-1:<account-id>:image-processed-topic
```

### 7. Create Lambdas (3 Functions)

#### 1. GenerateUploadURL

- **Runtime:** Python 3.12
- Invoked via API Gateway
- **IAM Permissions required:** `s3:PutObject` `s3:GetObject` `sts:GetCallerIdentity` `cognito-identity:GetId`

------

#### 2. image-processor-lambda

- **Runtime:** Python 3.12
- Add a **Pillow Layer**
- **Event Source:** SQS
- **IAM Permissions required:** **S3:** `GetObject`/ `PutObject` **DynamoDB:** `PutItem` **SNS:** `Publish`

------

#### 3. s3-metrics-lambda

- Executes every **5 minutes**
- **IAM Permissions required:** `s3:ListBucket` `cloudwatch:PutMetricData`

------

### 8. Configure IAM and Permission Boundaries

Create for the project:

- Lambda Execution Roles
- SQS Access Policy
- DynamoDB Access Policy
- S3 Minimum Permission Policy
- SNS Publish Permission
- Cognito Identity Unified Permissions (Identity Pool role)

------

### 9. Configure Cognito

Consists of **two parts**:

#### User Pool

- Create a login and registration entry point
- **Username:** Email
- Enable **Hosted UI**
- Configure Domain

#### Identity Pool

- Allows obtaining temporary credentials via the User Pool
- Assigns an **IAM Role** (Authenticated role)
- Allows `s3:PutObject`, `s3:GetObject`to the user's directory

------

### 10. Create API Gateway

- Create an API

- **Authorizer:** Cognito User Pool → JWT

- 

  **Path:**

  ```
  POST /generate-upload-url
  ```

- **Integration:** **Lambda:** GenerateUploadURL

- 

  Enable **CORS:**

  ```
  Allowed origins: *
  Allowed methods: OPTIONS, POST
  Headers: Content-Type, Authorization, X-Amz-Date
  ```

------

### 11. Test the Complete Flow

You can test by following these steps:

#### Step 1 – Login

Visit the Cognito Hosted UI → Login to obtain a Token.

#### Step 2 – Request Upload URL

Use Postman or a frontend application to call:

```
POST /generate-upload-url
Authorization: Bearer <JWT>
```

Returns:

```
upload_url
file_path
```

#### Step 3 – Upload to S3

Perform a PUT request to upload the image to the `upload_url`.

#### Step 4 – Check if SQS Queue Received the Message

#### Step 5 – Check if Lambda Processed the Image and Generated Thumbnails

#### Step 6 – Check if DynamoDB Wrote the Record

#### Step 7 – Check if SNS Sent the Email

------

### 12. CloudWatch Metrics & Operations

The `s3-metrics-lambda`will periodically generate metrics:

- `BucketSizeBytes`
- `NumberOfObjects`

You can create: Alarms, Dashboards

------

## 🎉🎉🎉 Deployment Successful

# Deployment

## 目录

1. 系统架构概览
2. 部署前准备
3. 创建 S3（原图 / 处理图）
4. 创建 SQS（标准或 FIFO）
5. 创建 DynamoDB（元数据表）
6. 创建 SNS（通知）
7. 创建 Lambda（3 个）
8. 配置 IAM + 权限边界
9. 配置 Cognito（User Pool + Identity Pool）
10. 创建 API Gateway
11. 测试整个流程
12. 后续运维（CloudWatch Metrics）

### 1.系统架构概览

本项目采用完全 Serverless 架构：

```
Cognito → API Gateway → Lambda → S3 → SQS → Lambda → S3 → DynamoDB → SNS
```

包含三个 Lambda：Generate-Upload-URL，image-processor-lambda，s3-metrics-lambda

### 2.部署前准备

你需要：

- AWS 账号
- 已选择 可用区：ap-northeast-1（东京）
- 管理员权限（或等效权限）

### 3.创建 S3 Bucket（原图 + 处理图）

#### 原图 bucket（用户上传）

名字示例：

```
my-unique-image-upload-bucket
```

**设置：**

- Block public access：开启
- Bucket policy：强制 HTTPS
- 启用 CORS（允许 PUT、GET）
- 生命周期（可选）
  - 30 天后转 IA
  - 90 天后转 Glacier

------

#### 处理后 bucket

```
my-image-processed-first
```

**设置同上**

### 4.创建 SQS 队列

进入：SQS → Create Queue

- 类型：**Standard 或 FIFO**
- 名称：`image-processing-queue`
- 配置 DLQ（死信队列）

DLQ 名称：

```
image-processing-dlq
```

### 5.创建 DynamoDB

Name：

```
ImageMetadata
```

Partition key：

```
imageId (String)
```

容量模式：按需（On-demand）

### 6.创建 SNS Topic

Topic 名：

```
image-processed-topic
```

获取 Topic ARN：

```
arn:aws:sns:ap-northeast-1:<account-id>:image-processed-topic
```

### 7.创建 Lambda（3 个）

#### 1. GenerateUploadURL

- Runtime：Python 3.12
- 使用 API Gateway 调用
- IAM 权限：
  - s3:PutObject
  - s3:GetObject
  - sts:GetCallerIdentity
  - cognito-identity:GetId

------

#### 2. image-processor-lambda

- Runtime：Python 3.12
- 添加 Pillow Layer
- Event Source：SQS
- IAM 权限：
  - S3：GetObject / PutObject
  - DynamoDB：PutItem
  - SNS：Publish

------

#### 3. s3-metrics-lambda

- 每 5 分钟执行一次
- IAM 权限：
  - s3:ListBucket
  - CloudWatch:PutMetricData

------

### 8.配置 IAM 与权限边界

为项目创建：

- Lambda 执行角色
- SQS 访问策略
- DynamoDB 访问策略
- S3 最小权限策略
- SNS publish 权限
- Cognito 身份统一权限（Identity Pool role）

------

### 9.配置 Cognito

包含 **两个部分**：

#### User Pool

- 创建登录与注册入口
- username：Email
- 启用托管登录（Hosted UI）
- 配置域名

#### Identity Pool

- 允许通过 User Pool 获取临时凭证
- 分配 IAM Role（Authenticated role）
- 允许 S3 PutObject、GetObject 到用户目录

------

### 10.创建 API Gateway

- 创建 API 
- Authorizer：Cognito User Pool → JWT
- 子路径：

```
POST /generate-upload-url
```

集成：

- Lambda：GenerateUploadURL

启用 CORS：

```
Allowed origins: *
Allowed methods: OPTIONS, POST
Headers: Content-Type, Authorization, X-Amz-Date
```

------

### 11.测试完整流程

你可以按以下步骤测试：

#### Step 1 – 登录

访问 Cognito Hosted UI → 登录获取 Token

#### Step 2 – 请求上传 URL

使用 Postman 或前端调用：

```
POST /generate-upload-url
Authorization: Bearer <JWT>
```

返回：

```
upload_url
file_path
```

#### Step 3 – 上传到 S3

PUT 上传图像到 upload-url。

#### Step 4 – 查看 SQS 队列是否收到消息

#### Step 5 – Lambda 是否处理、生成缩略图

#### Step 6 – DynamoDB 是否写入记录

#### Step 7 – SNS 是否发送邮件

------

### 12.CloudWatch Metrics 运维

`s3-metrics-lambda` 会定期生成指标：

- `BucketSizeBytes`
- `NumberOfObjects`

你可以创建：Alarm，Dashboard

------

## 🎉 部署成功！