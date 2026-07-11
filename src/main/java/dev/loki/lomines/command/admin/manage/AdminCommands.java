package dev.loki.lomines.command.admin.manage;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.core.mine.registry.Mines;
import dev.loki.lomines.util.ErrorHandler;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public class AdminCommands {

    private final LoMinesPlugin plugin;
    private final Mines mines;
    private final ErrorHandler errorHandler;
    private final MineActionHandler actionHandler;

    public AdminCommands(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.mines = plugin.getMines();
        this.errorHandler = new ErrorHandler(plugin.getLogger(), "[LoMines] ");
        this.actionHandler = new MineActionHandler(plugin, mines, errorHandler);
    }

    public void handle(CommandSender sender, String subcommand, String[] args) {
        switch (subcommand.toLowerCase()) {
            case "create" -> {
                if (args.length < 1) {
                    sender.sendMessage(Component.text("Usage: /lm create <name>"));
                    return;
                }
                actionHandler.create(sender, args[0]);
            }
            case "delete" -> {
                if (args.length < 1) {
                    sender.sendMessage(Component.text("Usage: /lm delete <name>"));
                    return;
                }
                actionHandler.delete(sender, args[0]);
            }
            case "edit" -> {
                if (args.length < 1) {
                    sender.sendMessage(Component.text("Usage: /lm edit <mine>"));
                    return;
                }
                actionHandler.edit(sender, args[0]);
            }
            case "reset" -> {
                if (args.length < 1) {
                    sender.sendMessage(Component.text("Usage: /lm reset <mine> [silent]"));
                    return;
                }
                boolean silent = args.length > 1 && (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("silent"));
                actionHandler.reset(sender, args[0], silent);
            }
            case "reload" -> actionHandler.reload(sender);
            case "list" -> {
                Collection<Mine> allMines = mines.getAll();
                if (allMines.isEmpty()) {
                    sender.sendMessage(Component.text("No mines found"));
                    return;
                }
                sender.sendMessage(Component.text("=== Mines (" + allMines.size() + ") ==="));
                for (Mine mine : allMines) {
                    String status = formatMineStatus(mine);
                    sender.sendMessage(Component.text("- " + mine.getName() + " " + status));
                }
            }
            default -> {}
        }
    }

    private String formatMineStatus(Mine mine) {
        int blocks = mine.getBlocks();
        int total = mine.getTotalVolume();
        double percent = mine.getPercentFilled();
        return String.format("(%d/%d blocks, %.1f%%)", blocks, total, percent);
    }
}
