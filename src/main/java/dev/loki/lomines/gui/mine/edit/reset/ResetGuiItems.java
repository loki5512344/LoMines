package dev.loki.lomines.gui.mine.edit.reset;

import dev.loki.lomines.data.config.reset.ResetConfig;
import dev.loki.lomines.gui.common.ItemStackFactory;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Item factory for ResetGui.
 */
final class ResetGuiItems {

    private ResetGuiItems() {}

    static ItemStack intervalItem(ResetConfig config) {
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

    static ItemStack percentTriggerItem(ResetConfig config) {
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

    static ItemStack messageItem(ResetConfig config) {
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

    static ItemStack commandsItem(ResetConfig config) {
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

    static ItemStack backItem() {
        return ItemStackFactory.create(Material.ARROW, "§c§lНазад",
            "§8───────────────",
            "§7Вернуться в редактор шахты",
            "",
            "§e▸ Нажмите для возврата"
        );
    }
}
