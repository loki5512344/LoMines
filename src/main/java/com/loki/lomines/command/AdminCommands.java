package com.loki.lomines.command;

import com.loki.lomines.LoMinesPlugin;
import com.loki.lomines.core.Mine;
import com.loki.lomines.core.Mines;
import dev.lolib.commands.annotation.Arg;
import dev.lolib.commands.annotation.Command;
import dev.lolib.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.io.IOException;
import java.util.Collection;

/**
 * Admin commands for mine management.
 * Provides commands for creating, deleting, resetting, reloading, and listing mines.
 */
@Command(value = "lm", permission = "lomines.use")
public class AdminCommands {

    private final LoMinesPlugin plugin;
    private final Mines mines;

    public AdminCommands(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.mines = plugin.getMines();
    }

    /**
     * Creates a new mine with the given name.
     * Usage: /lm create <name>
     */
    @Subcommand(value = "create", permission = "lomines.admin.create")
    public void create(CommandSender sender, @Arg("name") String name) {
        try {
            mines.create(name);
            sender.sendMessage(Component.text("Successfully created mine: " + name));
            sender.sendMessage(Component.text(
                "Edit selection & contents in plugins/LoMines/mines/" + name + ".yml — "
                    + "for shape-based ore (not full cuboid), paint mask.marker blocks, then: /lm maskscan " + name));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Mine already exists: " + name));
        } catch (IOException e) {
            sender.sendMessage(Component.text("Failed to create mine: " + e.getMessage()));
            plugin.loLogger().error("Failed to create mine " + name, e);
        }
    }

    /**
     * Deletes an existing mine.
     * Usage: /lm delete <name>
     */
    @Subcommand(value = "delete", permission = "lomines.admin.delete")
    public void delete(CommandSender sender, @Arg("name") String name) {
        try {
            mines.delete(name);
            sender.sendMessage(Component.text("Successfully deleted mine: " + name));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Mine not found: " + name));
        } catch (IOException e) {
            sender.sendMessage(Component.text("Failed to delete mine: " + e.getMessage()));
            plugin.loLogger().error("Failed to delete mine " + name, e);
        }
    }

    /**
     * Resets a mine by refilling all its regions.
     * Usage: /lm reset <mine> [silent]
     */
    @Subcommand(value = "reset", permission = "lomines.admin.reset")
    public void reset(
        CommandSender sender,
        @Arg("mine") String mineName,
        @Arg(value = "silent", optional = true, defaultValue = "false") boolean silent
    ) {
        try {
            Mine mine = mines.get(mineName);
            mine.reset(silent);
            sender.sendMessage(Component.text("Resetting mine: " + mineName));
        } catch (IllegalArgumentException e) {
            sender.sendMessage(Component.text("Mine not found: " + mineName));
        }
    }

    /**
     * Reloads all mine configurations from disk.
     * Usage: /lm reload
     */
    @Subcommand(value = "reload", permission = "lomines.admin.reload")
    public void reload(CommandSender sender) {
        try {
            // Stop all existing mines
            mines.getAll().forEach(Mine::stop);

            // Reload all mines
            mines.loadAll();

            int count = mines.getAll().size();
            sender.sendMessage(Component.text("Successfully reloaded " + count + " mine(s)"));
        } catch (IOException e) {
            sender.sendMessage(Component.text("Failed to reload mines: " + e.getMessage()));
            plugin.loLogger().error("Failed to reload mines", e);
        }
    }

    /**
     * Lists all active mines.
     * Usage: /lm list
     */
    @Subcommand(value = "list", permission = "lomines.admin.list")
    public void list(CommandSender sender) {
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

    /**
     * Formats mine status information for display.
     */
    private String formatMineStatus(Mine mine) {
        int blocks = mine.getBlocks();
        int total = mine.getTotalVolume();
        double percent = mine.getPercentFilled();

        return String.format("(%d/%d blocks, %.1f%%)", blocks, total, percent);
    }
}
