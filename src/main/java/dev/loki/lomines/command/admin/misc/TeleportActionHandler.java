package dev.loki.lomines.command.admin.misc;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.core.mine.registry.Mines;
import dev.loki.lomines.data.config.model.MineConfig;
import dev.loki.lomines.data.config.spawn.PlayerSpawnConfig;
import dev.loki.lomines.data.config.teleport.TeleportConfig;
import dev.loki.lomines.util.ErrorHandler;
import dev.loki.lomines.util.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class TeleportActionHandler {

    private final LoMinesPlugin plugin;
    private final Mines mines;
    private final ErrorHandler errorHandler;

    public TeleportActionHandler(LoMinesPlugin plugin, Mines mines, ErrorHandler errorHandler) {
        this.plugin = plugin;
        this.mines = mines;
        this.errorHandler = errorHandler;
    }

    public void setTeleport(CommandSender sender, String mineName) {
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

    public void setSpawn(CommandSender sender, String mineName) {
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

    public void clearSpawn(CommandSender sender, String mineName) {
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

    public String formatLocation(Location loc) {
        return String.format("%.1f, %.1f, %.1f in %s",
                loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
    }
}
