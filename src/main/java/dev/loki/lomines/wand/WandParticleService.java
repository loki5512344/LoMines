package dev.loki.lomines.wand;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.wand.group.GroupWandItem;
import dev.loki.lomines.wand.group.GroupWandSession;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shows particles around wand selection areas to visualize the selected region.
 * Particles are shown only to the player holding the wand.
 */
public final class WandParticleService {

    private static final Color COLOR_POS1 = Color.LIME;
    private static final Color COLOR_POS2 = Color.AQUA;
    private static final Color COLOR_EDGE = Color.YELLOW;
    private static final Color COLOR_READY = Color.GREEN;
    private final LoMinesPlugin plugin;
    private final Map<UUID, BukkitRunnable> activeTasks = new ConcurrentHashMap<>();

    public WandParticleService(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    public void startShowingParticles(Player player) {
        UUID playerId = player.getUniqueId();
        stopShowingParticles(playerId);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !isHoldingWand(player)) {
                    cancel();
                    activeTasks.remove(playerId);
                    return;
                }
                showParticles(player);
            }
        };

        task.runTaskTimer(plugin, 0L, 10L);
        activeTasks.put(playerId, task);
    }

    public void stopShowingParticles(UUID playerId) {
        BukkitRunnable task = activeTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    public void stopAll() {
        for (BukkitRunnable task : activeTasks.values()) {
            task.cancel();
        }
        activeTasks.clear();
    }

    private boolean isHoldingWand(Player player) {
        return GroupWandItem.isWand(plugin, player.getInventory().getItemInMainHand());
    }

    private void showParticles(Player player) {
        GroupWandSession session = plugin.getGroupWandManager().getSession(player.getUniqueId());
        int activeSlot = session.getActiveSlot();

        Location pos1 = session.getPos1(activeSlot);
        Location pos2 = session.getPos2(activeSlot);

        if (pos1 != null && pos1.getWorld() != null) {
            ParticleUtil.showCornerParticles(player, pos1, COLOR_POS1);
        }

        if (pos2 != null && pos2.getWorld() != null) {
            ParticleUtil.showCornerParticles(player, pos2, COLOR_POS2);
        }

        if (pos1 != null && pos2 != null && pos1.getWorld() != null && pos2.getWorld() != null) {
            if (pos1.getWorld().equals(pos2.getWorld())) {
                Color edgeColor = session.isSlotReady(activeSlot) ? COLOR_READY : COLOR_EDGE;
                ParticleUtil.showEdgeParticles(player, pos1, pos2, edgeColor);
            }
        }

        showOtherSlots(player, session, activeSlot);
    }

    private void showOtherSlots(Player player, GroupWandSession session, int activeSlot) {
        for (int i = 0; i < 9; i++) {
            if (i == activeSlot) {
                continue;
            }
            if (session.isSlotReady(i)) {
                Location p1 = session.getPos1(i);
                Location p2 = session.getPos2(i);
                if (p1 != null && p2 != null && p1.getWorld() != null && p1.getWorld().equals(player.getWorld())) {
                    ParticleUtil.showOutlineParticles(player, p1, p2, Color.GRAY, 5);
                }
            }
        }
    }
}
