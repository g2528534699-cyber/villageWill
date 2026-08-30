# 村庄意志 (Village Will)

一个以村庄为主题增强的 Minecraft Mod，使用 **Forge 1.20.1 (47.4.23)** 开发。

## 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17（本机路径 `C:\Program Files\Microsoft\jdk-17.0.10.7-hotspot`） | 已写入 `gradle.properties` 的 `org.gradle.java.home` |
| Forge | 1.20.1-47.4.23 | 与游戏端安装版本一致 |
| Gradle | 8.8（由 wrapper 自动下载，无需手动安装） | |

## 常用命令（在工程根目录执行）

```bat
:: 首次构建（下载依赖，较慢，需要联网）
gradlew.bat build

:: 启动游戏测试（开发模式，自动加载 mod）
gradlew.bat runClient

:: 生成 IDE 工程（可选）
gradlew.bat eclipse        :: Eclipse
gradlew.bat idea           :: IntelliJ IDEA
```

构建产物：`build/libs/village_will-0.1.0.jar`，复制到 `.minecraft/mods/` 即可安装。

## 目录结构

```
VillageWill/
├── gradlew.bat              # Windows 构建入口
├── build.gradle             # 构建脚本
├── gradle.properties        # 版本/mod 标识配置（改这里改 mod 信息）
└── src/main/
    ├── java/com/villagewill/    # 主代码（包名 com.villagewill）
    └── resources/
        ├── META-INF/mods.toml   # mod 加载描述
        └── assets/village_will/ # 资源（lang 语言文件等）
```

## Mod 标识

- **modid**: `village_will`
- **显示名**: 村庄意志
- **包名**: `com.villagewill`
- **版本**: 0.1.0

## 注意事项

- `gradle.properties` 中的 `org.gradle.java.home` 是本机 JDK17 路径，换机器或协作者需删除该行并设置系统 `JAVA_HOME`。
- 游戏端已安装 Forge 1.20.1 (47.4.23)，`.minecraft/mods/` 目录已就绪，直接放入构建产物即可。
- 在本开发环境中 Gradle 缓存目录被重定向到工作区的 `.gradle-user-home/`（沙箱限制），手动在 IDE/终端构建时无需此设置，使用默认 `~/.gradle` 即可。
