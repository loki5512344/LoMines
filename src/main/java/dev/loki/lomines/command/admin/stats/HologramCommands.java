package dev.loki.lomines.command.admin.stats;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.integration.hologram.HologramManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class HologramCommands {

    private final LoMinesPlugin plugin;
    private final HologramManager hologramManager;

    public HologramCommands(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.hologramManager = plugin.getHologramManager();
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /lm hologram <mine> [on|off]", NamedTextColor.RED));
            return;
        }
        String mineName = args[0];
        String state = args.length > 1 ? args[1] : null;

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
            enable = hologramManager.toggleHologram(mine);
        } else {
            enable = "on".equalsIgnoreCase(state) || "true".equalsIgnoreCase(state) || "enable".equalsIgnoreCase(state);
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
