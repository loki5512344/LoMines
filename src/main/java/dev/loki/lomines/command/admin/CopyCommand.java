package dev.loki.lomines.command.admin;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.data.config.MineConfig;
import dev.lolib.commands.annotation.Arg;
import dev.lolib.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

/**
 * Command to copy mine configuration from one mine to another.
 */
public class CopyCommand {

    private final LoMinesPlugin plugin;

    public CopyCommand(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Copies mine configuration (blocks, reset settings, rewards, etc.)
     * from source mine to target mine. Regions and positions are NOT copied.
     * Usage: /lm copy <from> <to>
     */
    @Subcommand(value = "copy", permission = "lomines.admin.copy")
    void copy(CommandSender sender,
              @Arg("from-mine") String fromMineName,
              @Arg("to-mine") String toMineName) {

        Mine fromMine = plugin.getMines().find(fromMineName).orElse(null);
        if (fromMine == null) {
            sender.sendMessage(Component.text("Исходная шахта не найдена: " + fromMineName, NamedTextColor.RED));
            return;
        }

        Mine toMine = plugin.getMines().find(toMineName).orElse(null);
        if (toMine == null) {
            sender.sendMessage(Component.text("Целевая шахта не найдена: " + toMineName, NamedTextColor.RED));
            return;
        }

        if (fromMine.getName().equalsIgnoreCase(toMine.getName())) {
            sender.sendMessage(Component.text("Нельзя копировать шахту саму в себя!", NamedTextColor.RED));
            return;
        }

        var sourceConfig = fromMine.getConfig();
        var targetConfig = toMine.getConfig();

        // Keep target region/name, copy all other sections from source.
        MineConfig newTargetConfig = MineConfig.builder(toMine.getName())
                .region(targetConfig.region())
                .blocks(sourceConfig.blocks())
                .reset(sourceConfig.reset())
                .rewards(sourceConfig.rewards())
                .teleport(sourceConfig.teleport())
                .ui(sourceConfig.ui())
                .worldGuard(sourceConfig.worldGuard())
                .playerSpawn(sourceConfig.playerSpawn())
                .build();

        try {
            plugin.getMines().updateMineConfig(toMine.getName(), newTargetConfig);
        } catch (Exception e) {
            sender.sendMessage(Component.text("Ошибка сохранения: " + e.getMessage(), NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("§aКонфигурация скопирована из §f" + fromMine.getName() +
                " §aв §f" + toMine.getName()));
        sender.sendMessage(Component.text("§7Скопировано:"));
        sender.sendMessage(Component.text("  §7• Блоки: §f" + sourceConfig.blocks().weights().size() + " типов"));
        sender.sendMessage(Component.text("  §7• Настройки сброса"));
        sender.sendMessage(Component.text("  §7• Точки телепорта/спавна"));
        sender.sendMessage(Component.text("  §7• Награды: §f" + sourceConfig.rewards().entries().size() + " записей"));
        sender.sendMessage(Component.text("§7§oПримечание: регионы и позиции не скопированы"));
    }
}
