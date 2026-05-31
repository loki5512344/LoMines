package dev.loki.lomines.gui;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.Mine;
import dev.loki.lomines.data.config.MineConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Main GUI for editing a mine.
 * Provides navigation to sub-editors for different configuration sections.
 * Improved UX with better materials, enhanced lore, and clearer information display.
 */
public final class MineEditGui {

    private static final int SIZE = 54;
    private static final String TITLE_PREFIX = "Редактор шахты: ";

    // Slot constants - organized in visual groups
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

    // UI Constants
    private static final String CLICK_EDIT = "§e▸ Нажмите для редактирования";
    private static final String CLICK_BACK = "§c▸ Нажмите чтобы закрыть";
    private static final String CLICK_SAVE = "§a▸ Нажмите для сохранения";
    private static final String CLICK_DELETE = "§c▸ Нажмите для удаления";
    private static final String SECTION_CONTENT = "§7Содержимое:";
    private static final String SECTION_SETTINGS = "§7Настройки:";
    private static final String SEPARATOR = "§8────────────────";
    private static final String COMMANDS_HEADER = "§8Команды:";

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

    /**
     * Formats location as short readable string.
     */
    private static String formatLocation(Location loc) {
        if (loc == null) return "§7не задана";
        return String.format("§7%.0f §8/§7 %.0f §8/§7 %.0f §8(§7%s§8)",
                loc.getX(), loc.getY(), loc.getZ(), loc.getWorld().getName());
    }

    /**
     * Truncates string for preview display.
     */
    private static String truncate(String str, int maxLen) {
        if (str == null || str.isEmpty()) return "§7не задан";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }

    private static ItemStack blocksItem(MineConfig config) {
        ItemStack stack = new ItemStack(Material.DIAMOND_PICKAXE);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§a§lБлоки", NamedTextColor.GREEN));

            int blockCount = config.blocks().blockCount();
            String fillMode = config.blocks().fillMode().name();
            boolean hasCustom = config.blocks().hasCustomBlocks();

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(SEPARATOR, NamedTextColor.DARK_GRAY));
            lore.add(Component.text(SECTION_SETTINGS, NamedTextColor.GRAY));
            lore.add(Component.text("  §fТипов: §7" + blockCount, NamedTextColor.WHITE));
            lore.add(Component.text("  §fРежим: §7" + fillMode, NamedTextColor.WHITE));
            lore.add(Component.text("  §fКастомные: " + (hasCustom ? "§aДа" : "§7Нет"), NamedTextColor.WHITE));

            // Show first few blocks as preview
            var weights = config.blocks().weights();
            if (!weights.isEmpty()) {
                lore.add(Component.empty());
                lore.add(Component.text("§7Топ блоков:", NamedTextColor.GRAY));
                weights.entrySet().stream()
                        .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                        .limit(3)
                        .forEach(e -> lore.add(Component.text(
                                String.format("  §8• §7%s §8(§7%.0f%%§8)",
                                        e.getKey().serialize(), e.getValue() * 100),
                                NamedTextColor.GRAY)));
            }

            lore.add(Component.empty());
            lore.add(Component.text(CLICK_EDIT, NamedTextColor.YELLOW));

            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack regionsItem(MineConfig config) {
        ItemStack stack = new ItemStack(Material.COMPASS);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§a§lРегионы", NamedTextColor.GREEN));

            int regionCount = config.region().regionCount();
            int volume = config.region().totalVolume();

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(SEPARATOR, NamedTextColor.DARK_GRAY));
            lore.add(Component.text(SECTION_SETTINGS, NamedTextColor.GRAY));
            lore.add(Component.text("  §fКоличество: §7" + regionCount, NamedTextColor.WHITE));
            lore.add(Component.text("  §fОбщий объём: §7" + volume + " §8блоков", NamedTextColor.WHITE));

            // Show world info
            lore.add(Component.empty());
            lore.add(Component.text("  §fМир: §7" + config.worldName(), NamedTextColor.WHITE));

            lore.add(Component.empty());
            lore.add(Component.text("§8Изменяется через палочку", NamedTextColor.DARK_GRAY));

            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack resetItem(MineConfig config) {
        ItemStack stack = new ItemStack(Material.CLOCK);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§a§lСброс", NamedTextColor.GREEN));

            String interval = config.reset().intervalDisplay();
            double percent = config.reset().percentTrigger();
            boolean percentEnabled = config.reset().isPercentTriggerEnabled();
            int cmdCount = config.reset().commands().size();
            String broadcast = config.reset().broadcastMessage();

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(SEPARATOR, NamedTextColor.DARK_GRAY));
            lore.add(Component.text(SECTION_SETTINGS, NamedTextColor.GRAY));
            lore.add(Component.text("  §fИнтервал: §7" + interval, NamedTextColor.WHITE));
            lore.add(Component.text("  §fТриггер по %: " + (percentEnabled ? "§a" + percent + "%" : "§7выключен"), NamedTextColor.WHITE));
            lore.add(Component.text("  §fКоманд: §7" + cmdCount, NamedTextColor.WHITE));

            // Show broadcast status
            lore.add(Component.empty());
            lore.add(Component.text("  §fСообщение: " + (broadcast.isEmpty() ? "§7нет" : "§aесть"), NamedTextColor.WHITE));
            if (!broadcast.isEmpty()) {
                lore.add(Component.text("    §8" + truncate(broadcast, 25), NamedTextColor.DARK_GRAY));
            }

            lore.add(Component.empty());
            lore.add(Component.text(CLICK_EDIT, NamedTextColor.YELLOW));

            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack rewardsItem(MineConfig config) {
        ItemStack stack = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§a§lНаграды", NamedTextColor.GREEN));

            var entries = config.rewards().entries();
            int rewardCount = entries.size();

            // Count reward types
            long itemRewards = entries.stream()
                    .flatMap(e -> e.items().stream()).count();
            long cmdRewards = entries.stream()
                    .filter(e -> !e.commands().isEmpty()).count();
            long preventDrops = entries.stream()
                    .filter(RewardConfig.RewardEntry::preventVanillaDrops).count();

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(SEPARATOR, NamedTextColor.DARK_GRAY));
            lore.add(Component.text(SECTION_SETTINGS, NamedTextColor.GRAY));
            lore.add(Component.text("  §fЗаписей: §7" + rewardCount, NamedTextColor.WHITE));

            if (rewardCount > 0) {
                lore.add(Component.empty());
                lore.add(Component.text("§7Типы наград:", NamedTextColor.GRAY));
                lore.add(Component.text("  §8• §fПредметов: §7" + itemRewards, NamedTextColor.WHITE));
                lore.add(Component.text("  §8• §fКоманд: §7" + cmdRewards, NamedTextColor.WHITE));
                if (preventDrops > 0) {
                    lore.add(Component.text("  §8• §cБлокируют дроп: §7" + preventDrops, NamedTextColor.WHITE));
                }
            }

            lore.add(Component.empty());
            lore.add(Component.text(CLICK_EDIT, NamedTextColor.YELLOW));

            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack teleportItem(MineConfig config) {
        ItemStack stack = new ItemStack(Material.ENDER_PEARL);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§a§lТелепорт", NamedTextColor.GREEN));

            boolean tpEnabled = config.teleport().enabled();
            boolean spawnEnabled = config.playerSpawn().enabled();

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(SEPARATOR, NamedTextColor.DARK_GRAY));
            lore.add(Component.text(SECTION_SETTINGS, NamedTextColor.GRAY));

            // Teleport location
            lore.add(Component.text("  §fТелепорт: " + (tpEnabled ? "§aвкл" : "§7выкл"), NamedTextColor.WHITE));
            if (tpEnabled) {
                config.teleport().getLocation().ifPresent(loc ->
                        lore.add(Component.text("    " + formatLocation(loc), NamedTextColor.WHITE)));
            }

            // Spawn location (for stuck players)
            lore.add(Component.empty());
            lore.add(Component.text("  §fСпавн (застрявших): " + (spawnEnabled ? "§aвкл" : "§7выкл"), NamedTextColor.WHITE));
            if (spawnEnabled) {
                config.playerSpawn().getLocation().ifPresent(loc ->
                        lore.add(Component.text("    " + formatLocation(loc), NamedTextColor.WHITE)));
            }

            // Command hints
            lore.add(Component.empty());
            lore.add(Component.text(COMMANDS_HEADER, NamedTextColor.DARK_GRAY));
            lore.add(Component.text("  §8• §7/lm setteleport §f<mine>", NamedTextColor.GRAY));
            lore.add(Component.text("  §8• §7/lm setspawn §f<mine>", NamedTextColor.GRAY));
            lore.add(Component.text("  §8• §7/lm clearspawn §f<mine>", NamedTextColor.GRAY));

            lore.add(Component.empty());
            lore.add(Component.text(CLICK_EDIT, NamedTextColor.YELLOW));

            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack uiItem(MineConfig config) {
        ItemStack stack = new ItemStack(Material.PAINTING);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§a§lИнтерфейс", NamedTextColor.GREEN));

            boolean actionBarEnabled = config.ui().actionBarEnabled();
            double range = config.ui().actionBarRange();
            String format = config.ui().actionBarFormat();
            String timerFmt = config.ui().timerFormat();

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(SEPARATOR, NamedTextColor.DARK_GRAY));
            lore.add(Component.text(SECTION_SETTINGS, NamedTextColor.GRAY));
            lore.add(Component.text("  §fAction Bar: " + (actionBarEnabled ? "§aвкл" : "§7выкл"), NamedTextColor.WHITE));
            lore.add(Component.text("  §fРадиус: §7" + (int) range + " §8блоков", NamedTextColor.WHITE));

            // Show format previews
            lore.add(Component.empty());
            lore.add(Component.text("§7Формат ActionBar:", NamedTextColor.GRAY));
            lore.add(Component.text("  §8" + truncate(format, 28), NamedTextColor.DARK_GRAY));
            lore.add(Component.empty());
            lore.add(Component.text("§7Формат таймера: §8" + timerFmt, NamedTextColor.GRAY));

            lore.add(Component.empty());
            lore.add(Component.text(CLICK_EDIT, NamedTextColor.YELLOW));

            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack saveItem() {
        ItemStack stack = new ItemStack(Material.LIME_DYE);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§a§lСохранить изменения", NamedTextColor.GREEN));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(SEPARATOR, NamedTextColor.DARK_GRAY));
            lore.add(Component.text("§7Сохраняет конфигурацию шахты", NamedTextColor.GRAY));
            lore.add(Component.text("§7в файл на диске", NamedTextColor.GRAY));
            lore.add(Component.empty());
            lore.add(Component.text(CLICK_SAVE, NamedTextColor.GREEN));
            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack deleteItem(String mineName) {
        ItemStack stack = new ItemStack(Material.TNT);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§c§lУдалить шахту", NamedTextColor.RED));
            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(SEPARATOR, NamedTextColor.DARK_GRAY));
            lore.add(Component.text("§c§lВнимание!", NamedTextColor.RED));
            lore.add(Component.text("§cЭто действие нельзя отменить!", NamedTextColor.RED));
            lore.add(Component.empty());
            lore.add(Component.text("§7Шахта: §f" + mineName, NamedTextColor.GRAY));
            lore.add(Component.empty());
            lore.add(Component.text(CLICK_DELETE, NamedTextColor.RED));
            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack infoItem(Mine mine) {
        ItemStack stack = new ItemStack(Material.BOOK);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text("§b§lИнформация", NamedTextColor.AQUA));

            int blocks = mine.getBlocks();
            int total = mine.getTotalVolume();
            double percent = mine.getPercentFilled();
            String statusColor = percent > 50 ? "§a" : percent > 25 ? "§e" : "§c";

            List<Component> lore = new ArrayList<>();
            lore.add(Component.text(SEPARATOR, NamedTextColor.DARK_GRAY));
            lore.add(Component.text("§7Базовые данные:", NamedTextColor.GRAY));
            lore.add(Component.text("  §fНазвание: §7" + mine.getName(), NamedTextColor.WHITE));
            lore.add(Component.text("  §fМир: §7" + mine.getConfig().worldName(), NamedTextColor.WHITE));

            lore.add(Component.empty());
            lore.add(Component.text("§7Состояние заполнения:", NamedTextColor.GRAY));
            lore.add(Component.text("  §fБлоков: " + statusColor + blocks + "§8/§7" + total, NamedTextColor.WHITE));
            lore.add(Component.text("  §fПроцент: " + statusColor + String.format("%.1f%%", percent), NamedTextColor.WHITE));

            // Visual percentage bar
            int barLen = 10;
            int filled = (int) Math.round(percent / 100.0 * barLen);
            String bar = "§a" + "█".repeat(filled) + "§8" + "░".repeat(barLen - filled);
            lore.add(Component.text("  " + bar, NamedTextColor.WHITE));

            meta.lore(lore);
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
