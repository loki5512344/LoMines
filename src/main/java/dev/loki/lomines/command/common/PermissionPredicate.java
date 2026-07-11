package dev.loki.lomines.command.common;

import org.bukkit.command.CommandSender;

public final class PermissionPredicate {

    public boolean hasPermission(CommandSender sender, String cmd) {
        return switch (cmd) {
            case "create", "delete", "reload", "list", "maskscan", "edit", "info" ->
                    sender.hasPermission("lomines.admin");
            case "reset" -> sender.hasPermission("lomines.admin.reset");
            case "setteleport" -> sender.hasPermission("lomines.admin.setteleport");
            case "setspawn", "clearspawn" -> sender.hasPermission("lomines.admin.setspawn");
            case "wand", "group" -> sender.hasPermission("lomines.admin.wand");
            case "stats", "top" -> sender.hasPermission("lomines.stats");
            case "tp" -> sender.hasPermission("lomines.teleport");
            case "copy" -> sender.hasPermission("lomines.admin.copy");
            case "regions", "addregion", "removeregion" -> sender.hasPermission("lomines.admin.regions");
            case "help" -> sender.hasPermission("lomines.use");
            default -> true;
        };
    }
}
