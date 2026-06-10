package dev.loki.lomines.command.player;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.util.location.Cuboid;
import dev.lolib.commands.annotation.Arg;
import dev.lolib.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to teleport to a mine.
 */
public class TeleportCommand {

    private final LoMinesPlugin plugin;

    public TeleportCommand(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Teleports player to the mine's location.
     * Usage: /lm tp <mine>
     */
    @Subcommand(value = "tp", permission = "lomines.teleport")
    void teleport(CommandSender sender, @Arg("mine") String mineName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Эта команда только для игроков!", NamedTextColor.RED));
            return;
        }

        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            player.sendMessage(Component.text("Шахта не найдена: " + mineName, NamedTextColor.RED));
            return;
        }

        var config = mine.getConfig();
        Location target;

        // Try teleport location first
        if (config.teleport().enabled() && config.teleport().getLocation().isPresent()) {
            target = config.teleport().getLocation().get();
        } else if (!mine.getRegions().isEmpty()) {
            // Use center of first region
            Cuboid region = mine.getRegions().get(0);
            double centerX = (region.getMinX() + region.getMaxX()) / 2.0 + 0.5;
            double centerZ = (region.getMinZ() + region.getMaxZ()) / 2.0 + 0.5;
            double centerY = region.getMaxY() + 1;
            target = new Location(region.getWorld(), centerX, centerY, centerZ);
        } else {
            player.sendMessage(Component.text("У шахты нет настроенной точки телепорта!", NamedTextColor.RED));
            return;
        }

        player.teleport(target);
        player.sendMessage(Component.text("§aТелепортировано к шахте §f" + mine.getName() +
                " §a(§f" + String.format("%.0f, %.0f, %.0f", target.getX(), target.getY(), target.getZ()) + "§a)"));
    }
}
