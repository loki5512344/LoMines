package dev.loki.lomines.command.player;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.util.location.geo.Cuboid;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TeleportCommand {

    private final LoMinesPlugin plugin;

    public TeleportCommand(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /lm tp <mine>", NamedTextColor.RED));
            return;
        }
        String mineName = args[0];

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

        if (config.teleport().enabled() && config.teleport().getLocation().isPresent()) {
            target = config.teleport().getLocation().get();
        } else if (!mine.getRegions().isEmpty()) {
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
