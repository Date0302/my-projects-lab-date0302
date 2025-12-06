# SQS + Lambda Flow（图像处理异步架构）

本项目使用 SQS 解耦 S3 上传和图像处理。  
这是企业级常用架构，可以：

- 自动扩展
- 避免高峰期拥堵
- 确保任务不丢失
- 做重试、死信队列

---

# 🏗 系统流程图

```
User → Upload → S3 → Event → SQS → Lambda(image-processor)
                       ↓
                    Dead Letter Queue
```

---

# 1️⃣ S3 上传完成 → 推送消息到 SQS

S3 event 结构：

```json
{
  "bucket": "my-unique-image-upload-bucket",
  "s3Key": "user-uploads/<userId>/<uuid>.jpg",
  "imageId": "<uuid>"
}
```

---

# 2️⃣ SQS 收到消息

Queue Example：
```
image-processing-queue
```

Queue Attributes：

- Visibility timeout：>= Lambda timeout * 6  
- Receive message wait time：10 seconds  
- DLQ：image-processing-dlq  

---

# 3️⃣ Lambda（image-processor-lambda）从 SQS 取消息

IAM 权限：

```json
"sqs:ReceiveMessage"
"sqs:DeleteMessage"
```

流程：

1. 接收消息  
2. 下载原图（S3）  
3. Pillow 生成缩略图  
4. 上传处理图到另一个 bucket  
5. 写入 DynamoDB  
6. 发送 SNS 通知  

---

# 4️⃣ 死信队列（Dead Letter Queue）

DLQ 名称：

```
image-processing-dlq
```

用途：

- Lambda 连续处理失败 → 自动进入 DLQ  
- 可用于排查损坏文件、格式错误文件  
- 保证不会丢数据  

---

# 5️⃣ Lambda 自动扩展

SQS → Lambda 是自动扩容的：

- 消息多 → Lambda 实例高速扩增  
- 消息少 → 自动缩减到 0  
- 无需手动管理服务器  

---

# 🎉 总结

SQS + Lambda 让你的系统具备：

✔ 高扩展性  
✔ 高可靠性  
✔ 全自动重试  
✔ 数据不会丢失  
✔ 强大的错误处理能力  

这是企业中最常见的事件驱动架构，非常适合生产环境。