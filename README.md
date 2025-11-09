# WorldProtection

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/StarCodeClub/WorldProtection)
[![Version](https://img.shields.io/badge/version-1.3.0--SNAPSHOT-blue.svg)](https://github.com/StarCodeClub/WorldProtection/releases)
[![Minecraft](https://img.shields.io/badge/minecraft-1.13--1.21-green.svg)](https://www.spigotmc.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

纯净 & 现代化 & 高性能的 Minecraft 服务器世界保护插件，支持层次化配置和权限系统。

## ✨ 特性

- **🌍 多世界保护**：层次化配置，支持多世界同时使用不同的保护配置
- **🔧 灵活配置**：支持配置继承，用最简单的配置轻松设置复杂保护规则
- **🛡️ 全面保护**：包括反方块修改、反方块交互、反PVP、反实体交互、反实体攻击、爆炸保护、禁止进入世界功能
- **💬 消息自定义**：支持为任意世界和保护类型设置单独的 MiniMessage 格式的提示消息
- **⚡ High Performance**：优化的 ActionBar 消息冷却，防止高带宽占用
- **🎯 权限系统**：细粒度的权限控制，支持使用特定权限绕过某项保护

## 🚀 快速开始

### 对于普通服主
**从 [Releases](https://github.com/StarCodeClub/WorldProtection/releases) 获取最新的预编译版本**

### 对于开发者

- **Java 8** 或更高版本
- **Maven 3.6+** (仅编译时需要)

1. 克隆仓库：
```bash
git clone https://github.com/StarCodeClub/WorldProtection.git
cd WorldProtection
```

2. 编译插件：
```bash
mvn clean package
```

3. 编译完成后，在 `target/` 目录下找到 `WorldProtection-1.3.0-SNAPSHOT.jar`

## 📖 配置说明

### 配置文件结构

```yaml
protect-config:
  # 配置文件必须包含 default 节点，请勿删除！！！
  default:
    # 禁止玩家修改世界（放置方块，填充水、岩浆，放置画，修改物品展示框和花盆...）
    anti-world-edit: false
    # 禁止玩家 pvp (玩家互相伤害)
    anti-pvp: false
    # 禁止玩家进入世界 (PlayerChangedWorldEvent)
    anti-player-enter: false
    # 使爆炸在该世界不破坏方块 (EntityExplodeEvent)
    anti-explosion: false
    # 禁止玩家与该世界的方块交互 (PlayerInteractEvent)
    anti-block-interaction: false
    # 禁止玩家攻击该世界的实体 (不包含玩家互相伤害)
    anti-attack-entity: false
    # 禁止玩家与实体交互 (onPlayerInteractEntity)
    anti-entity-interaction: false

    # 玩家无权限时 actionbar 显示的消息 (MiniMessage 格式)
    message:
      anti-world-edit: "<color:#d60032><b>[WP]</b></color> <red>你没有权限修改这个世界.</red>"
      anti-pvp: "<color:#d60032><b>[WP]</b></color> <red>已禁止在该世界 PVP.</red>"
      # ... 其他消息

  # 继承配置示例
  common:
    parent: default  # 继承 default 配置
    anti-world-edit: true
    anti-explosion: true

  hub-config:
    parent: common  # 继承 common 配置
    anti-block-interaction: true
    anti-attack-entity: true

# 指定哪些世界使用哪些保护配置
world-config:
  "world": common        # world 世界使用 common 配置
  "hub": hub-config      # hub 世界使用 hub-config 配置
  "world_the_end": disable  # 末地禁用所有保护
```

### 保护类型说明

| 保护类型 | 说明 | 涵盖事件 |
|---------|------|----------|
| `anti-world-edit` | 禁止修改世界 | 方块放置/破坏、桶操作、画/物品展示框交互 |
| `anti-pvp` | 禁止玩家战斗 | 玩家间伤害 |
| `anti-player-enter` | 禁止进入世界 | 世界切换、传送 |
| `anti-explosion` | 禁止爆炸破坏 | 实体爆炸事件 |
| `anti-block-interaction` | 禁止方块交互 | 方块交互事件 |
| `anti-attack-entity` | 禁止攻击实体 | 攻击非玩家实体 |
| `anti-entity-interaction` | 禁止实体交互 | 实体交互事件 |

## 🎮 命令与权限

### 命令列表

| 命令 | 说明 | 权限 |
|------|------|------|
| `/wp help` | 显示帮助信息 | `wp.admin` |
| `/wp status` | 查看保护设置总览 | `wp.admin` |
| `/wp status <world>` | 查看指定世界保护设置 | `wp.admin` |
| `/wp reload` | 重载配置文件 | `wp.admin` |
| `/wp version` | 显示插件版本 | `wp.admin` |

### 权限系统

#### 管理权限
- `wp.admin` - 使用所有 /wp 命令（默认：OP）

#### 绕过权限
- `wp.bypass.*` - 绕过所有世界保护（默认：OP）
- `wp.bypass.<world>.anti-world-edit` - 绕过指定世界的世界编辑保护
- `wp.bypass.<world>.anti-pvp` - 绕过指定世界的 PVP 保护
- `wp.bypass.<world>.anti-player-enter` - 绕过指定世界的进入保护
- `wp.bypass.<world>.anti-block-interaction` - 绕过指定世界的方块交互保护
- `wp.bypass.<world>.anti-attack-entity` - 绕过指定世界的攻击实体保护
- `wp.bypass.<world>.anti-entity-interaction` - 绕过指定世界的实体交互保护

## 🛠️ 开发信息

### 项目结构

```
src/main/java/org/xiaomu/WorldProtection/
├── WorldProtection.java    # 主插件类
├── Commander.java          # 命令处理器
├── Protector.java          # 事件监听器
├── ProtectConfig.java      # 配置类
└── WPTabCompleter.java     # Tab 补全
```

## 📝 更新日志

### v1.3.0-SNAPSHOT
- 支持层次化配置系统
- 添加配置继承功能
- 优化 ActionBar 消息冷却
- 支持 MiniMessage 格式
- 添加 Tab 补全功能
- 支持 Folia


如果您遇到问题或有功能建议，请在 [GitHub Issues](https://github.com/StarCodeClub/WorldProtection/issues) 提交。  
本项目采用 MIT 许可证，详见 [LICENSE](LICENSE) 文件。

---

**作者**: xiaomu18  
**网站**: https://github.com/StarCodeClub/WorldProtection
