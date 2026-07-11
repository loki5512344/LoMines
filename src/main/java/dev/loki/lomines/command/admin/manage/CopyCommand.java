package dev.loki.lomines.command.admin.manage;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.data.config.model.MineConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

public class CopyCommand {

    private final LoMinesPlugin plugin;

    public CopyCommand(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /lm copy <from> <to>", NamedTextColor.RED));
            return;
        }
        String fromMineName = args[0];
        String toMineName = args[1];

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
