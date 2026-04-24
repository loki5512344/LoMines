package com.loki.lomines.command;

import com.loki.lomines.LoMinesPlugin;
import com.loki.lomines.core.Mines;
import com.loki.lomines.data.config.ConfigParseException;
import dev.lolib.commands.annotation.Arg;
import dev.lolib.commands.annotation.Command;
import dev.lolib.commands.annotation.Subcommand;
import dev.lolib.scheduler.Scheduler;
import net.kyori.adventure.text.Component;
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

    public MaskCommands(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.mines = plugin.getMines();
    }

    /**
     * Scans selection for marker blocks ({@code mask.marker}, default pink concrete), saves mask positions and switches mine to mask fill mode.
     * Run from the server main thread (scheduled automatically).
     */
    @Subcommand(value = "maskscan", permission = "lomines.admin.maskscan")
    public void maskScan(CommandSender sender, @Arg("mine") String name) {
        Scheduler.get(plugin).run(() -> {
            try {
                int n = mines.scanAndSaveMask(name);
                sender.sendMessage(Component.text(
                    "Mask mode: saved " + n + " cell(s) for mine '" + name
                        + "'. Reset will spawn ore only at those positions."));
            } catch (IllegalArgumentException e) {
                sender.sendMessage(Component.text("Mine not found: " + name));
            } catch (IOException | ConfigParseException e) {
                sender.sendMessage(Component.text("Mask scan failed: " + e.getMessage()));
                plugin.loLogger().error("maskscan failed for " + name, e);
            }
        });
    }
}
