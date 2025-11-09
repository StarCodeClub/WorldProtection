package org.xiaomu.WorldProtection;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public class Commander implements CommandExecutor {
    
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<color:#d60032><b>[WP]</b></color> <gray>WorldProtection 插件帮助</gray>"));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>/wp status</gray> <white>- 查看保护设置总览</white>"));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>/wp status <world></gray> <white>- 查看某个世界的保护设置</white>"));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>/wp reload</gray> <white>- 重载配置文件</white>"));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>/wp version</gray> <white>- 显示插件版本</white>"));
    }
    
    private void sendStatus(CommandSender sender) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<color:#d60032><b>[WP]</b></color> <gray>=== 保护设置总览 ===</gray>"));
        
        ConfigurationSection worldConfig = WorldProtection.getInstance().getConfig().getConfigurationSection("world-config");
        if (worldConfig != null) {
            for (String worldName : worldConfig.getKeys(false)) {
                String configName = worldConfig.getString(worldName);
                if ("disable".equals(configName)) {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>" + worldName + ":</gray> <red>已禁用保护</red>"));
                } else {
                    sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>" + worldName + ":</gray> <green>使用配置 '" + configName + "'</green>"));
                }
            }
        }
        
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>其他世界:</gray> <white>使用默认配置</white>"));
    }
    
    private void sendWorldStatus(CommandSender sender, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<color:#d60032><b>[WP]</b></color> <red>此服务器上没有 '" + worldName + "' 世界，任何配置都未生效.</red>"));
            return;
        }
        
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<color:#d60032><b>[WP]</b></color> <gray>=== 世界 '" + worldName + "' 保护设置 ===</gray>"));
        
        String configName = "default";
        
        if (Protector.worldConfigs.containsKey(worldName)) {
            configName = Protector.worldConfigs.get(worldName);

            if ("disable".equals(configName)) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>保护状态:</gray> <red>已禁用</red>"));
                return;
            }
        }
        
        ProtectConfig worldConfig = Protector.ProtectConfigs.get(configName);
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>使用配置:</gray> <green>" + worldConfig.configName + "</green>"));
        if (worldConfig.parentName != null) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>该配置继承于:</gray> <green>" + worldConfig.parentName + "</green>"));
        }

        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>反世界编辑:</gray> " + (worldConfig.antiWorldEdit ? "<green>启用</green>" : "<red>禁用</red>")));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>反PVP:</gray> " + (worldConfig.antiPvp ? "<green>启用</green>" : "<red>禁用</red>")));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>反玩家进入:</gray> " + (worldConfig.antiPlayerEnter ? "<green>启用</green>" : "<red>禁用</red>")));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>反爆炸:</gray> " + (worldConfig.antiExplosion ? "<green>启用</green>" : "<red>禁用</red>")));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>反方块交互:</gray> " + (worldConfig.antiBlockInteraction ? "<green>启用</green>" : "<red>禁用</red>")));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>反攻击实体:</gray> " + (worldConfig.antiAttackEntity ? "<green>启用</green>" : "<red>禁用</red>")));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>反实体交互:</gray> " + (worldConfig.antiEntityInteraction ? "<green>启用</green>" : "<red>禁用</red>")));
    }
    
    private void reloadConfig(CommandSender sender) {
        try {
            WorldProtection.getInstance().reloadConfig();
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<color:#d60032><b>[WP]</b></color> <green>已读取并重载配置文件.</green>"));
        } catch (Exception e) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<color:#d60032><b>[WP]</b></color> <red>配置文件重载失败: " + e.getMessage() + "</red>"));
        }
    }
    
    private void sendVersion(CommandSender sender) {
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<color:#d60032><b>[WP]</b></color> <gray>关于 WorldProtection</gray>"));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>作者: xiaomu18</gray>"));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>插件版本: " + WorldProtection.getInstance().getPluginMeta().getVersion() + "</gray>"));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>网站: https://github.com/StarCodeClub/WorldProtection</gray>"));
        sender.sendMessage(MiniMessage.miniMessage().deserialize("<gray>适用于: Minecraft 1.13.X-1.21.X</gray>"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("wp.admin")) {
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<color:#d60032><b>[WP]</b></color> <red>你没有权限使用此命令.</red>"));
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "help":
                sendHelp(sender);
                break;
                
            case "status":
                if (args.length == 1) {
                    sendStatus(sender);
                } else {
                    sendWorldStatus(sender, args[1]);
                }
                break;
                
            case "reload":
                reloadConfig(sender);
                break;
                
            case "version":
                sendVersion(sender);
                break;
                
            default:
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<color:#d60032><b>[WP]</b></color> <red>未知命令. 使用 /wp help 查看帮助.</red>"));
                break;
        }
        
        return true;
    }
}
