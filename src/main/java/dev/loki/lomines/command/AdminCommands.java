package dev.loki.lomines.command;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.Mine;
import dev.loki.lomines.core.Mines;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.data.config.spawn.PlayerSpawnConfig;
import dev.loki.lomines.data.config.teleport.TeleportConfig;
import dev.loki.lomines.gui.MineEditGui;
import dev.loki.lomines.util.ErrorHandler;
import dev.loki.lomines.util.MessageFormatter;
import dev.lolib.commands.annotation.Arg;
import dev.lolib.commands.annotation.Command;
import dev.lolib.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Collection;

/**
 * Admin commands for mine management.
 * Provides commands for creating, deleting, resetting, reloading, listing, and editing mines.
 */
@Command(value = "lm", permission = "lomines.use")
public class AdminCommands {

    private final LoMinesPlugin plugin;
    private final Mines mines;
    private final ErrorHandler errorHandler;

    public AdminCommands(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.mines = plugin.getMines();
        this.errorHandler = new ErrorHandler(plugin.getLogger(), "[LoMines] ");
    }

    /**
     * Creates a new mine with the given name.
     * Usage: /lm create <name>
     */
    @Subcommand(value = "create", permission = "lomines.admin.create")
    public void create(CommandSender sender, @Arg("name") String name) {
        try {
            mines.create(name);
            sender.sendMessage(MessageFormatter.success("Successfully created mine: " + name));
            sender.sendMessage(Component.text(
                    "Edit selection & contents in plugins/LoMines/mines/" + name + ".yml — "
                            + "for shape-based ore (not full cuboid), paint mask.marker blocks, then: /lm maskscan " + name));
            if (sender instanceof Player player) {
                sender.sendMessage(Component.text(
                        "Or use: /lm edit " + name + " to open GUI editor", NamedTextColor.YELLOW));
            }
        } catch (IllegalArgumentException e) {
            errorHandler.handleNotFound(sender, "Mine already exists", name);
        } catch (IOException e) {
            errorHandler.handleError(sender, "Failed to create mine: " + e.getMessage(),
                    "Failed to create mine " + name, e);
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
            sender.sendMessage(MessageFormatter.success("Successfully deleted mine: " + name));
        } catch (IllegalArgumentException e) {
            errorHandler.handleNotFound(sender, "Mine", name);
        } catch (IOException e) {
            errorHandler.handleError(sender, "Failed to delete mine: " + e.getMessage(),
                    "Failed to delete mine " + name, e);
        }
    }

    /**
     * Opens the GUI editor for a mine.
     * Usage: /lm edit <mine>
     */
    @Subcommand(value = "edit", permission = "lomines.admin.edit")
    public void edit(CommandSender sender, @Arg("mine") String mineName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return;
        }

        Mine mine = mines.find(mineName).orElse(null);
        if (mine == null) {
            errorHandler.handleNotFound(sender, "Mine", mineName);
            return;
        }

        MineEditGui.open(plugin, player, mineName);
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
            sender.sendMessage(MessageFormatter.success("Resetting mine: " + mineName));
        } catch (IllegalArgumentException e) {
            errorHandler.handleNotFound(sender, "Mine", mineName);
        }
    }

    /**
     * Reloads all mine configurations from disk.
     * Usage: /lm reload
     */
    @Subcommand(value = "reload", permission = "lomines.admin.reload")
    public void reload(CommandSender sender) {
        try {
            mines.getAll().forEach(Mine::stop);
            mines.loadAll();
            int count = mines.getAll().size();
            sender.sendMessage(MessageFormatter.success("Successfully reloaded " + count + " mine(s)"));
        } catch (IOException e) {
            errorHandler.handleError(sender, "Failed to reload mines: " + e.getMessage(),
                    "Failed to reload mines", e);
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
     * Sets the teleport location for players when mine resets.
     * Usage: /lm setteleport <mine>
     */
    @Subcommand(value = "setteleport", permission = "lomines.admin.setteleport")
    public void setTeleport(CommandSender sender, @Arg("mine") String mineName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return;
        }

        try {
            Mine mine = mines.find(mineName).orElse(null);
            if (mine == null) {
                errorHandler.handleNotFound(sender, "Mine", mineName);
                return;
            }

            // Get current player location and update config
            var config = mine.getConfig();
            var newTeleport = TeleportConfig.at(player.getLocation());

            var updatedConfig = MineConfig.builder(mineName)
                    .region(config.region())
                    .blocks(config.blocks())
                    .reset(config.reset())
                    .rewards(config.rewards())
                    .teleport(newTeleport)
                    .ui(config.ui())
                    .worldGuard(config.worldGuard())
                    .playerSpawn(config.playerSpawn())
                    .build();

            plugin.getMines().getFileManager().saveConfig(updatedConfig);
            sender.sendMessage(MessageFormatter.success("Teleport location for mine '" + mineName + "' set to your current position"));
            sender.sendMessage(Component.text("Location: " + formatLocation(player.getLocation()), NamedTextColor.GRAY));
        } catch (Exception e) {
            errorHandler.handleError(sender, "Failed to set teleport: " + e.getMessage(),
                    "Failed to set teleport for " + mineName, e);
        }
    }

    /**
     * Sets the safe spawn location for stuck players after mine reset.
     * Usage: /lm setspawn <mine>
     */
    @Subcommand(value = "setspawn", permission = "lomines.admin.setspawn")
    public void setSpawn(CommandSender sender, @Arg("mine") String mineName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players!", NamedTextColor.RED));
            return;
        }

        try {
            Mine mine = mines.find(mineName).orElse(null);
            if (mine == null) {
                errorHandler.handleNotFound(sender, "Mine", mineName);
                return;
            }

            // Get current player location and update config
            var config = mine.getConfig();
            var newSpawn = PlayerSpawnConfig.at(player.getLocation());

            var updatedConfig = MineConfig.builder(mineName)
                    .region(config.region())
                    .blocks(config.blocks())
                    .reset(config.reset())
                    .rewards(config.rewards())
                    .teleport(config.teleport())
                    .ui(config.ui())
                    .worldGuard(config.worldGuard())
                    .playerSpawn(newSpawn)
                    .build();

            plugin.getMines().getFileManager().saveConfig(updatedConfig);
            sender.sendMessage(MessageFormatter.success("Safe spawn location for mine '" + mineName + "' set to your current position"));
            sender.sendMessage(Component.text("Location: " + formatLocation(player.getLocation()), NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Players stuck in blocks will be teleported here after reset", NamedTextColor.YELLOW));
        } catch (Exception e) {
            errorHandler.handleError(sender, "Failed to set spawn: " + e.getMessage(),
                    "Failed to set spawn for " + mineName, e);
        }
    }

    /**
     * Clears the spawn location for stuck players.
     * Usage: /lm clearspawn <mine>
     */
    @Subcommand(value = "clearspawn", permission = "lomines.admin.setspawn")
    public void clearSpawn(CommandSender sender, @Arg("mine") String mineName) {
        try {
            Mine mine = mines.find(mineName).orElse(null);
            if (mine == null) {
                errorHandler.handleNotFound(sender, "Mine", mineName);
                return;
            }

            var config = mine.getConfig();
            var updatedConfig = MineConfig.builder(mineName)
                    .region(config.region())
                    .blocks(config.blocks())
                    .reset(config.reset())
                    .rewards(config.rewards())
                    .teleport(config.teleport())
                    .ui(config.ui())
                    .worldGuard(config.worldGuard())
                    .playerSpawn(PlayerSpawnConfig.disabled())
                    .build();

            plugin.getMines().getFileManager().saveConfig(updatedConfig);
            sender.sendMessage(MessageFormatter.success("Spawn location for mine '" + mineName + "' cleared"));
            sender.sendMessage(Component.text("Players will use teleport location instead", NamedTextColor.GRAY));
        } catch (Exception e) {
            errorHandler.handleError(sender, "Failed to clear spawn: " + e.getMessage(),
                    "Failed to clear spawn for " + mineName, e);
        }
    }

    /**
     * Formats location for display.
     */
    private String formatLocation(Location loc) {
        return String.format("%.1f, %.1f, %.1f in %s",
                loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
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