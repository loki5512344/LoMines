package dev.loki.lomines.command.player;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.util.ErrorHandler;
import dev.loki.lomines.wand.GroupWandItem;
import dev.lolib.commands.annotation.Arg;
import dev.lolib.commands.annotation.Command;
import dev.lolib.commands.annotation.Subcommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Player commands for mine interaction.
 * Provides commands for wand and group management.
 */
@Command(value = "lm", permission = "lomines.use")
public class PlayerCommands {

    private final LoMinesPlugin plugin;
    private final ErrorHandler errorHandler;

    public PlayerCommands(LoMinesPlugin plugin) {
        this.plugin = plugin;
        this.errorHandler = new ErrorHandler(plugin.getLogger(), "[LoMines] ");
    }

    @Subcommand(value = "wand", permission = "lomines.admin.wand")
    public void wand(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            errorHandler.sendError(sender, "Только для игрока.");
            return;
        }
        player.getInventory().addItem(GroupWandItem.create(plugin));
        player.sendMessage(Component.text(
                "Палочка группы: ЛКМ/ПКМ по блоку — углы, Shift+ПКМ — меню (9 шахт). Задайте /lm group <префикс>",
                NamedTextColor.GREEN));
    }

    @Subcommand(value = "group", permission = "lomines.admin.wand")
    public void group(CommandSender sender, @Arg("prefix") String prefix) {
        if (!(sender instanceof Player player)) {
            errorHandler.sendError(sender, "Префикс задаётся в игре.");
            return;
        }
        plugin.getGroupWandManager().getSession(player.getUniqueId()).setBaseName(prefix);
        String p = prefix.trim();
        player.sendMessage(Component.text("Префикс: " + p + " → файлы " + p + "_1.yml … " + p + "_9.yml", NamedTextColor.AQUA));
    }
}
