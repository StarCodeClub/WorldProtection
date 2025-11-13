package org.xiaomu.WorldProtection;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.GlowItemFrame;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Protector implements Listener {
    public static final Map<String, String> worldConfigs = new HashMap<>();
    public static final Map<String, ProtectConfig> ProtectConfigs = new HashMap<>();
    private static final ExpiringMap<String, Boolean> actionBarCooldown = ExpiringMap.builder()
            .expiration(1, TimeUnit.SECONDS)
            .expirationPolicy(ExpirationPolicy.CREATED)
            .build();
    
    public static void sendActionBar(Player player, Component message) {
        String playerKey = player.getUniqueId().toString();
        
        if (actionBarCooldown.containsKey(playerKey)) {
            return;
        }
        
        player.sendActionBar(message);
        actionBarCooldown.put(playerKey, true);
    }

    public static void reloadConfigs() {
        worldConfigs.clear();
        ProtectConfigs.clear();
        actionBarCooldown.clear();
        ConfigurationSection protectConfig = WorldProtection.getInstance().getConfig().getConfigurationSection("protect-config");
        ConfigurationSection worldConfig = WorldProtection.getInstance().getConfig().getConfigurationSection("world-config");

        // 收集所有配置段 allSections -> configName: ConfigurationSection
        Map<String, ConfigurationSection> allSections = new HashMap<>();
        for (String key : protectConfig.getKeys(false)) {
            allSections.put(key, protectConfig.getConfigurationSection(key));
        }

        if (!allSections.containsKey("default")) {
            throw new NoSuchFieldError("default section not found in protect-config");
        }

        // 创建所有配置，自动处理继承
        for (String configName : allSections.keySet()) {
            ConfigurationSection section = allSections.get(configName);
            ProtectConfig config = new ProtectConfig(configName, section, allSections);
            ProtectConfigs.put(configName, config);
        }
        
        if (worldConfig != null) {
            for (String worldName : worldConfig.getKeys(false)) {
                String configName = worldConfig.getString(worldName);
                if ("disable".equals(configName)) {
                    worldConfigs.put(worldName, null);
                } else if (ProtectConfigs.containsKey(configName)) {
                    worldConfigs.put(worldName, configName);
                }
            }
        }
    }

    private static ProtectConfig getWorldConfig(String worldName) {
        if (worldConfigs.containsKey(worldName)) {
            return ProtectConfigs.get(worldConfigs.get(worldName));
        }
        return ProtectConfigs.get("default");
    }

    public static boolean hasBypassPermission(Player player, String worldName, String protectionType) {
        return player.hasPermission("wp.bypass." + worldName + "." + protectionType);
    }

    public static String getPluginPrefix() {
        return WorldProtection.getInstance().getConfig().getString("plugin-prefix", "<shadow:#000000FF><gradient:#ff7300:#ff3c00><b>[WP]</b></gradient></shadow>");
    }

    private boolean shouldCancel(Player player, String protectionType) {
        String worldName = player.getWorld().getName();
        ProtectConfig config = getWorldConfig(worldName);
        
        if (config == null) {
            return false;
        }
        
        if (!config.isProtectionEnabled(protectionType)) {
            return false;
        }
        
        return !hasBypassPermission(player, worldName, protectionType);
    }

    private Component getProtectionMessage(String worldName, String protectionType) {
        ProtectConfig config = getWorldConfig(worldName);
        if (config != null) {
            return config.getProtectionMessage(protectionType);
        }
        return net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
            .deserialize(getPluginPrefix() + " <red>你没有权限执行此操作.</red>");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (shouldCancel(player, "anti-world-edit")) {
            event.setCancelled(true);
            sendActionBar(player, getProtectionMessage(player.getWorld().getName(), "anti-world-edit"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (shouldCancel(player, "anti-world-edit")) {
            event.setCancelled(true);
            sendActionBar(player, getProtectionMessage(player.getWorld().getName(), "anti-world-edit"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (shouldCancel(player, "anti-world-edit")) {
            event.setCancelled(true);
            sendActionBar(player, getProtectionMessage(player.getWorld().getName(), "anti-world-edit"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (shouldCancel(player, "anti-world-edit")) {
            event.setCancelled(true);
            sendActionBar(player, getProtectionMessage(player.getWorld().getName(), "anti-world-edit"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        String worldName = event.getLocation().getWorld().getName();
        ProtectConfig config = getWorldConfig(worldName);
        
        if (config != null && config.isProtectionEnabled("anti-explosion")) {
            event.blockList().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        
        if (shouldCancel(player, "anti-player-enter")) {
            sendActionBar(player, getProtectionMessage(worldName, "anti-player-enter"));
            // TODO
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        String worldName = event.getTo().getWorld().getName();
        if (event.getFrom().getWorld().getName().equals(worldName)) {
            return;
        }

        if (shouldCancel(player, "anti-player-enter")) {
            event.setCancelled(true);
            sendActionBar(player, getProtectionMessage(worldName, "anti-player-enter"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (shouldCancel(player, "anti-block-interaction")) {
            event.setCancelled(true);
            sendActionBar(player, getProtectionMessage(player.getWorld().getName(), "anti-block-interaction"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (player != null && shouldCancel(player, "anti-world-edit")) {
            event.setCancelled(true);
            sendActionBar(player, getProtectionMessage(player.getWorld().getName(), "anti-world-edit"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            Player player = (Player) event.getRemover();
            if (shouldCancel(player, "anti-world-edit")) {
                event.setCancelled(true);
                sendActionBar(player, getProtectionMessage(player.getWorld().getName(), "anti-world-edit"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(org.bukkit.event.player.PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        
        // 检查物品展示框交互 (属于 world-edit)
        if (event.getRightClicked() instanceof ItemFrame || event.getRightClicked() instanceof GlowItemFrame) {
            if (shouldCancel(player, "anti-world-edit")) {
                event.setCancelled(true);
                sendActionBar(player, getProtectionMessage(player.getWorld().getName(), "anti-world-edit"));
                return;
            }
        }
        
        // 检查画和其他悬挂实体 (属于 world-edit)
        if (event.getRightClicked() instanceof Hanging) {
            if (shouldCancel(player, "anti-world-edit")) {
                event.setCancelled(true);
                sendActionBar(player, getProtectionMessage(player.getWorld().getName(), "anti-world-edit"));
                return;
            }
        }
        
        // 检查一般实体交互保护
        if (shouldCancel(player, "anti-entity-interaction")) {
            event.setCancelled(true);
            sendActionBar(player, getProtectionMessage(player.getWorld().getName(), "anti-entity-interaction"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            if (shouldCancel(damager, "anti-pvp")) {
                event.setCancelled(true);
                sendActionBar(damager, getProtectionMessage(damager.getWorld().getName(), "anti-pvp"));
            }
        } else if (event.getDamager() instanceof Player) {
            Player damager = (Player) event.getDamager();
            if (shouldCancel(damager, "anti-attack-entity")) {
                event.setCancelled(true);
                sendActionBar(damager, getProtectionMessage(damager.getWorld().getName(), "anti-attack-entity"));
            }
        }
    }
}
