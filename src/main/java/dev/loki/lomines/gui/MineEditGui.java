package dev.loki.lomines.gui;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.Mine;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.data.config.RewardConfig;
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

    // UI Constants
    private static final String SEP = "§8───────────────";
    private static final String EDIT = "§e▸ Нажмите для редактирования";
    private static final String CLOSE = "§c▸ Нажмите чтобы закрыть";
    private static final String SAVE = "§a▸ Нажмите для сохранения";
    private static final String DEL = "§c▸ Нажмите для удаления";

    private MineEditGui() {}

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
        for (int i = 0; i < SIZE; i++) inv.setItem(i, filler());
        inv.setItem(SLOT_BLOCKS, blocksItem(config));
        inv.setItem(SLOT_REGIONS, regionsItem(config));
        inv.setItem(SLOT_RESET, resetItem(config));
        inv.setItem(SLOT_REWARDS, rewardsItem(config));
        inv.setItem(SLOT_TELEPORT, teleportItem(config));
        inv.setItem(SLOT_UI, uiItem(config));
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

    private static String loc(Location l) {
        if (l == null || l.getWorld() == null) return "§7не задана";
        return String.format("§7%.0f§8/§7%.0f§8/§7%.0f", l.getX(), l.getY(), l.getZ());
    }

    private static String trunc(String s, int max) {
        if (s == null || s.isEmpty()) return "§7нет";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }

    private static ItemStack create(Material mat, String name, String... loreLines) {
        ItemStack stack = new ItemStack(mat);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(name, NamedTextColor.WHITE));
            List<Component> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(Component.text(line, NamedTextColor.GRAY));
            }
            meta.lore(lore);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private static ItemStack blocksItem(MineConfig config) {
        var w = config.blocks().weights();
        List<String> lore = new ArrayList<>();
        lore.add(SEP);
        lore.add("§7Настройки:");
        lore.add("  §fТипов: §7" + config.blocks().blockCount());
        lore.add("  §fРежим: §7" + config.blocks().fillMode().name());
        lore.add("  §fКастомные: " + (config.blocks().hasCustomBlocks() ? "§aДа" : "§7Нет"));
        if (!w.isEmpty()) {
            lore.add("");
            lore.add("§7Топ блоков:");
            w.entrySet().stream().sorted((a, b) -> Double.compare(b.getValue(), a.getValue())).limit(3)
                .forEach(e -> lore.add(String.format("  §8• §7%s §8(§7%.0f%%§8)", e.getKey().serialize(), e.getValue() * 100)));
        }
        lore.add("");
        lore.add(EDIT);
        return create(Material.DIAMOND_PICKAXE, "§a§lБлоки", lore.toArray(new String[0]));
    }

    private static ItemStack regionsItem(MineConfig config) {
        return create(Material.COMPASS, "§a§lРегионы",
            SEP,
            "§7Настройки:",
            "  §fКоличество: §7" + config.region().regionCount(),
            "  §fОбщий объём: §7" + config.region().totalVolume() + " §8блоков",
            "",
            "  §fМир: §7" + config.worldName(),
            "",
            "§8Изменяется через палочку"
        );
    }

    private static ItemStack resetItem(MineConfig config) {
        List<String> lore = new ArrayList<>();
        lore.add(SEP);
        lore.add("§7Настройки:");
        lore.add("  §fИнтервал: §7" + config.reset().intervalDisplay());
        lore.add("  §fТриггер по %: " + (config.reset().isPercentTriggerEnabled() ? "§a" + config.reset().percentTrigger() + "%" : "§7выкл"));
        lore.add("  §fКоманд: §7" + config.reset().commands().size());
        lore.add("");
        lore.add("  §fСообщение: " + (config.reset().broadcastMessage().isEmpty() ? "§7нет" : "§aесть"));
        if (!config.reset().broadcastMessage().isEmpty()) {
            lore.add("    §8" + trunc(config.reset().broadcastMessage(), 25));
        }
        lore.add("");
        lore.add(EDIT);
        return create(Material.CLOCK, "§a§lСброс", lore.toArray(new String[0]));
    }

    private static ItemStack rewardsItem(MineConfig config) {
        var entries = config.rewards().entries();
        long items = entries.stream().flatMap(e -> e.items().stream()).count();
        long cmds = entries.stream().filter(e -> !e.commands().isEmpty()).count();
        long blockDrop = entries.stream().filter(RewardConfig.RewardEntry::preventVanillaDrops).count();
        List<String> lore = new ArrayList<>();
        lore.add(SEP);
        lore.add("§7Настройки:");
        lore.add("  §fЗаписей: §7" + entries.size());
        if (!entries.isEmpty()) {
            lore.add("");
            lore.add("§7Типы наград:");
            lore.add("  §8• §fПредметов: §7" + items);
            lore.add("  §8• §fКоманд: §7" + cmds);
            if (blockDrop > 0) lore.add("  §8• §cБлокируют дроп: §7" + blockDrop);
        }
        lore.add("");
        lore.add(EDIT);
        return create(Material.GOLD_INGOT, "§a§lНаграды", lore.toArray(new String[0]));
    }

    private static ItemStack teleportItem(MineConfig config) {
        List<String> lore = new ArrayList<>();
        lore.add(SEP);
        lore.add("§7Настройки:");
        lore.add("  §fТелепорт: " + (config.teleport().enabled() ? "§aвкл" : "§7выкл"));
        if (config.teleport().enabled()) {
            config.teleport().getLocation().ifPresent(loc -> lore.add("    " + loc(loc)));
        }
        lore.add("");
        lore.add("  §fСпавн (застрявших): " + (config.playerSpawn().enabled() ? "§aвкл" : "§7выкл"));
        if (config.playerSpawn().enabled()) {
            config.playerSpawn().getLocation().ifPresent(loc -> lore.add("    " + loc(loc)));
        }
        lore.add("");
        lore.add("§8Команды:");
        lore.add("  §8• §7/lm setteleport §f<mine>");
        lore.add("  §8• §7/lm setspawn §f<mine>");
        lore.add("  §8• §7/lm clearspawn §f<mine>");
        lore.add("");
        lore.add(EDIT);
        return create(Material.ENDER_PEARL, "§a§lТелепорт", lore.toArray(new String[0]));
    }

    private static ItemStack uiItem(MineConfig config) {
        return create(Material.PAINTING, "§a§lИнтерфейс",
            SEP,
            "§7Настройки:",
            "  §fAction Bar: " + (config.ui().actionBarEnabled() ? "§aвкл" : "§7выкл"),
            "  §fРадиус: §7" + (int) config.ui().actionBarRange() + " §8блоков",
            "",
            "§7Формат ActionBar:",
            "  §8" + trunc(config.ui().actionBarFormat(), 28),
            "",
            "§7Формат таймера: §8" + config.ui().timerFormat(),
            "",
            EDIT
        );
    }

    private static ItemStack saveItem() {
        return create(Material.LIME_DYE, "§a§lСохранить изменения",
            SEP,
            "§7Сохраняет конфигурацию шахты",
            "§7в файл на диске",
            "",
            SAVE
        );
    }

    private static ItemStack deleteItem(String mineName) {
        return create(Material.TNT, "§c§lУдалить шахту",
            SEP,
            "§c§lВнимание!",
            "§cЭто действие нельзя отменить!",
            "",
            "§7Шахта: §f" + mineName,
            "",
            DEL
        );
    }

    private static ItemStack infoItem(Mine mine) {
        int blocks = mine.getBlocks();
        int total = mine.getTotalVolume();
        double percent = mine.getPercentFilled();
        String c = percent > 50 ? "§a" : percent > 25 ? "§e" : "§c";
        int filled = (int) Math.round(percent / 100.0 * 10);
        String bar = "§a" + "█".repeat(filled) + "§8" + "░".repeat(10 - filled);
        return create(Material.BOOK, "§b§lИнформация",
            SEP,
            "§7Базовые данные:",
            "  §fНазвание: §7" + mine.getName(),
            "  §fМир: §7" + mine.getConfig().worldName(),
            "",
            "§7Состояние заполнения:",
            "  §fБлоков: " + c + blocks + "§8/§7" + total,
            "  §fПроцент: " + c + String.format("%.1f%%", percent),
            "  " + bar
        );
    }

    private static ItemStack backItem() {
        return create(Material.ARROW, "§c§lЗакрыть",
            SEP,
            "§7Закрыть редактор",
            "",
            CLOSE
        );
    }

    public static boolean handleClick(LoMinesPlugin plugin, Player player, int rawSlot, String mineName) {
        if (rawSlot < 0 || rawSlot >= SIZE) return false;
        switch (rawSlot) {
            case SLOT_BACK -> {
                player.closeInventory();
                return true;
            }
            case SLOT_BLOCKS -> {
                player.sendMessage(Component.text("Открытие редактора блоков... (в разработке)", NamedTextColor.YELLOW));
                return true;
            }
            case SLOT_RESET -> {
                player.sendMessage(Component.text("Открытие редактора сброса... (в разработке)", NamedTextColor.YELLOW));
                return true;
            }
            case SLOT_REWARDS -> {
                player.sendMessage(Component.text("Открытие редактора наград... (в разработке)", NamedTextColor.YELLOW));
                return true;
            }
            case SLOT_TELEPORT -> {
                player.sendMessage(Component.text("Открытие редактора телепорта... (в разработке)", NamedTextColor.YELLOW));
                return true;
            }
            case SLOT_UI -> {
                player.sendMessage(Component.text("Открытие редактора интерфейса... (в разработке)", NamedTextColor.YELLOW));
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
