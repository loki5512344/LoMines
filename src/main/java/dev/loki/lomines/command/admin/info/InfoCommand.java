package dev.loki.lomines.command.admin.info;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.data.config.block.BlockKey;
import dev.loki.lomines.util.location.geo.Cuboid;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class InfoCommand {

    private final LoMinesPlugin plugin;

    public InfoCommand(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    public void handle(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /lm info <mine>", NamedTextColor.RED));
            return;
        }
        String mineName = args[0];

        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            sender.sendMessage(Component.text("Шахта не найдена: " + mineName, NamedTextColor.RED));
            return;
        }

        var config = mine.getConfig();
        sender.sendMessage(Component.text("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
        sender.sendMessage(Component.text("§6§lИнформация о шахте: §f" + mine.getName()));
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("§7Мир: §f" + config.worldName()));
        sender.sendMessage(Component.text("§7Блоков: §f" + mine.getBlocks() + "§7/§f" + mine.getTotalVolume() +
                " §7(§f" + String.format("%.1f", mine.getPercentFilled()) + "%§7)"));

        sender.sendMessage(Component.text("§6§lРегионы:"));
        List<Cuboid> regions = mine.getRegions();
        for (int i = 0; i < regions.size(); i++) {
            Cuboid r = regions.get(i);
            sender.sendMessage(Component.text("§7  #" + (i + 1) + ": §f" +
                    r.getMinX() + "," + r.getMinY() + "," + r.getMinZ() + " §7-> §f" +
                    r.getMaxX() + "," + r.getMaxY() + "," + r.getMaxZ() +
                    " §7(§f" + r.getVolume() + " §7блоков)"));
        }
        sender.sendMessage(Component.text(""));
        sender.sendMessage(Component.text("§6§lБлоки:"));
        Map<BlockKey, Double> weights = config.blocks().weights();
        weights.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(10)
                .forEach(e -> sender.sendMessage(Component.text(
                        "§7  " + formatBlockKey(e.getKey()) + ": §f" + String.format("%.1f", e.getValue() * 100) + "%")));
        if (weights.size() > 10) {
            sender.sendMessage(Component.text("§7  ... и ещё " + (weights.size() - 10) + " блоков"));
        }

        sender.sendMessage(Component.text("§6§lНастройки сброса:"));
        sender.sendMessage(Component.text("§7  Интервал: §f" + config.reset().intervalDisplay()));
        sender.sendMessage(Component.text("§7  Триггер по %: §f" +
                (config.reset().isPercentTriggerEnabled() ? config.reset().percentTrigger() + "%" : "выкл")));

        sender.sendMessage(Component.text("§6§lТелепортация:"));
        sender.sendMessage(Component.text("§7  Точка телепорта: §f" +
                (config.teleport().enabled() ? "§aустановлена" : "§7не задана")));
        sender.sendMessage(Component.text("§7  Точка спавна: §f" +
                (config.playerSpawn().enabled() ? "§aустановлена" : "§7используется точка телепорта")));

        sender.sendMessage(Component.text("§6§lНаграды: §f" + config.rewards().entries().size() + " записей"));
        sender.sendMessage(Component.text("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
    }

    private String formatBlockKey(BlockKey key) {
        return switch (key) {
            case BlockKey.Vanilla v -> v.material().name().toLowerCase();
            case BlockKey.Oraxen o -> "oraxen:" + o.id();
            case BlockKey.ItemsAdder i -> "ia:" + i.id();
        };
    }
}
