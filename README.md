# MyPlayer

一款 Android 本地视频播放器，基于 [NextPlayer](https://github.com/anilbeesetti/nextplayer) 深度定制，方便手机端使用。

## 截图

| 媒体库（列表） | 媒体库（网格） | 多选操作 | 外观设置 |
| --- | --- | --- | --- |
| ![媒体库](docs/screenshots/library.png) | ![网格](docs/screenshots/grid.png) | ![多选](docs/screenshots/selection.png) | ![外观](docs/screenshots/appearance.png) |

## 功能亮点

### 媒体库
- **扫描文件夹白名单**：只显示选中目录里的视频，目录可增删
- **列表 / 网格双布局**，顶栏长按打开排序快捷设置
- **1–10 分评分 + 文字点评**，缩略图橙色徽标，支持按分数排序
- **隐私空间**：指纹/PIN 解锁，视频从媒体库消失且文件名被隐藏，支持一键移回
- **删除可恢复**：误删记录可还原显示

### 个性化
- 5 套手工调校 Material 3 配色（明暗双版）
- 自定义应用背景图，卡片半透明不遮画面
- 橙色应用图标与 TV banner

### 播放
- 硬件解码、手势控制、倍速、字幕音轨、画中画、后台播放
- 隐私空间内播放不留任何痕迹

### 隐私优先
- 无网络权限，不联网、无遥测
- 无播放历史
- 首次启动零数据

## 与上游的主要差异

移除了上游的自动全盘扫描、网络播放、播放历史、播放列表、搜索等功能；新增扫描白名单、评分、隐私空间、主题与背景定制。

## 构建
## 构建

### 1. 环境要求

| 依赖 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 17 | 需 `JAVA_HOME` 指向 JDK 17（不要用系统默认的 8） |
| Android SDK | Platform 37 | 需 `ANDROID_HOME` 指向 SDK 目录 |
| 网络 | 可访问 Maven Central / Google Maven | 首次构建需下载依赖 |

### 2. 安装 SDK（已有可跳过）

用 [commandlinetools](https://developer.android.com/studio#command-line-tools-only) 最小化安装：

```bash
mkdir -p ~/android-sdk/cmdline-tools
unzip commandlinetools-*.zip -d ~/android-sdk/cmdline-tools
mv ~/android-sdk/cmdline-tools/cmdline-tools ~/android-sdk/cmdline-tools/latest

# 接受许可并安装所需组件
yes | ~/android-sdk/cmdline-tools/latest/bin/sdkmanager --licenses
~/android-sdk/cmdline-tools/latest/bin/sdkmanager "platforms;android-37" "build-tools;37.0.0" "platform-tools"
```

### 3. 配置环境变量

```bash
export JAVA_HOME=/path/to/jdk-17
export ANDROID_HOME=~/android-sdk
```

### 4. 构建

```bash
git clone https://github.com/wsgwx/MyPlayer.git
cd MyPlayer
./gradlew :app:assembleDebug
```

产物在 `app/build/outputs/apk/debug/`，按 ABI 分包：

- `app-arm64-v8a-debug.apk` — 现代手机（绝大多数）
- `app-universal-debug.apk` — 全架构通用包

安装：`adb install -r app-arm64-v8a-debug.apk`，或把 APK 传到手机直接安装。

发布版：`./gradlew :app:assembleRelease`（需自行配置签名）。

### 版本信息

- minSdk 24（Android 6.0） / targetSdk 37
- Gradle 9.9 / AGP 9.5.1 / Kotlin 2.4.21
- 包名 `com.wsgwx.myplayer`

## 许可

遵循上游 [GPL-3.0](LICENSE) 许可证，基于 [anilbeesetti/nextplayer](https://github.com/anilbeesetti/nextplayer)。
