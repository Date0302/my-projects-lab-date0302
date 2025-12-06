# Cognito Setup Guide（2025 最新版 UI）

本项目使用 Amazon Cognito 提供用户登录能力。  
Cognito 由两部分组成：

1. **User Pool**（用户注册、登录、托管 UI）
2. **Identity Pool**（为登录用户分配 AWS 临时凭证 → 用于 S3 上传）

本文档将说明如何使用 **新版控制台** 创建二者并正确连接。

---

# 1️⃣ 创建 User Pool（用户池）

进入：
**Amazon Cognito → User pools → Create user pool**

### ✔ 基本设置

- User pool name:  
  ```
  ImageAppUserPool
  ```

点击 **Next**。

---

# 2️⃣ Authentication（认证设置）

### ✔ Sign-in

选择：
- **Email**（推荐）

关闭 username（使用 email 更简单）。

选择完后 → Next。

---

# 3️⃣ Sign-up（注册设置）

保持默认：

- 用户可注册  
- 验证方式：Email  
- Confirm messages → 默认即可  

点击 Next。

---

# 4️⃣ MFA & password policy

- MFA: Optional（可选）
- Password policy: 默认即可

Next。

---

# 5️⃣ Create App Client（应用客户端）

点击：

➡ **Add app client**

设置：

- App client name：
  ```
  ImageAppClient
  ```
- Generate client secret：**关闭**（前端不能存 secret）
- Callback URL：  
  ```
  https://example.com
  ```
  （开发阶段可以随便写，后期替换前端域名）
- Logout URL：
  ```
  https://example.com
  ```

启用：

- Authorization code grant  
- Implicit grant（可选）  

保存。

---

# 6️⃣ 设置托管登录界面（Hosted UI）

左侧菜单：

**Branding → Domain**

### ✔ 设置托管域名

```
https://<your-domain>.auth.ap-northeast-1.amazoncognito.com
```

创建成功后即可用此 URL 登录。

---

# 7️⃣ Style（托管页面样式）

左侧菜单：

**Branding → Styles**

你可以：

- 自定义 logo  
- 自定义颜色  
- 修改 UI 文字（支持日文、中文等）

---

# 8️⃣ 创建 Identity Pool（身份池）

进入：

**User pools → Identity pools（右上角） → Create new identity pool**

### ✔ 选择 Authentication Access（推荐）

选择：
- **Authenticated access only**

点击 Next。

---

# 9️⃣ 绑定 User Pool 与 Identity Pool

在 "Authentication providers" 页面：

- Providers → Cognito user pool  
- User Pool ID：选择上一步创建的 User Pool  
- App Client ID：选择 ImageAppClient  

---

# 🔟 Role（IAM Role）

系统会要求为：

- Authenticated role  
- Guest role（不使用，可禁用）

自动创建 Role：

```
Cognito_ImageAppAuth_Role
```

此 Role 需要允许用户访问：

```
s3:PutObject
s3:GetObject
```

并且必须限定用户子目录：

```
user-uploads/${cognito-identity.amazonaws.com:sub}/*
```

示例策略见：`cognito-identity-policy.json`

---

# 1️⃣1️⃣ 测试 Login Flow

打开托管 UI 域名：

```
https://xxxx.auth.ap-northeast-1.amazoncognito.com/login
```

输入 email + 密码  
→ 登录成功  
→ 会跳转到设置的 redirect_url  
→ 前端可使用 Token 调用 API

---

# 🎉 完成！

你现在已经成功启用了：

✔ User Pool 登录  
✔ Hosted UI  
✔ Identity Pool 获取 AWS 临时凭证  
✔ 用户可安全上传 S3  

你的 Cognito 配置是完全企业级的。