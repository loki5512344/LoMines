package dev.loki.lomines.gui.group;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.Mines;
import dev.loki.lomines.wand.GroupWandManager;
import dev.loki.lomines.wand.GroupWandSession;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * 6-row chest: left column = create; center 3×3 = nine sub-mines with shared settings.
 */
public final class GroupCreateGui {

    public static final int SLOT_CREATE = 0;
    public static final int SLOT_INFO = 8;
    private static final int SIZE = 54;
    private static final int[] MINE_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};

    private GroupCreateGui() {}

    public static void open(LoMinesPlugin plugin, Player player) {
        GroupCreateGuiHolder holder = new GroupCreateGuiHolder(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(holder, SIZE,
                Component.text("LoMines · группа (9 шахт)", NamedTextColor.DARK_GREEN));
        holder.setInventory(inv);
        fill(plugin, player, inv);
        player.openInventory(inv);
    }

    public static void refresh(LoMinesPlugin plugin, Player player, Inventory inv) {
        fill(plugin, player, inv);
    }

    private static void fill(LoMinesPlugin plugin, Player player, Inventory inv) {
        GroupWandSession session = plugin.getGroupWandManager().getSession(player.getUniqueId());
        for (int i = 0; i < SIZE; i++) {
            inv.setItem(i, GroupCreateItems.filler());
        }
        inv.setItem(SLOT_CREATE, GroupCreateItems.createButton());
        inv.setItem(SLOT_INFO, GroupCreateItems.infoPaper(session));
        for (int i = 0; i < MINE_SLOTS.length; i++) {
            inv.setItem(MINE_SLOTS[i], GroupCreateItems.mineSlotItem(session, i));
        }
    }

    public static int mineSlotIndex(int rawSlot) {
        for (int i = 0; i < MINE_SLOTS.length; i++) {
            if (MINE_SLOTS[i] == rawSlot) {
                return i;
            }
        }
        return -1;
    }

    public static boolean handleClick(LoMinesPlugin plugin, Player player, int rawSlot) {
        if (rawSlot < 0 || rawSlot >= SIZE) {
            return false;
        }
        GroupWandManager mgr = plugin.getGroupWandManager();
        GroupWandSession session = mgr.getSession(player.getUniqueId());

        if (rawSlot == SLOT_CREATE) {
            runCreate(plugin, player, session);
            player.closeInventory();
            return true;
        }

        int mineIdx = mineSlotIndex(rawSlot);
        if (mineIdx >= 0) {
            session.setActiveSlot(mineIdx);
            Inventory top = player.getOpenInventory().getTopInventory();
            if (top.getHolder() instanceof GroupCreateGuiHolder) {
                refresh(plugin, player, top);
            }
            player.sendMessage(Component.text("Активный слот палочки: " + (mineIdx + 1), NamedTextColor.YELLOW));
            return true;
        }
        return true;
    }

    private static void runCreate(LoMinesPlugin plugin, Player player, GroupWandSession session) {
        if (!session.hasBaseName()) {
            player.sendMessage(Component.text("Сначала: /lm group <префикс>", NamedTextColor.RED));
            return;
        }
        String base = session.getBaseName();
        if (!base.matches("^[a-zA-Z0-9_-]+$")) {
            player.sendMessage(Component.text("Префикс: только латиница, цифры, _ -", NamedTextColor.RED));
            return;
        }

        Mines mines = plugin.getMines();
        int created = 0;
        int skipped = 0;

        for (int i = 0; i < 9; i++) {
            if (!session.isSlotReady(i)) continue;
            String mineName = base + "_" + (i + 1);
            try {
                mines.create(mineName, session.getPos1(i), session.getPos2(i));
                created++;
            } catch (IllegalArgumentException e) {
                player.sendMessage(Component.text("Пропуск (уже есть): " + mineName, NamedTextColor.GOLD));
                skipped++;
            } catch (Exception e) {
                player.sendMessage(Component.text("Ошибка " + mineName + ": " + e.getMessage(), NamedTextColor.RED));
                plugin.loLogger().error("Group create failed for " + mineName, e);
            }
        }

        if (created == 0) {
            player.sendMessage(Component.text("Нет готовых слотов (2 точки в каждом).", NamedTextColor.RED));
            return;
        }
        player.sendMessage(Component.text(
                "Создано шахт: " + created + (skipped > 0 ? " (пропущено: " + skipped + ")" : ""),
                NamedTextColor.GREEN));
    }
}
