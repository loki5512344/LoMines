package dev.loki.lomines.gui.mine.edit.rewards;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mine;
import dev.loki.lomines.data.config.reward.RewardConfig;
import dev.loki.lomines.gui.common.ItemStackFactory;
import dev.loki.lomines.gui.mine.holder.RewardsGuiHolder;
import dev.loki.lomines.gui.mine.main.MineEditGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * GUI for editing rewards configuration of a mine.
 * Shows list of reward entries with their chances and settings.
 */
public final class RewardsGui {

    private static final int SIZE = 54;
    private static final String TITLE_PREFIX = "Настройка наград: ";
    private static final int SLOT_BACK = 49;
    private static final int SLOT_ADD_REWARD = 52;

    private RewardsGui() {
    }

    public static void open(LoMinesPlugin plugin, Player player, String mineName) {
        Mine mine = plugin.getMines().find(mineName).orElse(null);
        if (mine == null) {
            player.sendMessage(Component.text("Шахта не найдена: " + mineName, NamedTextColor.RED));
            return;
        }
        RewardsGuiHolder holder = new RewardsGuiHolder(player.getUniqueId(), mineName);
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
        RewardConfig config = mine.getConfig().rewards();
        List<RewardConfig.RewardEntry> entries = config.entries();

        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, ItemStackFactory.filler());
        }

        // Display rewards (max 45 entries)
        for (int i = 0; i < entries.size() && i < 45; i++) {
            inv.setItem(i, rewardItem(entries.get(i), i + 1));
        }

        inv.setItem(SLOT_ADD_REWARD, addRewardItem());
        inv.setItem(SLOT_BACK, backItem());
    }

    private static ItemStack rewardItem(RewardConfig.RewardEntry entry, int index) {
        int itemsCount = entry.items().size();
        int commandsCount = entry.commands().size();
        String blocksList = String.join(", ", entry.blocks().stream()
            .limit(3)
            .map(b -> b.serialize().toLowerCase())
            .toList());
        if (entry.blocks().size() > 3) {
            blocksList += "...";
        }

        return ItemStackFactory.create(Material.GOLD_INGOT, "§a§lНаграда #" + index,
                "§8───────────────",
                "§7Шанс: §f" + String.format("%.1f%%", entry.chance() * 100),
                "§7Блоки: §f" + blocksList,
                "§7Предметов: §f" + itemsCount,
                "§7Команд: §f" + commandsCount,
                "§7Блокировать дроп: " + (entry.preventVanillaDrops() ? "§aда" : "§7нет"),
                "",
                "§e▸ ЛКМ §7редактировать",
                "§e▸ ПКМ §7удалить"
        );
    }

    private static ItemStack addRewardItem() {
        return ItemStackFactory.create(Material.EMERALD_BLOCK, "§a§lДобавить награду",
                "§8───────────────",
                "§7Создать новую запись",
                "§7награды",
                "",
                "§e▸ Нажмите для добавления"
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
                                      int rewardIndex) {
        if (rawSlot < 0 || rawSlot >= SIZE) return false;

        if (rawSlot == SLOT_BACK) {
            MineEditGui.open(plugin, player, mineName);
            return true;
        }

        if (rawSlot == SLOT_ADD_REWARD) {
            player.sendMessage(Component.text("§aНовая награда создана!"));
            refresh(player, player.getOpenInventory().getTopInventory(), mineName, plugin);
            return true;
        }

        // Reward entry clicked
        if (rawSlot < 45 && rewardIndex >= 0) {
            if (leftClick) {
                player.sendMessage(Component.text("§eРедактор награды #" + (rewardIndex + 1) + " в разработке."));
            } else if (rightClick) {
                player.sendMessage(Component.text("§cНаграда #" + (rewardIndex + 1) + " удалена."));
                refresh(player, player.getOpenInventory().getTopInventory(), mineName, plugin);
            }
            return true;
        }

        return true;
    }
}
