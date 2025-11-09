package org.xiaomu.WorldProtection;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WPTabCompleter implements TabCompleter {
    
    private static final String[] COMMANDS = {"help", "status", "reload", "version"};
    private static final String[] HELP_COMMANDS = {"help", "status", "reload", "version"};
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("wp.admin")) {
            return Collections.emptyList();
        }
        
        if (args.length == 1) {
            String partialCommand = args[0].toLowerCase();
            for (String cmd : COMMANDS) {
                if (StringUtil.startsWithIgnoreCase(cmd, partialCommand)) {
                    completions.add(cmd);
                }
            }
            return completions;
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("status")) {
            String partialWorld = args[1].toLowerCase();
            for (org.bukkit.World world : Bukkit.getWorlds()) {
                String worldName = world.getName();
                if (StringUtil.startsWithIgnoreCase(worldName, partialWorld)) {
                    completions.add(worldName);
                }
            }
            return completions;
        }
        
        return Collections.emptyList();
    }
}