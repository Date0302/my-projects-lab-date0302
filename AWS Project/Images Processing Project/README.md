# AWS Serverless Image Upload & Processing System

This project is a high-performance, scalable image upload and processing system built on the AWS Serverless architecture. The core logic consists of three collaboratively working Lambda functions, implementing end-to-end functionality from secure upload and asynchronous processing to result notification and monitoring.

## System Architecture Overview

**Core Features**

- **Fully Serverless Architecture:** Based on managed services like Lambda, S3, and SQS; auto-scaling with no servers to manage.
- **Asynchronous Decoupled Design:** Separates upload and processing workflows using SQS Standard Queue, enhancing system reliability and resilience under load.
- **Secure Storage Separation:** Original and processed images are stored separately, with upload security ensured via Cognito authentication.
- **Production-Grade Observability:** Fully integrated with CloudWatch monitoring and SNS notifications for easy tracking and alerting.
- **Metadata Management:** Uses DynamoDB to store all image metadata for easy tracking and querying.

### Lambda Functions Detailed Explanation

The system includes the following three core Lambda functions:

#### 1. Generate-Upload-URL Lambda

**Functionality**

- Generates a secure S3 pre-signed upload URL (PUT method) for authenticated Cognito users.
- Automatically categorizes each user's uploaded images into their dedicated directory.

**Input/Output**

- **Input:** User identity (Cognito User ID) automatically passed via API Gateway's JWT authorizer.

- 

  **Output:**

  ```
  {
    "upload_url": "<PresignedURL>",
    "file_path": "user-uploads/<userId>/<uuid>.jpg"
  }
  ```

**Security Features**

- Can only be invoked by logged-in Cognito users.
- Pre-signed URL defaults to a 5-minute validity period to prevent misuse.
- Ensures user data security through directory isolation, preventing overwriting of others' files.

#### 2. Image Processor Lambda

This is the core processing unit of the system.

**Functionality**

- Receives image upload event messages from the SQS Standard Queue.
- Downloads the original image from the source S3 bucket.
- Performs image processing (e.g., generating thumbnails, format conversion) using the Pillow library.
- Uploads the processed results to the designated target S3 bucket.
- Writes processing status and metadata (e.g., image ID, S3 paths, status) to a DynamoDB table.
- Sends processing result notifications (success/failure) to an SNS topic.

**Advantages of Backend Asynchronous Processing**

- **High Concurrency Processing:** Leverages Lambda's auto-scaling capability to handle traffic fluctuations.
- **Decoupling & Reliability:** SQS acts as a buffer layer, ensuring tasks are not lost if the processing component fails.
- **Task Independence:** Each processing task is isolated; failure of a single task does not affect the overall system.

**Output Example (Written to DynamoDB)**

```
{
  "imageId": "uuid",
  "originalS3Key": "user-uploads/xxx/xxx.jpg",
  "processedS3Key": "processed/uuid.jpg",
  "status": "DONE",
  "lastModified": "2023-10-01T12:00:00Z"
}
```

#### 3. S3 Metrics Monitoring Lambda

**Functionality**

- Periodically (configurable, e.g., 1 minute, 5 minutes, 1 hour) statistics the size and object count of a specified S3 bucket.
- Reports custom metrics (e.g., `BucketSizeBytes`, `NumberOfObjects`) to CloudWatch.

**Purpose**

- **Cost Monitoring & Reporting:** Monitors storage growth trends to support cost optimization.
- **Proactive Alerting:** Set CloudWatch alarms based on metrics (e.g., storage capacity thresholds).

**Dependencies**

- Uses only the `boto3`SDK, with no additional third-party library dependencies.

## Monitoring & Operations

The system is fully integrated with CloudWatch for comprehensive monitoring:

- View execution logs, duration, and error rates for each Lambda function.
- Set alarms in the CloudWatch console based on custom metrics (metrics under the `CustomS3`namespace).
- Receive processing success or failure messages via SNS notifications.

#  AWS Serverless 图像上传与处理系统

本项目是一个构建在 AWS Serverless 架构上的高性能、可扩展的图像上传与处理系统。系统核心逻辑由三个协同工作的 Lambda 函数组成，实现了从安全上传、异步处理到结果通知和监控的全链路功能。

## 系统架构总览

核心特性

- 完全无服务器架构：基于 Lambda、S3、SQS 等托管服务，自动扩展，无需管理服务器
- 异步解耦设计：通过 SQS 标准队列分离上传与处理流程，增强系统可靠性和抗压能力
- 安全的存储分离：原始图像与处理后的图像分别存储，并通过 Cognito 进行身份验证保障上传安全
- 生产级可观测性：完整集成 CloudWatch 监控和 SNS 通知，便于监控和告警
- 元数据管理：使用 DynamoDB 存储所有图像的元数据，便于追踪和查询

### Lambda 函数详解

系统包含以下三个核心 Lambda 函数：

#### 1.Generate-Upload-URL Lambda

功能

- 为已通过 Cognito 认证的用户生成安全的 S3 预签名上传 URL（PUT 方法）
- 自动将每个用户上传的图像归类到其专属目录

输入/输出

- 输入：通过 API Gateway 的 JWT authorizer 自动传递的用户身份（Cognito User ID）
- 输出：
{
  "upload_url": "<PresignedURL>",
  "file_path": "user-uploads/<userId>/<uuid>.jpg"
}

安全特性

- 仅允许已登录的 Cognito 用户调用
- 预签名 URL 有效期默认为 5 分钟，防止滥用
- 通过目录隔离机制，确保用户数据安全，防止覆盖他人文件

#### 2.Image Processor Lambda 

这是系统的核心处理单元。

功能

- 从 SQS 标准队列接收图像上传事件消息
- 从 S3 原始桶下载原始图像
- 使用 Pillow 库进行图像处理（如生成缩略图、格式转换等）
- 将处理后的结果上传至指定的目标 S3 桶
- 将处理状态和元数据（如图像ID、S3路径、状态）写入 DynamoDB 表
- 发送处理结果通知（成功/失败）到 SNS 主题

后端异步处理优势

- 高并发处理：利用 Lambda 的自动扩缩容能力应对流量波动
- 解耦与可靠性：SQS 作为缓冲层，确保处理组件故障时任务不丢失
- 任务独立性：每个处理任务相互隔离，单个任务失败不影响整体系统

输出示例（写入DynamoDB）

{
  "imageId": "uuid",
  "originalS3Key": "user-uploads/xxx/xxx.jpg",
  "processedS3Key": "processed/uuid.jpg",
  "status": "DONE",
  "lastModified": "2023-10-01T12:00:00Z"
}

#### 3.S3 Metrics Monitoring Lambda

功能

- 定时（可配置为1分钟、5分钟、1小时等）统计指定 S3 桶的容量和对象数量
- 将自定义指标（如 
"BucketSizeBytes"、
"NumberOfObjects"）上报至 CloudWatch

用途

- 成本监控与报表：监控存储增长趋势，为成本优化提供数据支持
- 主动告警：基于 CloudWatch 指标设置告警（如存储容量阈值）

依赖

- 仅使用 
"boto3" SDK，无额外第三方库依赖

## 监控与运维

系统已集成 CloudWatch 进行全方位监控：

- 查看各 Lambda 函数的执行日志、持续时间、错误率
- 在 CloudWatch 控制台设置基于自定义指标（如 
"CustomS3" 命名空间下的指标）的告警
- 通过 SNS 通知接收处理成功或失败的消息
