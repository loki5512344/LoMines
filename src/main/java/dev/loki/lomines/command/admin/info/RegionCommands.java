package dev.loki.lomines.command.admin.info;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.util.location.geo.Cuboid;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class RegionCommands {

    private final LoMinesPlugin plugin;
    private final RegionActionHandler actionHandler;

    public RegionCommands(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.actionHandler = new RegionActionHandler(plugin);
    }

    public void handle(CommandSender sender, String subcommand, String[] args) {
        switch (subcommand.toLowerCase()) {
            case "regions" -> {
                if (args.length < 1) {
                    sender.sendMessage(Component.text("Usage: /lm regions <mine>", NamedTextColor.RED));
                    return;
                }
                String mineName = args[0];
                Mine mine = plugin.getMines().find(mineName).orElse(null);
                if (mine == null) {
                    sender.sendMessage(Component.text("Шахта не найдена: " + mineName, NamedTextColor.RED));
                    return;
                }

                List<Cuboid> regions = mine.getRegions();
                sender.sendMessage(Component.text("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                sender.sendMessage(Component.text("§6§lРегионы шахты §f" + mine.getName()));
                sender.sendMessage(Component.text("§7Всего регионов: §f" + regions.size()));
                sender.sendMessage(Component.text(""));

                for (int i = 0; i < regions.size(); i++) {
                    Cuboid r = regions.get(i);
                    sender.sendMessage(Component.text("§e#" + (i + 1) + " §7(§f" + r.getVolume() + " §7блоков)"));
                    sender.sendMessage(Component.text("  §7Мир: §f" + r.getWorld().getName()));
                    sender.sendMessage(Component.text("  §7От: §f" + r.getMinX() + ", " + r.getMinY() + ", " + r.getMinZ()));
                    sender.sendMessage(Component.text("  §7До: §f" + r.getMaxX() + ", " + r.getMaxY() + ", " + r.getMaxZ()));
                }
                sender.sendMessage(Component.text("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            }
            case "addregion" -> {
                if (args.length < 1) {
                    sender.sendMessage(Component.text("Usage: /lm addregion <mine>", NamedTextColor.RED));
                    return;
                }
                actionHandler.addRegion(sender, args[0]);
            }
            case "removeregion" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /lm removeregion <mine> <index>", NamedTextColor.RED));
                    return;
                }
                try {
                    int index = Integer.parseInt(args[1]);
                    actionHandler.removeRegion(sender, args[0], index);
                } catch (NumberFormatException e) {
                    sender.sendMessage(Component.text("Индекс должен быть числом: " + args[1], NamedTextColor.RED));
                }
            }
            default -> {}
        }
    }
}
