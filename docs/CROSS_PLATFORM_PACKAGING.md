# FreeFlow (自由发) 跨平台兼容与双端打包方案

## 1. 为什么原版机制需要区分桌面引擎与移动 APK？

`xingguangYan/freeflow` 的核心发布能力依赖 **本地浏览器自动化（Playwright / Chromium）** 维持各内容平台的登录 Cookie 与富文本编辑器注入。由于 Android 系统沙箱无法直接运行桌面版 Chromium/Playwright 二进制文件，跨平台最佳架构为 **「桌面执行引擎 + 移动端原生口袋创作台」** 或 **「Kotlin Multiplatform 统一 UI + 本地 Node/Playwright Sidecar」**。

---

## 2. 本次 Android APK 专项优化点

1. **原生 Jetpack Compose 替代 WebView / Capacitor 壳**：启动更快、输入法键盘防遮挡（`adjustResize` + `imePadding`）、支持折叠屏与平板自适应（`BoxWithConstraints` 自动切换 `NavigationBar` / `NavigationRail`）。
2. **APK 体积精简**：移除了未使用的 Firebase/Retrofit/Moshi 反射库，仅保留核心 Compose + Room + DataStore + OkHttp + Coil。
3. **零权限合规选图**：使用系统 `ActivityResultContracts.PickVisualMedia` 选取封面与多图素材，无需申请 `READ_MEDIA_IMAGES` / `READ_EXTERNAL_STORAGE`。
4. **局域网直连（LAN Bridge）**：通过 `network_security_config.xml` 允许本地局域网 HTTP 通信（默认端口 `8917`），支持真机（`192.168.x.x:8917`）与模拟器宿主机（`10.0.2.2:8917`）。
5. **离线优先（Local-First）**：草稿与推送任务队列持久化于本地 SQLite（Room），无桌面端连接时仍可撰写 Markdown、校验多平台字数规范并通过 Android 系统分享导出。

---

## 3. Windows 可执行版本兼容性与打包方案

### 方案 A：Electron + Playwright 桌面端 + Android 原生 APK（推荐，100% 兼容原仓库）

#### 3.1 Windows 局域网兼容要点
- **监听所有网卡**：桌面端 HTTP Server 必须绑定 `0.0.0.0:8917`（而非 `127.0.0.1`），否则手机端无法通过局域网访问。
- **Windows 防火墙放行**：在安装包 NSIS 脚本或首次启动时提示放行 TCP `8917` 端口：
  ```powershell
  New-NetFirewallRule -DisplayName "FreeFlow LAN Bridge" -Direction Inbound -Protocol TCP -LocalPort 8917 -Action Allow -Profile Private
  ```
- **跨平台路径与换行规范**：统一使用 `path.join()` 与 UTF-8 + LF（`\n`）处理 Markdown，避免 Windows `\` 与 CRLF 导致微信/知乎解析异常。
- **内置 Chromium 路径隔离**：将 Playwright 浏览器缓存打包或下载至 `%LOCALAPPDATA%\FreeFlow\ms-playwright`，防止无管理员权限时写入 `Program Files` 失败。

#### 3.2 `electron-builder.yml`（Windows `.exe` 安装包 + 免安装 `.zip`）
```yaml
appId: com.freeflow.desktop
productName: FreeFlow
directories:
  output: dist-electron
win:
  target:
    - target: nsis
      arch: [x64, arm64]
    - target: zip
      arch: [x64]
  icon: build/icon.ico
nsis:
  oneClick: false
  allowToChangeInstallationDirectory: true
  createDesktopShortcut: true
  perMachine: false
extraResources:
  - from: node_modules/playwright-core
    to: playwright-core
```

---

### 方案 B：Kotlin Multiplatform (Compose Multiplatform) 统一代码库双端打包

如果你希望 **Android APK 和 Windows `.exe` 共用同一套 Kotlin/Compose 业务与 UI 代码**：
1. 将 Markdown 编辑器、平台规范矩阵、LAN 客户端抽离到 `shared/src/commonMain`。
2. `androidApp` 打包为 `.apk` / `.aab`。
3. `desktopApp` 使用 Compose Desktop (`jvm("desktop")`)，通过 `jpackage` 打包为自带精简 JRE 的 Windows 原生可执行程序：
   ```bash
   gradle :desktopApp:packageExe        # 生成 Windows .exe 安装包
   gradle :desktopApp:packageMsi        # 生成企业部署 .msi
   gradle :desktopApp:createDistributable # 免安装便携文件夹
   ```
4. Windows 端仅在后台以子进程启动轻量 Node.js + Playwright Sidecar（或通过 WebView2/CDP 复用系统自带 Edge）执行自动化发布。

---

## 4. GitHub Actions 双端自动构建流水线示例

```yaml
name: Build FreeFlow Cross-Platform
on:
  push:
    tags: ['v*']

jobs:
  build-android:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { distribution: 'temurin', java-version: '17' }
      - run: ./gradlew :app:assembleRelease :app:bundleRelease
      - uses: actions/upload-artifact@v4
        with:
          name: freeflow-android
          path: app/build/outputs/**/*.apk

  build-windows:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '20' }
      - run: npm ci && npx playwright install chromium && npm run build
      - run: npx electron-builder --win nsis zip --x64
      - uses: actions/upload-artifact@v4
        with:
          name: freeflow-windows
          path: dist-electron/*.exe
```
