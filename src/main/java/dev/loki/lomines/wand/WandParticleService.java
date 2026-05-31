package dev.loki.lomines.wand;

import dev.loki.lomines.LoMinesPlugin;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
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

    private final LoMinesPlugin plugin;
    private final Map<UUID, BukkitRunnable> activeTasks = new ConcurrentHashMap<>();

    // Particle colors for different states
    private static final Color COLOR_POS1 = Color.LIME;      // Green for corner 1
    private static final Color COLOR_POS2 = Color.AQUA;      // Cyan for corner 2
    private static final Color COLOR_EDGE = Color.YELLOW;    // Yellow for edges
    private static final Color COLOR_READY = Color.GREEN;    // Green when both corners set

    public WandParticleService(LoMinesPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Starts showing particles for a player holding the wand.
     */
    public void startShowingParticles(Player player) {
        UUID playerId = player.getUniqueId();

        // Stop existing task if any
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

        task.runTaskTimer(plugin, 0L, 10L); // Every 10 ticks (0.5 seconds)
        activeTasks.put(playerId, task);
    }

    /**
     * Stops showing particles for a player.
     */
    public void stopShowingParticles(UUID playerId) {
        BukkitRunnable task = activeTasks.remove(playerId);
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Stops all particle tasks.
     */
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

        // Show particles for corner 1 if set
        if (pos1 != null && pos1.getWorld() != null) {
            showCornerParticles(player, pos1, COLOR_POS1);
        }

        // Show particles for corner 2 if set
        if (pos2 != null && pos2.getWorld() != null) {
            showCornerParticles(player, pos2, COLOR_POS2);
        }

        // Show edge particles if both corners are set
        if (pos1 != null && pos2 != null && pos1.getWorld() != null && pos2.getWorld() != null) {
            if (pos1.getWorld().equals(pos2.getWorld())) {
                showEdgeParticles(player, pos1, pos2, session.isSlotReady(activeSlot) ? COLOR_READY : COLOR_EDGE);
            }
        }

        // Also show other slots that are ready (with lower intensity)
        for (int i = 0; i < 9; i++) {
            if (i == activeSlot) continue;
            if (session.isSlotReady(i)) {
                Location p1 = session.getPos1(i);
                Location p2 = session.getPos2(i);
                if (p1 != null && p2 != null && p1.getWorld() != null && p1.getWorld().equals(player.getWorld())) {
                    showOutlineParticles(player, p1, p2, Color.GRAY, 5);
                }
            }
        }
    }

    private void showCornerParticles(Player player, Location loc, Color color) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);

        // Show particles in a small sphere around the corner
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            double x = loc.getX() + 0.5 + Math.cos(angle) * 0.5;
            double z = loc.getZ() + 0.5 + Math.sin(angle) * 0.5;
            double y = loc.getY() + 0.5;

            Location particleLoc = new Location(world, x, y, z);
            player.spawnParticle(Particle.DUST, particleLoc, 1, dustOptions);
        }
    }

    private void showEdgeParticles(Player player, Location pos1, Location pos2, Color color) {
        World world = pos1.getWorld();
        if (world == null) return;

        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 0.8f);

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        // Show particles along the edges of the cuboid
        double step = 1.0; // Every block

        // Bottom face edges
        spawnEdgeParticles(player, world, minX, minY, minZ, maxX, minY, minZ, step, dustOptions); // Bottom front
        spawnEdgeParticles(player, world, minX, minY, maxZ, maxX, minY, maxZ, step, dustOptions); // Bottom back
        spawnEdgeParticles(player, world, minX, minY, minZ, minX, minY, maxZ, step, dustOptions); // Bottom left
        spawnEdgeParticles(player, world, maxX, minY, minZ, maxX, minY, maxZ, step, dustOptions); // Bottom right

        // Top face edges
        spawnEdgeParticles(player, world, minX, maxY, minZ, maxX, maxY, minZ, step, dustOptions); // Top front
        spawnEdgeParticles(player, world, minX, maxY, maxZ, maxX, maxY, maxZ, step, dustOptions); // Top back
        spawnEdgeParticles(player, world, minX, maxY, minZ, minX, maxY, maxZ, step, dustOptions); // Top left
        spawnEdgeParticles(player, world, maxX, maxY, minZ, maxX, maxY, maxZ, step, dustOptions); // Top right

        // Vertical edges
        spawnEdgeParticles(player, world, minX, minY, minZ, minX, maxY, minZ, step, dustOptions); // Front left
        spawnEdgeParticles(player, world, maxX, minY, minZ, maxX, maxY, minZ, step, dustOptions); // Front right
        spawnEdgeParticles(player, world, minX, minY, maxZ, minX, maxY, maxZ, step, dustOptions); // Back left
        spawnEdgeParticles(player, world, maxX, minY, maxZ, maxX, maxY, maxZ, step, dustOptions); // Back right
    }

    private void spawnEdgeParticles(Player player, World world,
                                    double x1, double y1, double z1,
                                    double x2, double y2, double z2,
                                    double step, Particle.DustOptions dustOptions) {
        double distance = Math.sqrt(
                Math.pow(x2 - x1, 2) +
                Math.pow(y2 - y1, 2) +
                Math.pow(z2 - z1, 2)
        );

        int count = (int) (distance / step) + 1;

        for (int i = 0; i <= count; i++) {
            double t = (double) i / count;
            double x = x1 + (x2 - x1) * t + 0.5;
            double y = y1 + (y2 - y1) * t + 0.5;
            double z = z1 + (z2 - z1) * t + 0.5;

            Location loc = new Location(world, x, y, z);
            if (loc.getWorld() != null && loc.getWorld().equals(player.getWorld())) {
                player.spawnParticle(Particle.DUST, loc, 1, dustOptions);
            }
        }
    }

    private void showOutlineParticles(Player player, Location pos1, Location pos2, Color color, int density) {
        World world = pos1.getWorld();
        if (world == null || !world.equals(player.getWorld())) return;

        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 0.5f);

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        // Just show corners for non-active slots (lighter)
        Location[] corners = {
                new Location(world, minX + 0.5, minY + 0.5, minZ + 0.5),
                new Location(world, maxX + 0.5, minY + 0.5, minZ + 0.5),
                new Location(world, minX + 0.5, minY + 0.5, maxZ + 0.5),
                new Location(world, maxX + 0.5, minY + 0.5, maxZ + 0.5),
                new Location(world, minX + 0.5, maxY + 0.5, minZ + 0.5),
                new Location(world, maxX + 0.5, maxY + 0.5, minZ + 0.5),
                new Location(world, minX + 0.5, maxY + 0.5, maxZ + 0.5),
                new Location(world, maxX + 0.5, maxY + 0.5, maxZ + 0.5)
        };

        for (Location corner : corners) {
            player.spawnParticle(Particle.DUST, corner, 1, dustOptions);
        }
    }
}
