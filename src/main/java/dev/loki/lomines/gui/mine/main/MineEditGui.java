package dev.loki.lomines.gui.mine.main;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.gui.confirm.ConfirmDeleteGui;
import dev.loki.lomines.gui.mine.edit.blocks.BlocksGui;
import dev.loki.lomines.gui.mine.edit.reset.ResetGui;
import dev.loki.lomines.gui.mine.edit.rewards.RewardsGui;
import dev.loki.lomines.gui.mine.holder.MineEditGuiHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * Main GUI for editing a mine.
 * Provides navigation to sub-editors for different configuration sections.
 * Improved UX with better materials, enhanced lore, and clearer information display.
 */
public final class MineEditGui {

    // Slot constants
    public static final int SLOT_BACK = 49;
    public static final int SLOT_BLOCKS = 10;
    public static final int SLOT_REGIONS = 12;
    public static final int SLOT_RESET = 14;
    public static final int SLOT_REWARDS = 16;
    public static final int SLOT_TELEPORT = 28;
    public static final int SLOT_UI = 30;
    public static final int SLOT_SAVE = 32;
    public static final int SLOT_DELETE = 34;
    public static final int SLOT_INFO = 40;
    private static final int SIZE = 54;
    private static final String TITLE_PREFIX = "Редактор шахты: ";

    private MineEditGui() {
    }

    public static void open(LoMinesPlugin plugin, Player player, String mineName) {
        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            player.sendMessage(Component.text("Шахта не найдена: " + mineName, NamedTextColor.RED));
            return;
        }
        MineEditGuiHolder holder = new MineEditGuiHolder(player.getUniqueId(), mineName);
        Inventory inv = Bukkit.createInventory(holder, SIZE, Component.text(TITLE_PREFIX + mineName, NamedTextColor.DARK_GREEN));
        holder.setInventory(inv);
        fill(plugin, mine, inv);
        player.openInventory(inv);
    }

    public static void refresh(LoMinesPlugin plugin, Player player, Inventory inv, String mineName) {
        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            player.closeInventory();
            return;
        }
        fill(plugin, mine, inv);
    }

    private static void fill(LoMinesPlugin plugin, Mine mine, Inventory inv) {
        MineConfig config = mine.getConfig();
        for (int i = 0; i < SIZE; i++) inv.setItem(i, MineEditItems.filler());
        inv.setItem(SLOT_BLOCKS, MineEditItems.blocksItem(config));
        inv.setItem(SLOT_REGIONS, MineEditItems.regionsItem(config));
        inv.setItem(SLOT_RESET, MineEditItems.resetItem(config));
        inv.setItem(SLOT_REWARDS, MineEditItems.rewardsItem(config));
        inv.setItem(SLOT_TELEPORT, MineEditItems.teleportItem(config));
        inv.setItem(SLOT_UI, MineEditItems.uiItem(config));
        inv.setItem(SLOT_SAVE, MineEditItems.saveItem());
        inv.setItem(SLOT_DELETE, MineEditItems.deleteItem(mine.getName()));
        inv.setItem(SLOT_INFO, MineEditItems.infoItem(mine));
        inv.setItem(SLOT_BACK, MineEditItems.backItem());
    }

    public static boolean handleClick(LoMinesPlugin plugin, Player player, int rawSlot, String mineName) {
        if (rawSlot < 0 || rawSlot >= SIZE) return false;
        switch (rawSlot) {
            case SLOT_BACK -> {
                player.closeInventory();
                return true;
            }
            case SLOT_BLOCKS -> {
                BlocksGui.open(plugin, player, mineName);
                return true;
            }
            case SLOT_RESET -> {
                ResetGui.open(plugin, player, mineName);
                return true;
            }
            case SLOT_REWARDS -> {
                RewardsGui.open(plugin, player, mineName);
                return true;
            }
            case SLOT_TELEPORT -> {
                player.sendMessage(Component.text("Используйте команды /lm setteleport и /lm setspawn", NamedTextColor.YELLOW));
                return true;
            }
            case SLOT_UI -> {
                player.sendMessage(Component.text("Настройки UI редактируются в конфиге", NamedTextColor.YELLOW));
                return true;
            }
            case SLOT_SAVE -> {
                player.sendMessage(Component.text("Конфигурация сохранена для шахты: " + mineName, NamedTextColor.GREEN));
                player.closeInventory();
                return true;
            }
            case SLOT_DELETE -> {
                ConfirmDeleteGui.open(plugin, player, mineName);
                return true;
            }
            case SLOT_INFO, SLOT_REGIONS -> {
                return true;
            }
        }
        return true;
    }
}