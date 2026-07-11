package dev.loki.lomines.command.player;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.util.ErrorHandler;
import dev.loki.lomines.wand.group.GroupWandItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayerCommands {

    private final LoMinesPlugin plugin;
    private final ErrorHandler errorHandler;

    public PlayerCommands(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.errorHandler = new ErrorHandler(plugin.getLogger(), "[LoMines] ");
    }

    public void handle(CommandSender sender, String subcommand, String[] args) {
        switch (subcommand.toLowerCase()) {
            case "wand" -> {
                if (!(sender instanceof Player player)) {
                    errorHandler.sendError(sender, "Только для игрока.");
                    return;
                }
                player.getInventory().addItem(GroupWandItem.create(plugin));
                player.sendMessage(Component.text(
                        "Палочка группы: ЛКМ/ПКМ по блоку — углы, Shift+ПКМ — меню (9 шахт). Задайте /lm group <префикс>",
                        NamedTextColor.GREEN));
            }
            case "group" -> {
                if (args.length < 1) {
                    sender.sendMessage(Component.text("Usage: /lm group <prefix>", NamedTextColor.RED));
                    return;
                }
                if (!(sender instanceof Player player)) {
                    errorHandler.sendError(sender, "Префикс задаётся в игре.");
                    return;
                }
                plugin.getGroupWandManager().getSession(player.getUniqueId()).setBaseName(args[0]);
                String p = args[0].trim();
                player.sendMessage(Component.text("Префикс: " + p + " → файлы " + p + "_1.yml … " + p + "_9.yml", NamedTextColor.AQUA));
            }
            default -> {}
        }
    }
}
