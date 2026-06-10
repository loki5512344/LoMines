package dev.loki.lomines.command.admin;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mines;
import dev.loki.lomines.data.config.ConfigLoader;
import dev.loki.lomines.util.ErrorHandler;
import dev.loki.lomines.util.MessageFormatter;
import dev.lolib.commands.annotation.Arg;
import dev.lolib.commands.annotation.Command;
import dev.lolib.commands.annotation.Subcommand;
import dev.lolib.scheduler.Scheduler;
import org.bukkit.command.CommandSender;

import java.io.IOException;

/**
 * Mask commands for mine shape management.
 * Provides commands for scanning and saving mask positions.
 */
@Command(value = "lm", permission = "lomines.use")
public class MaskCommands {

    private final LoMinesPlugin plugin;
    private final Mines mines;
    private final ErrorHandler errorHandler;

    public MaskCommands(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.mines = plugin.getMines();
        this.errorHandler = new ErrorHandler(plugin.getLogger(), "[LoMines] ");
    }

    @Subcommand(value = "maskscan", permission = "lomines.admin.maskscan")
    public void maskScan(CommandSender sender, @Arg("mine") String name) {
        Scheduler.get(plugin).run(() -> {
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
