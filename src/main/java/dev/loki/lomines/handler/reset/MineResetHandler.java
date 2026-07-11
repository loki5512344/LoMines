package dev.loki.lomines.handler.reset;

import dev.loki.lomines.LoMinesPlugin;
import dev.loki.lomines.core.mine.model.Mine;
import dev.loki.lomines.data.config.block.FillMode;
import dev.loki.lomines.util.location.geo.Cuboid;
import dev.loki.lomines.util.location.LocationParser;
import org.bukkit.Bukkit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** Thread-safe mine reset with async fill and post-reset actions. */
public final class MineResetHandler {

    private final Mine mine;
    private final LoMinesPlugin plugin;
    private final PlayerTeleportHandler teleportHandler;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public MineResetHandler(Mine mine, LoMinesPlugin plugin) {
        this.mine = mine;
        this.plugin = plugin;
        this.teleportHandler = new PlayerTeleportHandler(mine);
    }

    public void reset(boolean silent) {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            resetAllRegions(silent);
        } catch (Exception e) {
            running.set(false);
            plugin.getLogger().severe("Failed to reset mine " + mine.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetAllRegions(boolean silent) {
        List<Cuboid> regions = mine.getRegions();
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger totalBlocks = new AtomicInteger(0);
        if (mine.getBlockSetter() == null) {
            plugin.getLogger().warning("BlockSetter not initialized for mine " + mine.getName());
            running.set(false);
            return;
        }
        if (mine.getConfig().blocks().fillMode() == FillMode.MASK) {
            List<Location> maskPositions = getMaskPositions();
            if (maskPositions.isEmpty()) {
                plugin.getLogger().warning("No mask positions found for mine " + mine.getName());
                running.set(false);
                return;
            }
            mine.getBlockSetter().fillAtLocations(maskPositions, placed ->
                    Bukkit.getScheduler().runTask(plugin, () -> onResetComplete(placed, silent)));
            return;
        }
        for (Cuboid region : regions) {
            mine.getBlockSetter().fill(region, blocksSet -> {
                totalBlocks.addAndGet(blocksSet);
                if (completed.incrementAndGet() == regions.size()) {
                    Bukkit.getScheduler().runTask(plugin, () -> onResetComplete(totalBlocks.get(), silent));
                }
            });
        }
    }

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
                plugin.getLogger().warning("Invalid mask position in mine " + mine.getName() + ": " + posStr);
            }
        }
        return positions;
    }

    private void onResetComplete(int totalBlocks, boolean silent) {
        mine.getBlocksAtomic().set(totalBlocks);
        mine.getTicksAtomic().set(0);
        executeResetCommands();
        if (!silent) {
            broadcastReset();
        }
        if (mine.getConfig().teleport().enabled()) {
            teleportHandler.teleportStuckPlayers();
        }
        running.set(false);
    }

    private void executeResetCommands() {
        List<String> commands = mine.getConfig().reset().commands();
        for (String command : commands) {
            String parsed = command.replace("%mine%", mine.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), parsed);
        }
    }

    private void broadcastReset() {
        String message = mine.getConfig().reset().broadcastMessage();
        if (message == null || message.isEmpty()) {
            return;
        }
        String formatted = message.replace("%mine%", mine.getName());
        Component component = MiniMessage.miniMessage().deserialize(formatted);
        plugin.getServer().broadcast(component);
    }
}
