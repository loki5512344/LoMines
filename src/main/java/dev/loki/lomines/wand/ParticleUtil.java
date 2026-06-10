package dev.loki.lomines.wand;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Utility class for spawning particles to visualize wand selections.
 */
final class ParticleUtil {

    private ParticleUtil() {
    }

    static void showCornerParticles(Player player, Location loc, Color color) {
        World world = loc.getWorld();
        if (world == null) return;

        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);

        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            double x = loc.getX() + 0.5 + Math.cos(angle) * 0.5;
            double z = loc.getZ() + 0.5 + Math.sin(angle) * 0.5;
            double y = loc.getY() + 0.5;

            Location particleLoc = new Location(world, x, y, z);
            player.spawnParticle(Particle.DUST, particleLoc, 1, dustOptions);
        }
    }

    static void showEdgeParticles(Player player, Location pos1, Location pos2, Color color) {
        World world = pos1.getWorld();
        if (world == null) return;

        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 0.8f);

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        double step = 1.0;

        // Bottom face edges
        spawnEdgeParticles(player, world, minX, minY, minZ, maxX, minY, minZ, step, dustOptions);
        spawnEdgeParticles(player, world, minX, minY, maxZ, maxX, minY, maxZ, step, dustOptions);
        spawnEdgeParticles(player, world, minX, minY, minZ, minX, minY, maxZ, step, dustOptions);
        spawnEdgeParticles(player, world, maxX, minY, minZ, maxX, minY, maxZ, step, dustOptions);

        // Top face edges
        spawnEdgeParticles(player, world, minX, maxY, minZ, maxX, maxY, minZ, step, dustOptions);
        spawnEdgeParticles(player, world, minX, maxY, maxZ, maxX, maxY, maxZ, step, dustOptions);
        spawnEdgeParticles(player, world, minX, maxY, minZ, minX, maxY, maxZ, step, dustOptions);
        spawnEdgeParticles(player, world, maxX, maxY, minZ, maxX, maxY, maxZ, step, dustOptions);

        // Vertical edges
        spawnEdgeParticles(player, world, minX, minY, minZ, minX, maxY, minZ, step, dustOptions);
        spawnEdgeParticles(player, world, maxX, minY, minZ, maxX, maxY, minZ, step, dustOptions);
        spawnEdgeParticles(player, world, minX, minY, maxZ, minX, maxY, maxZ, step, dustOptions);
        spawnEdgeParticles(player, world, maxX, minY, maxZ, maxX, maxY, maxZ, step, dustOptions);
    }

    private static void spawnEdgeParticles(Player player, World world,
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

    static void showOutlineParticles(Player player, Location pos1, Location pos2, Color color, int density) {
        World world = pos1.getWorld();
        if (world == null || !world.equals(player.getWorld())) return;

        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 0.5f);

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

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
