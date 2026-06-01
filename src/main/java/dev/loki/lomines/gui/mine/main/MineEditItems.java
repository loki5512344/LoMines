package dev.loki.lomines.gui.mine.main;

import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.data.config.MineConfig;
import dev.loki.lomines.data.config.RewardConfig;
import dev.loki.lomines.gui.common.ItemStackFactory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Item factory for MineEditGui. Creates all inventory items.
 */
final class MineEditItems {

    private static final String SEP = "§8───────────────";
    private static final String EDIT = "§e▸ Нажмите для редактирования";
    private static final String CLOSE = "§c▸ Нажмите чтобы закрыть";
    private static final String SAVE = "§a▸ Нажмите для сохранения";
    private static final String DEL = "§c▸ Нажмите для удаления";

    private MineEditItems() {}

    static ItemStack filler() {
        return ItemStackFactory.filler();
    }

    static ItemStack blocksItem(MineConfig config) {
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
        return ItemStackFactory.create(Material.DIAMOND_PICKAXE, "§a§lБлоки", lore.toArray(new String[0]));
    }

    static ItemStack regionsItem(MineConfig config) {
        return ItemStackFactory.create(Material.COMPASS, "§a§lРегионы",
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

    static ItemStack resetItem(MineConfig config) {
        List<String> lore = new ArrayList<>();
        lore.add(SEP);
        lore.add("§7Настройки:");
        lore.add("  §fИнтервал: §7" + config.reset().intervalDisplay());
        lore.add("  §fТриггер по %: " + (config.reset().isPercentTriggerEnabled() ? "§a" + config.reset().percentTrigger() + "%" : "§7выкл"));
        lore.add("  §fКоманд: §7" + config.reset().commands().size());
        lore.add("");
        lore.add("  §fСообщение: " + (config.reset().broadcastMessage().isEmpty() ? "§7нет" : "§aесть"));
        if (!config.reset().broadcastMessage().isEmpty()) {
            lore.add("    §8" + ItemStackFactory.trunc(config.reset().broadcastMessage(), 25));
        }
        lore.add("");
        lore.add(EDIT);
        return ItemStackFactory.create(Material.CLOCK, "§a§lСброс", lore.toArray(new String[0]));
    }

    static ItemStack rewardsItem(MineConfig config) {
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
        return ItemStackFactory.create(Material.GOLD_INGOT, "§a§lНаграды", lore.toArray(new String[0]));
    }

    static ItemStack teleportItem(MineConfig config) {
        List<String> lore = new ArrayList<>();
        lore.add(SEP);
        lore.add("§7Настройки:");
        lore.add("  §fТелепорт: " + (config.teleport().enabled() ? "§aвкл" : "§7выкл"));
        if (config.teleport().enabled()) {
            config.teleport().getLocation().ifPresent(loc -> lore.add("    " + ItemStackFactory.loc(loc)));
        }
        lore.add("");
        lore.add("  §fСпавн (застрявших): " + (config.playerSpawn().enabled() ? "§aвкл" : "§7выкл"));
        if (config.playerSpawn().enabled()) {
            config.playerSpawn().getLocation().ifPresent(loc -> lore.add("    " + ItemStackFactory.loc(loc)));
        }
        lore.add("");
        lore.add("§8Команды:");
        lore.add("  §8• §7/lm setteleport §f<mine>");
        lore.add("  §8• §7/lm setspawn §f<mine>");
        lore.add("  §8• §7/lm clearspawn §f<mine>");
        lore.add("");
        lore.add(EDIT);
        return ItemStackFactory.create(Material.ENDER_PEARL, "§a§lТелепорт", lore.toArray(new String[0]));
    }

    static ItemStack uiItem(MineConfig config) {
        return ItemStackFactory.create(Material.PAINTING, "§a§lИнтерфейс",
            SEP,
            "§7Настройки:",
            "  §fAction Bar: " + (config.ui().actionBarEnabled() ? "§aвкл" : "§7выкл"),
            "  §fРадиус: §7" + (int) config.ui().actionBarRange() + " §8блоков",
            "",
            "§7Формат ActionBar:",
            "  §8" + ItemStackFactory.trunc(config.ui().actionBarFormat(), 28),
            "",
            "§7Формат таймера: §8" + config.ui().timerFormat(),
            "",
            EDIT
        );
    }

    static ItemStack saveItem() {
        return ItemStackFactory.create(Material.LIME_DYE, "§a§lСохранить изменения",
            SEP,
            "§7Сохраняет конфигурацию шахты",
            "§7в файл на диске",
            "",
            SAVE
        );
    }

    static ItemStack deleteItem(String mineName) {
        return ItemStackFactory.create(Material.TNT, "§c§lУдалить шахту",
            SEP,
            "§c§lВнимание!",
            "§cЭто действие нельзя отменить!",
            "",
            "§7Шахта: §f" + mineName,
            "",
            DEL
        );
    }

    static ItemStack infoItem(Mine mine) {
        int blocks = mine.getBlocks();
        int total = mine.getTotalVolume();
        double percent = mine.getPercentFilled();
        String c = percent > 50 ? "§a" : percent > 25 ? "§e" : "§c";
        int filled = (int) Math.round(percent / 100.0 * 10);
        String bar = "§a" + "█".repeat(filled) + "§8" + "░".repeat(10 - filled);
        return ItemStackFactory.create(Material.BOOK, "§b§lИнформация",
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

    static ItemStack backItem() {
        return ItemStackFactory.create(Material.ARROW, "§c§lЗакрыть",
            SEP,
            "§7Закрыть редактор",
            "",
            CLOSE
        );
    }
}
