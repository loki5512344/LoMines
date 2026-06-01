package dev.loki.lomines.command.admin;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.integration.hologram.HologramManager;
import dev.lolilb.commands.annotation.Arg;
import dev.lolilb.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Commands for managing holograms above mines.
 */
class HologramCommands {

    private final LoMinesPlugin plugin;
    private final HologramManager hologramManager;

    HologramCommands(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.hologramManager = plugin.getHologramManager();
    }

    /**
     * Toggles hologram for a mine.
     * Usage: /lm hologram <mine> [on|off]
     */
    @Subcommand(value = "hologram", permission = "lomines.admin.hologram")
    void hologram(CommandSender sender, @Arg("mine") String mineName,
                  @Arg(value = "state", optional = true) String state) {
        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            sender.sendMessage(Component.text("Шахта не найдена: " + mineName, NamedTextColor.RED));
            return;
        }

        if (hologramManager.getProviderName() == null) {
            sender.sendMessage(Component.text("Голограммы недоступны. Установите HolographicDisplays или DecentHolograms.", NamedTextColor.RED));
            return;
        }

        boolean enable;
        if (state == null) {
            // Toggle
            enable = hologramManager.toggleHologram(mine);
        } else {
            enable = state.equalsIgnoreCase("on") || state.equalsIgnoreCase("true") || state.equalsIgnoreCase("enable");
            if (enable) {
                hologramManager.createMineHologram(mine);
            } else {
                hologramManager.deleteMineHologram(mineName);
            }
        }

        sender.sendMessage(Component.text(
            "Голограмма для шахты " + mineName + " " + (enable ? "§aвключена" : "§cвыключена")));
    }
}
