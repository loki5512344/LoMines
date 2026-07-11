package dev.loki.lomines.command;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.command.admin.info.InfoCommand;
import dev.loki.lomines.command.admin.info.RegionCommands;
import dev.loki.lomines.command.admin.manage.AdminCommands;
import dev.loki.lomines.command.admin.manage.CopyCommand;
import dev.loki.lomines.command.admin.misc.MaskCommands;
import dev.loki.lomines.command.admin.misc.TeleportCommands;
import dev.loki.lomines.command.admin.stats.HologramCommands;
import dev.loki.lomines.command.admin.stats.StatsCommands;
import dev.loki.lomines.command.player.PlayerCommands;
import dev.loki.lomines.command.player.TeleportCommand;
import dev.loki.lomines.command.common.LoMinesTabCompleter;
import dev.loki.lomines.util.ErrorHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LmCommand implements CommandExecutor, TabCompleter {

    private final LoMinesPlugin plugin;
    private final AdminCommands adminCommands;
    private final InfoCommand infoCommand;
    private final RegionCommands regionCommands;
    private final CopyCommand copyCommand;
    private final MaskCommands maskCommands;
    private final TeleportCommands teleportCommands;
    private final HologramCommands hologramCommands;
    private final StatsCommands statsCommands;
    private final PlayerCommands playerCommands;
    private final TeleportCommand tpCommand;
    private final LoMinesTabCompleter tabCompleter;

    public LmCommand(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.adminCommands = new AdminCommands(plugin);
        this.infoCommand = new InfoCommand(plugin);
        this.regionCommands = new RegionCommands(plugin);
        this.copyCommand = new CopyCommand(plugin);
        this.maskCommands = new MaskCommands(plugin);
        this.teleportCommands = new TeleportCommands(plugin, plugin.getMines(), new ErrorHandler(plugin.getLogger(), "[LoMines] "));
        this.hologramCommands = new HologramCommands(plugin);
        this.statsCommands = new StatsCommands(plugin);
        this.playerCommands = new PlayerCommands(plugin);
        this.tpCommand = new TeleportCommand(plugin);
        this.tabCompleter = new LoMinesTabCompleter(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§6/lm help §f- Show help");
            return true;
        }

        String sub = args[0].toLowerCase();
        String[] rest = new String[args.length - 1];
        System.arraycopy(args, 1, rest, 0, rest.length);

        switch (sub) {
            case "create", "delete", "edit", "reset", "reload", "list" -> adminCommands.handle(sender, sub, rest);
            case "info" -> infoCommand.handle(sender, rest);
            case "regions", "addregion", "removeregion" -> regionCommands.handle(sender, sub, rest);
            case "copy" -> copyCommand.handle(sender, rest);
            case "maskscan" -> maskCommands.handle(sender, rest);
            case "setteleport", "setspawn", "clearspawn" -> teleportCommands.handle(sender, sub, rest);
            case "hologram" -> hologramCommands.handle(sender, rest);
            case "stats", "top" -> statsCommands.handle(sender, sub, rest);
            case "tp" -> tpCommand.handle(sender, rest);
            case "wand", "group", "help" -> playerCommands.handle(sender, sub, rest);
            default -> sender.sendMessage("§cUnknown command: " + sub);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return tabCompleter.onTabComplete(sender, command, alias, args);
    }
}
