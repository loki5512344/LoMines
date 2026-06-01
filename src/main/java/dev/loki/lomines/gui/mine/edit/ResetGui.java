package dev.loki.lomines.gui.mine.edit;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.data.config.reset.ResetConfig;
import dev.loki.lomines.gui.common.ItemStackFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * GUI for editing reset configuration of a mine.
 * Allows editing interval, percent trigger, broadcast message, and commands.
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
        ResetConfig config = mine.getConfig().reset();

        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, ItemStackFactory.filler());
        }

        inv.setItem(SLOT_INTERVAL, intervalItem(config));
        inv.setItem(SLOT_PERCENT_TRIGGER, percentTriggerItem(config));
        inv.setItem(SLOT_MESSAGE, messageItem(config));
        inv.setItem(SLOT_COMMANDS, commandsItem(config));
        inv.setItem(SLOT_BACK, backItem());
    }

    private static ItemStack intervalItem(ResetConfig config) {
        return ItemStackFactory.create(Material.CLOCK, "§a§lИнтервал сброса",
            "§8───────────────",
            "§7Текущее: §f" + config.intervalDisplay(),
            "",
            "§e▸ ЛКМ §7+1 минута",
            "§e▸ ПКМ §7-1 минута",
            "§e▸ Shift+ЛКМ §7+10 минут",
            "§e▸ Shift+ПКМ §7-10 минут"
        );
    }

    private static ItemStack percentTriggerItem(ResetConfig config) {
        String status = config.isPercentTriggerEnabled() ? "§aвкл" : "§7выкл";
        String value = config.isPercentTriggerEnabled() ? config.percentTrigger() + "%" : "—";

        return ItemStackFactory.create(Material.COMPARATOR, "§a§lТриггер по %",
            "§8───────────────",
            "§7Статус: " + status,
            "§7Значение: §f" + value,
            "",
            "§7Сброс шахты когда",
            "§7добыто указанный %",
            "",
            "§e▸ ЛКМ §7вкл/выкл",
            "§e▸ ПКМ §7изменить %"
        );
    }

    private static ItemStack messageItem(ResetConfig config) {
        String message = config.broadcastMessage().isEmpty() ? "§7(не задано)" : "§f" + config.broadcastMessage();
        return ItemStackFactory.create(Material.BOOK, "§a§lСообщение при сбросе",
            "§8───────────────",
            "§7Текущее:",
            "  " + ItemStackFactory.trunc(message, 30),
            "",
            "§7Поддерживает MiniMessage",
            "§8{mine} §7— название шахты",
            "§8{player} §7— игрок вызвавший сброс",
            "",
            "§e▸ ЛКМ §7изменить сообщение",
            "§e▸ ПКМ §7очистить"
        );
    }

    private static ItemStack commandsItem(ResetConfig config) {
        int count = config.commands().size();
        return ItemStackFactory.create(Material.COMMAND_BLOCK, "§a§lКоманды при сбросе",
            "§8───────────────",
            "§7Количество: §f" + count,
            "",
            "§7Плейсхолдеры:",
            "§8{mine} §7— название шахты",
            "§8{world} §7— мир",
            "",
            "§e▸ ЛКМ §7редактировать список"
        );
    }

    private static ItemStack backItem() {
        return ItemStackFactory.create(Material.ARROW, "§c§lНазад",
            "§8───────────────",
            "§7Вернуться в редактор шахты",
            "",
            "§e▸ Нажмите для возврата"
        );
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
            delta = 600; // +10 minutes
        } else if (rightClick && shiftClick) {
            delta = -600; // -10 minutes
        } else if (leftClick) {
            delta = 60; // +1 minute
        } else if (rightClick) {
            delta = -60; // -1 minute
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
