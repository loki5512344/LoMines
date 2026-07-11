package dev.loki.lomines.command.admin.misc;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.registry.Mines;
import dev.loki.lomines.data.config.ConfigLoader;
import dev.loki.lomines.util.ErrorHandler;
import dev.loki.lomines.util.MessageFormatter;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class MaskCommands {

    private final LoMinesPlugin plugin;
    private final Mines mines;
    private final ErrorHandler errorHandler;

    public MaskCommands(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.mines = plugin.getMines();
        this.errorHandler = new ErrorHandler(plugin.getLogger(), "[LoMines] ");
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(MessageFormatter.error("Usage: /lm maskscan <mine>"));
            return;
        }
        String name = args[0];
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                int n = mines.scanAndSaveMask(name);
                sender.sendMessage(MessageFormatter.success(
                        "Mask mode: saved " + n + " cell(s) for mine '" + name
                                + "'. Reset will spawn ore only at those positions."));
            } catch (IllegalArgumentException e) {
                errorHandler.handleNotFound(sender, "Mine", name);
            } catch (IOException | ConfigLoader.ConfigLoadException e) {
                errorHandler.handleError(sender, "Mask scan failed: " + e.getMessage(),
                        "maskscan failed for " + name, e);
            }
        });
    }
}
