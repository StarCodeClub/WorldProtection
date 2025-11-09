package org.xiaomu.WorldProtection;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class ProtectConfig {
    public String configName;
    public String parentName;
    public boolean antiWorldEdit = false;
    public boolean antiPvp = false;
    public boolean antiPlayerEnter = false;
    public boolean antiExplosion = false;
    public boolean antiBlockInteraction = false;
    public boolean antiAttackEntity = false;
    public boolean antiEntityInteraction = false;

    public Map<String, String> messages = new HashMap<>();

    public ProtectConfig(String configName, ConfigurationSection section, Map<String, ConfigurationSection> allSections) {
        this.configName = configName;

        if (section == null) {
            // 默认所有保护为false
            return;
        }

        if (!"default".equals(configName)) {
            // 确定父配置
            ProtectConfig parentConfig = null;
            String parentName = section.getString("parent");


            if (parentName != null) {
                // 指定了parent
                if (allSections.containsKey(parentName)) {
                    parentConfig = new ProtectConfig(parentName, allSections.get(parentName), allSections);
                }
                // 如果parent不存在，则不继承任何配置（保持默认false）
            } else {
                // 没有指定parent且不是default，默认继承default
                parentConfig = new ProtectConfig("default", allSections.get("default"), allSections);
            }

            // 继承父配置
            if (parentConfig != null) {
                this.parentName = parentConfig.configName;
                this.antiWorldEdit = parentConfig.antiWorldEdit;
                this.antiPvp = parentConfig.antiPvp;
                this.antiPlayerEnter = parentConfig.antiPlayerEnter;
                this.antiExplosion = parentConfig.antiExplosion;
                this.antiBlockInteraction = parentConfig.antiBlockInteraction;
                this.antiAttackEntity = parentConfig.antiAttackEntity;
                this.antiEntityInteraction = parentConfig.antiEntityInteraction;
                this.messages.putAll(parentConfig.messages);
            } else {
                // 没有父配置，使用默认值（全部false）
                this.antiWorldEdit = false;
                this.antiPvp = false;
                this.antiPlayerEnter = false;
                this.antiExplosion = false;
                this.antiBlockInteraction = false;
                this.antiAttackEntity = false;
                this.antiEntityInteraction = false;
            }
        }
        
        // 应用当前配置的覆盖
        if (section.contains("anti-world-edit")) {
            this.antiWorldEdit = section.getBoolean("anti-world-edit");
        }
        if (section.contains("anti-pvp")) {
            this.antiPvp = section.getBoolean("anti-pvp");
        }
        if (section.contains("anti-player-enter")) {
            this.antiPlayerEnter = section.getBoolean("anti-player-enter");
        }
        if (section.contains("anti-explosion")) {
            this.antiExplosion = section.getBoolean("anti-explosion");
        }
        if (section.contains("anti-block-interaction")) {
            this.antiBlockInteraction = section.getBoolean("anti-block-interaction");
        }
        if (section.contains("anti-attack-entity")) {
            this.antiAttackEntity = section.getBoolean("anti-attack-entity");
        }
        if (section.contains("anti-entity-interaction")) {
            this.antiEntityInteraction = section.getBoolean("anti-entity-interaction");
        }
        
        // 处理消息
        ConfigurationSection messageSection = section.getConfigurationSection("message");
        if (messageSection != null) {
            for (String key : messageSection.getKeys(false)) {
                this.messages.put(key, messageSection.getString(key));
            }
        }
    }

    public boolean isProtectionEnabled(String protectionType) {
        switch (protectionType) {
            case "anti-world-edit": return antiWorldEdit;
            case "anti-pvp": return antiPvp;
            case "anti-player-enter": return antiPlayerEnter;
            case "anti-explosion": return antiExplosion;
            case "anti-block-interaction": return antiBlockInteraction;
            case "anti-attack-entity": return antiAttackEntity;
            case "anti-entity-interaction": return antiEntityInteraction;
            default: return false;
        }
    }

    public Component getProtectionMessage(String protectionType) {
        String message = messages.getOrDefault(protectionType, "<color:#d60032><b>[WP]</b></color> <red>你没有权限执行此操作.</red>");
        return MiniMessage.miniMessage().deserialize(message);
    }
}