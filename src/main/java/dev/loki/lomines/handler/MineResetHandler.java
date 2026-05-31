package dev.loki.lomines.handler;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.Mine;
import dev.loki.lomines.data.config.block.FillMode;
import dev.loki.lomines.util.block.BlockUpdateUtil;
import dev.loki.lomines.util.location.Cuboid;
import dev.loki.lomines.util.location.LocationParser;
import dev.lolib.scheduler.Scheduler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles mine reset operations.
 * Ensures thread-safe reset execution and prevents concurrent resets.
 *
 * <p>Reset process:</p>
 * <ol>
 *   <li>Check if reset is already running (atomic check)</li>
 *   <li>Fill all regions asynchronously via BlockSetter</li>
 *   <li>Wait for all regions to complete</li>
 *   <li>Execute post-reset actions in main thread</li>
 * </ol>
 *
 * <p>Updated for new configuration system (v2).</p>
 */
public final class MineResetHandler {

    private final Mine mine;
    private final LoMinesPlugin plugin;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public MineResetHandler(Mine mine, LoMinesPlugin plugin) {
        this.mine = mine;
        this.plugin = plugin;
    }

    /**
     * Resets the mine by filling all regions with blocks.
     * If a reset is already in progress, this call is ignored.
     *
     * @param silent If true, suppresses broadcast messages
     */
    public void reset(boolean silent) {
        if (!running.compareAndSet(false, true)) {
            return;
        }

        try {
            resetAllRegions(silent);
        } catch (Exception e) {
            running.set(false);
            plugin.loLogger().error("Failed to reset mine " + mine.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fills all regions asynchronously and coordinates completion.
     */
    private void resetAllRegions(boolean silent) {
        List<Cuboid> regions = mine.getRegions();
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger totalBlocks = new AtomicInteger(0);

        if (mine.getBlockSetter() == null) {
            plugin.loLogger().warn("BlockSetter not initialized for mine " + mine.getName());
            running.set(false);
            return;
        }

        // Handle MASK fill mode
        if (mine.getConfig().blocks().fillMode() == FillMode.MASK) {
            List<Location> maskPositions = getMaskPositions();
            if (maskPositions.isEmpty()) {
                plugin.loLogger().warn("No mask positions found for mine " + mine.getName());
                running.set(false);
                return;
            }
            mine.getBlockSetter().fillAtLocations(maskPositions, placed ->
                    Scheduler.get(plugin).run(() -> onResetComplete(placed, silent)));
            return;
        }

        // Handle CUBOID fill mode
        for (Cuboid region : regions) {
            mine.getBlockSetter().fill(region, blocksSet -> {
                totalBlocks.addAndGet(blocksSet);

                if (completed.incrementAndGet() == regions.size()) {
                    Scheduler.get(plugin).run(() -> {
                        onResetComplete(totalBlocks.get(), silent);
                    });
                }
            });
        }
    }

    /**
     * Gets mask positions from config.
     */
    private List<Location> getMaskPositions() {
        List<Location> positions = new ArrayList<>();
        var mask = mine.getConfig().blocks().mask();
        if (mask == null) {
            return positions;
        }

        for (String posStr : mask.positions().keySet()) {
            try {
                Location loc = LocationParser.parse(posStr);
                if (loc != null) {
                    positions.add(loc);
                }
            } catch (Exception e) {
                plugin.loLogger().warn("Invalid mask position in mine " + mine.getName() + ": " + posStr);
            }
        }
        return positions;
    }

    /**
     * Called when all regions have been filled.
     * Executes post-reset actions in the main thread.
     */
    private void onResetComplete(int totalBlocks, boolean silent) {
        mine.getBlocksAtomic().set(totalBlocks);
        mine.getTicksAtomic().set(0);

        executeResetCommands();

        if (!silent) {
            broadcastReset();
        }

        if (mine.getConfig().teleport().enabled()) {
            teleportPlayers();
        }

        running.set(false);
    }

    /**
     * Executes commands configured for reset.
     */
    private void executeResetCommands() {
        List<String> commands = mine.getConfig().reset().commands();
        for (String command : commands) {
            String parsed = command.replace("%mine%", mine.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), parsed);
        }
    }

    /**
     * Broadcasts reset message to players.
     */
    private void broadcastReset() {
        String message = mine.getConfig().reset().broadcastMessage();
        if (message != null && !message.isEmpty()) {
            String formatted = message.replace("%mine%", mine.getName());
            Component component = MiniMessage.miniMessage().deserialize(formatted);
            plugin.getServer().broadcast(component);
        }
    }

    /**
     * Teleports players standing inside the mine to a safe location near the configured destination.
     * Prevents players from suffocating in blocks by finding a safe teleport spot.
     */
    private void teleportPlayers() {
        var destOpt = mine.getConfig().teleport().getLocation();
        if (destOpt.isEmpty()) {
            return;
        }
        Location dest = destOpt.get();
        if (dest.getWorld() == null) {
            return;
        }

        // Find a safe teleport location to prevent suffocation
        Location safeDest = BlockUpdateUtil.findSafeTeleportLocation(dest);

        for (Player p : dest.getWorld().getPlayers()) {
            if (mine.contains(p.getLocation())) {
                p.teleport(safeDest);
            }
        }
    }
}
