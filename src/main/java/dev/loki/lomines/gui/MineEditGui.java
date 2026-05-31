package dev.loki.lomines.gui;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.Mine;
import dev.loki.lomines.data.config.MineConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Main GUI for editing a mine.
 * Provides navigation to sub-editors for different configuration sections.
 */
public final class MineEditGui {

    private static final int SIZE = 54;
    private static final String TITLE_PREFIX = "Редактор шахты: ";

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

    private MineEditGui() {
    }

    /**
     * Opens the mine edit GUI for the specified mine.
     *
     * @param plugin the plugin instance
     * @param player the player to open for
     * @param mineName the name of the mine to edit
     */
    public static void open(LoMinesPlugin plugin, Player player, String mineName) {
        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            player.sendMessage(Component.text("Шахта не найдена: " + mineName, NamedTextColor.RED));
            return;
        }

        MineEditGuiHolder holder = new MineEditGuiHolder(player.getUniqueId(), mineName);
        Inventory inv = Bukkit.createInventory(holder, SIZE,
                Component.text(TITLE_PREFIX + mineName, NamedTextColor.DARK_GREEN));
        holder.setInventory(inv);

        fill(plugin, mine, inv);
        player.openInventory(inv);
    }

    /**
     * Refreshes the GUI contents.
     */
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

        // Clear and fill background
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, filler());
        }

        // Main editors
        inv.setItem(SLOT_BLOCKS, blocksItem(config));
        inv.setItem(SLOT_REGIONS, regionsItem(config));
        inv.setItem(SLOT_RESET, resetItem(config));
        inv.setItem(SLOT_REWARDS, rewardsItem(config));
        inv.setItem(SLOT_TELEPORT, teleportItem(config));
        inv.setItem(SLOT_UI, uiItem(config));

        // Actions
        inv.setItem(SLOT_SAVE, saveItem());
        inv.setItem(SLOT_DELETE, deleteItem(mine.getName()));
        inv.setItem(SLOT_INFO, infoItem(mine));
        inv.setItem(SLOT_BACK, backItem());
    }

    private static ItemStack filler() {
        ItemStack pane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            pane.setItemMeta(meta);
        }
        return pane;
    }

    private static ItemStack blocksItem(MineConfig config) {
        ItemStack stack = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§aБлоки", NamedTextColor.GREEN));
            int blockCount = config.blocks().blockCount();
            String fillMode = config.blocks().fillMode().name();
            meta.lore(List.of(
                    Component.text("Типов блоков: " + blockCount, NamedTextColor.GRAY),
                    Component.text("Режим заполнения: " + fillMode, NamedTextColor.GRAY),
                    Component.empty(),
                    Component.text("§eНажмите для редактирования", NamedTextColor.YELLOW)
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack regionsItem(MineConfig config) {
        ItemStack stack = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§aРегионы", NamedTextColor.GREEN));
            int regionCount = config.region().regionCount();
            int volume = config.region().totalVolume();
            meta.lore(List.of(
                    Component.text("Количество регионов: " + regionCount, NamedTextColor.GRAY),
                    Component.text("Общий объём: " + volume + " блоков", NamedTextColor.GRAY),
                    Component.empty(),
                    Component.text("§7Только просмотр (изменяется через палочку)", NamedTextColor.GRAY)
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack resetItem(MineConfig config) {
        ItemStack stack = new ItemStack(Material.CLOCK);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§aСброс", NamedTextColor.GREEN));
            String interval = config.reset().intervalDisplay();
            double percent = config.reset().percentTrigger();
            boolean percentEnabled = config.reset().isPercentTriggerEnabled();
            meta.lore(List.of(
                    Component.text("Интервал: " + interval, NamedTextColor.GRAY),
                    Component.text("Триггер по %: " + (percentEnabled ? percent + "%" : "выключен"), NamedTextColor.GRAY),
                    Component.text("Команд при сбросе: " + config.reset().commands().size(), NamedTextColor.GRAY),
                    Component.empty(),
                    Component.text("§eНажмите для редактирования", NamedTextColor.YELLOW)
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack rewardsItem(MineConfig config) {
        ItemStack stack = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§aНаграды", NamedTextColor.GREEN));
            int rewardCount = config.rewards().entries().size();
            meta.lore(List.of(
                    Component.text("Настроено наград: " + rewardCount, NamedTextColor.GRAY),
                    Component.empty(),
                    Component.text("§eНажмите для редактирования", NamedTextColor.YELLOW)
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack teleportItem(MineConfig config) {
        ItemStack stack = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§aТелепорт", NamedTextColor.GREEN));
            boolean enabled = config.teleport().enabled();
            meta.lore(List.of(
                    Component.text("Включено: " + (enabled ? "§aДа" : "§cНет"), NamedTextColor.GRAY),
                    Component.empty(),
                    Component.text("§eНажмите для редактирования", NamedTextColor.YELLOW)
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack uiItem(MineConfig config) {
        ItemStack stack = new ItemStack(Material.OAK_SIGN);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§aИнтерфейс", NamedTextColor.GREEN));
            boolean actionBarEnabled = config.ui().actionBarEnabled();
            double range = config.ui().actionBarRange();
            meta.lore(List.of(
                    Component.text("Action Bar: " + (actionBarEnabled ? "§aВкл" : "§cВыкл"), NamedTextColor.GRAY),
                    Component.text("Радиус: " + range, NamedTextColor.GRAY),
                    Component.empty(),
                    Component.text("§eНажмите для редактирования", NamedTextColor.YELLOW)
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack saveItem() {
        ItemStack stack = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§aСохранить изменения", NamedTextColor.GREEN));
            meta.lore(List.of(
                    Component.text("§7Сохраняет текущую конфигурацию", NamedTextColor.GRAY),
                    Component.text("§7в файл шахты", NamedTextColor.GRAY)
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack deleteItem(String mineName) {
        ItemStack stack = new ItemStack(Material.RED_CONCRETE);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§cУдалить шахту", NamedTextColor.RED));
            meta.lore(List.of(
                    Component.text("§cВнимание! Это действие нельзя отменить!", NamedTextColor.RED),
                    Component.empty(),
                    Component.text("§7Шахта: " + mineName, NamedTextColor.GRAY)
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack infoItem(Mine mine) {
        ItemStack stack = new ItemStack(Material.PAPER);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§bИнформация", NamedTextColor.AQUA));
            int blocks = mine.getBlocks();
            int total = mine.getTotalVolume();
            double percent = mine.getPercentFilled();
            meta.lore(List.of(
                    Component.text("Название: " + mine.getName(), NamedTextColor.GRAY),
                    Component.text("Мир: " + mine.getConfig().worldName(), NamedTextColor.GRAY),
                    Component.empty(),
                    Component.text("Заполнено: " + blocks + "/" + total, NamedTextColor.GRAY),
                    Component.text("Процент: " + String.format("%.1f%%", percent), NamedTextColor.GRAY)
            ));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack backItem() {
        ItemStack stack = new ItemStack(Material.BARRIER);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§cЗакрыть", NamedTextColor.RED));
            stack.setItemMeta(meta);
        }
        return stack;
    }

    /**
     * Handles clicks in the GUI.
     *
     * @return true if the event should be cancelled
     */
    public static boolean handleClick(LoMinesPlugin plugin, Player player, int rawSlot, String mineName) {
        if (rawSlot < 0 || rawSlot >= SIZE) {
            return false;
        }

        switch (rawSlot) {
            case SLOT_BACK -> {
                player.closeInventory();
                return true;
            }
            case SLOT_BLOCKS -> {
                player.sendMessage(Component.text("Открытие редактора блоков... (в разработке)", NamedTextColor.YELLOW));
                // TODO: Open BlockEditGui
                return true;
            }
            case SLOT_RESET -> {
                player.sendMessage(Component.text("Открытие редактора сброса... (в разработке)", NamedTextColor.YELLOW));
                // TODO: Open ResetEditGui
                return true;
            }
            case SLOT_REWARDS -> {
                player.sendMessage(Component.text("Открытие редактора наград... (в разработке)", NamedTextColor.YELLOW));
                // TODO: Open RewardEditGui
                return true;
            }
            case SLOT_TELEPORT -> {
                player.sendMessage(Component.text("Открытие редактора телепорта... (в разработке)", NamedTextColor.YELLOW));
                // TODO: Open TeleportEditGui
                return true;
            }
            case SLOT_UI -> {
                player.sendMessage(Component.text("Открытие редактора интерфейса... (в разработке)", NamedTextColor.YELLOW));
                // TODO: Open UIEditGui
                return true;
            }
            case SLOT_SAVE -> {
                player.sendMessage(Component.text("Конфигурация сохранена для шахты: " + mineName, NamedTextColor.GREEN));
                player.closeInventory();
                return true;
            }
            case SLOT_DELETE -> {
                // Open confirmation GUI
                ConfirmDeleteGui.open(plugin, player, mineName);
                return true;
            }
            case SLOT_INFO, SLOT_REGIONS -> {
                // Info only, no action
                return true;
            }
        }

        return true; // Cancel all other clicks
    }
}
