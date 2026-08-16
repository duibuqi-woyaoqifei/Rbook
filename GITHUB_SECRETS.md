# GitHub Actions Secret 配置清单（一次性初始化）

目标：把 Android 签名所需敏感信息一次性配置到 GitHub Secrets，之后只推送 `v*` tag
（例如 `v1.2.3`）即可自动构建、签名、发布 Release，无需再手动配置任何 Secret。

> 本仓库的发布流程**不会**自己创建 Secret（存在循环依赖），因此由你手动配置一次。
> 配置入口：`https://github.com/duibuqi-woyaoqifei/Rbook/settings/secrets/actions`

---

## 一、需要手动创建的 Secret 清单

| Secret 名称 | 取值 / 来源 | 是否必需 | 说明 |
|-------------|-------------|:--------:|------|
| `SIGNING_KEY` | 整个 keystore 文件的 **Base64 编码**（见下文生成方法） | ✅ 必需 | Actions 中解码恢复 keystore 文件用于签名 |
| `KEY_STORE_PASSWORD` | 生成 keystore 时设置的 **存储口令（storepass）** | ✅ 必需 | `apksigner --ks-pass` 使用 |
| `KEY_PASSWORD` | 生成 keystore 时设置的 **key 口令（keypass）** | ✅ 必需 | `apksigner --key-pass` 使用 |
| `BLOG_REPO_TOKEN` | 可对本仓库及 `jinlun-blog` 仓库 dispatch 的 token | ⚠️ 可选 | 仅当你要保留 workflow 末尾“通知博客仓库”步骤时才需要；不需要时请同时删除该步骤 |

> ℹ️ **不需要** `ALIAS`：key alias 已在 workflow 中硬编码为 `key0`（与你的
> keystore 一致），旧 Secret `ALIAS` 可以直接删除。

> ✅ **不需要**创建 `GITHUB_TOKEN`：Actions 的 构建/发布 步骤使用的是自动提供的
> `github.token`（workflow 已在 job 上声明 `permissions: contents: write`）。
> 请**不要**手动创建同名 Secret，避免覆盖/混淆。

---

## 二、如何生成 `SIGNING_KEY`（keystore 的 Base64）

你的 keystore 位于（**不会提交到 Git，也不在仓库内**）：
`D:\document\keyStore\Rbook\Rbook-release-key`

在 PowerShell 中执行（生成一行式 Base64，避免换行问题）：

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("D:\document\keyStore\Rbook\Rbook-release-key")) | Set-Content -NoNewline keystore_base64.txt
```

然后：
1. 打开 `keystore_base64.txt`，**全选复制**全部内容；
2. 在 GitHub Secrets 页面新建名为 `SIGNING_KEY` 的 Secret，粘贴进去；
3. 配置完成后**立即删除**本地的 `keystore_base64.txt`（它包含可还原的敏感数据）。

> 校验：GitHub 单个 Secret 值上限 64 KB，一般 keystore 远小于此。
> 若文件较大，说明需要检查（正常 PKCS12 keystore 只有几百字节到几 KB）。

---

## 三、配置完成后的自检

1. 打开 `https://github.com/duibuqi-woyaoqifei/Rbook/settings/secrets/actions`，确认
   `SIGNING_KEY`、`KEY_STORE_PASSWORD`、`KEY_PASSWORD` 均已存在（`ALIAS` 无需配置，可删除）。
2. 推送一个新的版本 tag 触发流程：
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
3. 在 `Actions` 标签页观察 `Android Release CI` 是否成功；成功后
   `Releases` 页会出现对应 Release，并附带已用 `key0` 签名的 APK。

---

## 四、发布流程说明（对照 `.github/workflows/release.yml`）

- 触发：推送 `v*` tag（如 `v1.2.3`）。
- 版本号只来自 tag：workflow 解析 tag 得到 `versionName = x.x.x`，`versionCode` 基于
  `major*10000 + minor*100 + patch` 计算，并用 git 历史 tag 保证严格大于所有既往发布版本码；
  通过 `-PRBOOK_VERSION_NAME / -PRBOOK_VERSION_CODE` 注入 Gradle，**不修改** `build.gradle.kts`。
- 构建 `assembleRelease`（开启 R8 混淆与资源压缩）。
- 手动签名：`base64 -d` 恢复 keystore → `zipalign` → `apksigner`（alias `key0`）。
- 创建 GitHub Release 并上传签好名的 APK（同名已存在则覆盖）。
- 部署 `rbook-latest.apk` 与 `update.json` 到 `release-bin` 分支；`update.json` 含
  `versionCode` 字段，App 端仅在 `remoteVersionCode > BuildConfig.VERSION_CODE` 时提示更新。
- （可选）dispatch 到 `jinlun-blog` 通知版本更新。
