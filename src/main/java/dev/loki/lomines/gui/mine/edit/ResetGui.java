package dev.loki.lomines.gui.mine.edit;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.gui.common.ItemStackFactory;
import dev.loki.lomines.gui.mine.holder.ResetGuiHolder;
import dev.loki.lomines.gui.mine.main.MineEditGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * GUI for editing reset configuration of a mine.
 */
public final class ResetGui {

    private static final int SIZE = 36;
    private static final String TITLE_PREFIX = "Настройка сброса: ";
    private static final int SLOT_BACK = 31;
    private static final int SLOT_INTERVAL = 10;
    private static final int SLOT_PERCENT_TRIGGER = 12;
    private static final int SLOT_MESSAGE = 14;
    private static final int SLOT_COMMANDS = 16;

    private ResetGui() {}

    public static void open(LoMinesPlugin plugin, Player player, String mineName) {
        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            player.sendMessage(Component.text("Шахта не найдена: " + mineName, NamedTextColor.RED));
            return;
        }
        ResetGuiHolder holder = new ResetGuiHolder(player.getUniqueId(), mineName);
        Inventory inv = Bukkit.createInventory(holder, SIZE,
                Component.text(TITLE_PREFIX + mineName, NamedTextColor.DARK_GREEN));
        holder.setInventory(inv);
        fill(mine, inv);
        player.openInventory(inv);
    }

    public static void refresh(Player player, Inventory inv, String mineName, LoMinesPlugin plugin) {
        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            player.closeInventory();
            return;
        }
        fill(mine, inv);
    }

    private static void fill(Mine mine, Inventory inv) {
        var config = mine.getConfig().reset();

        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, ItemStackFactory.filler());
        }

        inv.setItem(SLOT_INTERVAL, ResetGuiItems.intervalItem(config));
        inv.setItem(SLOT_PERCENT_TRIGGER, ResetGuiItems.percentTriggerItem(config));
        inv.setItem(SLOT_MESSAGE, ResetGuiItems.messageItem(config));
        inv.setItem(SLOT_COMMANDS, ResetGuiItems.commandsItem(config));
        inv.setItem(SLOT_BACK, ResetGuiItems.backItem());
    }

    public static boolean handleClick(LoMinesPlugin plugin, Player player, int rawSlot,
                                     String mineName, boolean leftClick, boolean rightClick,
                                     boolean shiftClick) {
        if (rawSlot < 0 || rawSlot >= SIZE) return false;

        if (rawSlot == SLOT_BACK) {
            MineEditGui.open(plugin, player, mineName);
            return true;
        }

        if (rawSlot == SLOT_INTERVAL) {
            adjustInterval(plugin, player, mineName, leftClick, rightClick, shiftClick);
            return true;
        }

        if (rawSlot == SLOT_PERCENT_TRIGGER) {
            togglePercentTrigger(plugin, player, mineName, leftClick, rightClick);
            return true;
        }

        if (rawSlot == SLOT_MESSAGE) {
            if (leftClick) {
                player.sendMessage(Component.text("§eВведите сообщение в чат (или 'cancel' для отмены):"));
                player.closeInventory();
            } else if (rightClick) {
                player.sendMessage(Component.text("§cСообщение очищено."));
                refresh(player, player.getOpenInventory().getTopInventory(), mineName, plugin);
            }
            return true;
        }

        if (rawSlot == SLOT_COMMANDS && leftClick) {
            player.sendMessage(Component.text("§eРедактор команд пока в разработке."));
            return true;
        }

        return true;
    }

    private static void adjustInterval(LoMinesPlugin plugin, Player player, String mineName,
                                       boolean leftClick, boolean rightClick, boolean shiftClick) {
        int delta = 0;
        if (leftClick && shiftClick) {
            delta = 600;
        } else if (rightClick && shiftClick) {
            delta = -600;
        } else if (leftClick) {
            delta = 60;
        } else if (rightClick) {
            delta = -60;
        }

        if (delta != 0) {
            player.sendMessage(Component.text("§aИнтервал изменен: " + (delta > 0 ? "+" : "") + (delta / 60) + " мин."));
            refresh(player, player.getOpenInventory().getTopInventory(), mineName, plugin);
        }
    }

    private static void togglePercentTrigger(LoMinesPlugin plugin, Player player,
                                              String mineName, boolean leftClick, boolean rightClick) {
        if (leftClick) {
            player.sendMessage(Component.text("§aТриггер по % переключен."));
        } else if (rightClick) {
            player.sendMessage(Component.text("§eВведите значение триггера (0-100):"));
            player.closeInventory();
        }
        refresh(player, player.getOpenInventory().getTopInventory(), mineName, plugin);
    }
}
