package dev.loki.lomines.gui.mine.main;

import dev.loki.lomines.data.config.model.MineConfig;
import dev.loki.lomines.data.config.reward.RewardConfig;
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

    private MineEditItems() {
    }



    static ItemStack filler() {
        return ItemStackFactory.filler();
    }

    static ItemStack blocksItem(MineConfig config) {
        List<String> lore = new ArrayList<>();
        lore.add(SEP);
        lore.add("§7Настройки:");
        lore.add("  §fТипов: §7" + config.blocks().blockCount());
        lore.add("  §fРежим: §7" + config.blocks().fillMode().name());
        lore.add("  §fКастомные: " + (config.blocks().hasCustomBlocks() ? "§aДа" : "§7Нет"));
        var w = config.blocks().weights();
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
        final var entries = config.rewards().entries();
        final long items = entries.stream().flatMap(e -> e.items().stream()).count();
        final long cmds = entries.stream().filter(e -> !e.commands().isEmpty()).count();
        final long blockDrop = entries.stream().filter(RewardConfig.RewardEntry::preventVanillaDrops).count();
        List<String> lore = new ArrayList<>();
        lore.add(SEP);
        lore.add("§7Настройки:");
        lore.add("  §fЗаписей: §7" + entries.size());
        if (!entries.isEmpty()) {
            lore.add("");
            lore.add("§7Типы наград:");
            lore.add("  §8• §fПредметов: §7" + items);
            lore.add("  §8• §fКоманд: §7" + cmds);
            if (blockDrop > 0) {
                lore.add("  §8• §cБлокируют дроп: §7" + blockDrop);
            }
        }
        lore.add("");
        lore.add(EDIT);
        return ItemStackFactory.create(Material.GOLD_INGOT, "§a§lНаграды", lore.toArray(new String[0]));
    }

}
