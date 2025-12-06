# S3 Setup Guide

本项目使用两个 S3 bucket：

1. `my-unique-image-upload-bucket`（用户上传原图）
2. `my-image-processed-first`（Lambda 处理后的图片）

本文档介绍创建与强化安全的所有步骤。

---

# 1️⃣ 创建 S3 Bucket（原图）

进入：
**Amazon S3 → Create bucket**

### ✔ 设置：

- Bucket name:
  ```
  my-unique-image-upload-bucket
  ```
- Region: ap-northeast-1（东京）
- Object ownership: Bucket owner enforced
- Block Public Access: ON（全部开启）

点击 Create。

---

# 2️⃣ 强制 HTTPS（非常重要）

进入 bucket → Permissions → Bucket policy  
添加：

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Deny",
      "Principal": "*",
      "Action": "s3:*",
      "Resource": [
        "arn:aws:s3:::my-unique-image-upload-bucket",
        "arn:aws:s3:::my-unique-image-upload-bucket/*"
      ],
      "Condition": {
        "Bool": {
          "aws:SecureTransport": "false"
        }
      }
    }
  ]
}
```

---

# 3️⃣ 设置 CORS（为预签名 URL 上传做准备）

进入：**Permissions → CORS**

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["PUT", "GET", "HEAD"],
    "AllowedOrigins": ["*"],
    "ExposeHeaders": []
  }
]
```

---

# 4️⃣ 配置生命周期（Lifecycle）

进入：

**Management → Lifecycle rules → Create rule**

示例策略：

### Rule 1：用户上传图像

- 30 天后移动到 Standard-IA  
- 90 天后移动到 Glacier Flexible Retrieval  

---

# 5️⃣ 创建第二个 bucket（处理后的图像）

```
my-image-processed-first
```

设置与第一个相同。

---

# 6️⃣ 使用前缀隔离用户目录

预签名 URL 的上传路径是：

```
user-uploads/<identity-id>/<uuid>.jpg
```

这种结构能有效保证用户间不会互相覆盖权限。

---

# 🎉 S3 设置完成！

你的存储已具备：

✔ HTTPS 强制  
✔ CORS 支持  
✔ 生命周期自动化  
✔ 用户隔离子目录  
✔ 安全上传机制  